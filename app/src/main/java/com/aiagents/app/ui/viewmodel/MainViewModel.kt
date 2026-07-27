package com.aiagents.app.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aiagents.app.data.datastore.SettingsDataStore
import com.aiagents.app.data.model.*
import com.aiagents.app.data.repository.AiRepository
import com.aiagents.app.domain.agents.AgentEngine
import com.aiagents.app.domain.files.FileManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = SettingsDataStore(application)
    private val fileManager = FileManager(application)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedAgent = MutableStateFlow<AgentConfig?>(null)
    val selectedAgent: StateFlow<AgentConfig?> = _selectedAgent.asStateFlow()

    private val _apiKeys = MutableStateFlow(ApiKeys())
    val apiKeys: StateFlow<ApiKeys> = _apiKeys.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var agentEngine: AgentEngine? = null
    val availableAgents: List<AgentConfig>
        get() = agentEngine?.agents ?: emptyList()

    init {
        viewModelScope.launch {
            settingsDataStore.apiKeys.collect { keys ->
                _apiKeys.value = keys
                updateRepository(keys)
            }
        }
    }

    private fun updateRepository(keys: ApiKeys) {
        val repo = AiRepository(
            geminiKey = keys.gemini.takeIf { it.isNotBlank() },
            deepSeekKey = keys.deepseek.takeIf { it.isNotBlank() },
            qwenKey = keys.qwen.takeIf { it.isNotBlank() },
            mistralKey = keys.mistral.takeIf { it.isNotBlank() }
        )
        agentEngine = AgentEngine(repo)
        if (_selectedAgent.value == null && agentEngine != null) {
            _selectedAgent.value = agentEngine!!.agents.firstOrNull()
        }
    }

    fun selectAgent(agent: AgentConfig) {
        _selectedAgent.value = agent
    }

    fun sendMessage(text: String, useMultiAgent: Boolean = false) {
        if (text.isBlank()) return
        if (!apiKeys.value.isAnySet()) {
            _error.value = "Add API keys in settings"
            _showSettings.value = true
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val userMsg = ChatMessage("user", text)
            _messages.value += userMsg

            try {
                val engine = agentEngine ?: throw Exception("Agents not initialized")

                val results = if (useMultiAgent) {
                    engine.executeMultiAgent(text)
                } else {
                    val agent = _selectedAgent.value ?: engine.agents.first()
                    listOf(engine.executeAgent(agent.id, text, _messages.value))
                }

                results.forEach { result ->
                    val agentName = engine.agents.find { it.id == result.agentId }?.name ?: result.agentId
                    val prefix = if (result.isError) "Error" else "Agent"
                    _messages.value += ChatMessage(
                        role = if (result.isError) "error" else "assistant",
                        content = "[$prefix $agentName]\n${result.content}"
                    )

                    result.toolCalls.forEach { tool ->
                        executeToolCall(tool)
                    }
                }
            } catch (e: Exception) {
                _messages.value += ChatMessage("error", "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun executeToolCall(tool: ToolCall) {
        try {
            when (tool.toolName) {
                "READ_FILE" -> {
                    val uri = Uri.parse(tool.parameters["path"])
                    val content = fileManager.readFile(uri)
                    _messages.value += ChatMessage(
                        "system",
                        "[File Content]\n${content.take(2000)}"
                    )
                }
                "LIST_DIR" -> {
                    val uri = Uri.parse(tool.parameters["path"])
                    val files = fileManager.listFiles(uri)
                    val list = files.joinToString("\n") { "- ${it.name}" }
                    _messages.value += ChatMessage("system", "[Files]\n$list")
                }
                else -> {
                    _messages.value += ChatMessage("system", "[Tool] ${tool.toolName}")
                }
            }
        } catch (e: Exception) {
            _messages.value += ChatMessage("error", "Tool error: ${e.message}")
        }
    }

    fun saveKeys(keys: ApiKeys) {
        viewModelScope.launch {
            settingsDataStore.saveKeys(keys)
            _showSettings.value = false
        }
    }

    fun toggleSettings() {
        _showSettings.value = !_showSettings.value
    }

    fun dismissError() {
        _error.value = null
    }

    fun clearChat() {
        _messages.value = emptyList()
    }

    fun attachFile(uri: Uri) {
        try {
            val name = fileManager.getFileName(uri) ?: "file"
            val content = fileManager.readFile(uri)
            val preview = content.take(500)
            _messages.value += ChatMessage(
                "user",
                "[Attached: $name]\n$preview"
            )
        } catch (e: Exception) {
            _error.value = "Failed to read file: ${e.message}"
        }
    }
}
