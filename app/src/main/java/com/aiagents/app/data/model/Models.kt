package com.aiagents.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

enum class AiProvider {
    GEMINI, DEEPSEEK, QWEN, MISTRAL
}

data class AgentConfig(
    val id: String,
    val name: String,
    val description: String,
    val systemPrompt: String,
    val provider: AiProvider,
    val modelName: String,
    val color: Long,
    val icon: String
)

data class AgentResult(
    val agentId: String,
    val content: String,
    val toolCalls: List<ToolCall> = emptyList(),
    val isError: Boolean = false
)

data class ToolCall(
    val toolName: String,
    val parameters: Map<String, String>
)

data class ApiKeys(
    val gemini: String = "",
    val deepseek: String = "",
    val qwen: String = "",
    val mistral: String = ""
) {
    fun isAnySet(): Boolean = gemini.isNotBlank() || deepseek.isNotBlank() ||
                              qwen.isNotBlank() || mistral.isNotBlank()
}
