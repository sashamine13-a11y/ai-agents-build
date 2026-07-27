package com.aiagents.app.domain.agents

import com.aiagents.app.data.model.*
import com.aiagents.app.data.repository.AiRepository

class AgentEngine(private val repository: AiRepository) {

    val agents = listOf(
        AgentConfig(
            id = "coder",
            name = "CodeAgent",
            description = "Writes and edits code",
            systemPrompt = "You are an experienced senior developer. Write clean, documented code. Use modern practices. Respond in Russian.",
            provider = AiProvider.GEMINI,
            modelName = "gemini-1.5-flash",
            color = 0xFF4CAF50,
            icon = "\uD83D\uDCBB"
        ),
        AgentConfig(
            id = "planner",
            name = "PlannerAgent",
            description = "Breaks tasks into steps",
            systemPrompt = "You are a software architect. Break down requests into concrete steps and determine which agents are needed.",
            provider = AiProvider.DEEPSEEK,
            modelName = "deepseek-chat",
            color = 0xFF2196F3,
            icon = "\uD83D\uDCCB"
        ),
        AgentConfig(
            id = "file",
            name = "FileAgent",
            description = "Works with file system",
            systemPrompt = "You are a file manager. Handle file operations. Respond concisely.",
            provider = AiProvider.QWEN,
            modelName = "qwen-turbo",
            color = 0xFFFF9800,
            icon = "\uD83D\uDCC1"
        ),
        AgentConfig(
            id = "research",
            name = "ResearchAgent",
            description = "Searches and analyzes",
            systemPrompt = "You are a researcher. Analyze deeply and find non-trivial solutions.",
            provider = AiProvider.GEMINI,
            modelName = "gemini-1.5-flash",
            color = 0xFF9C27B0,
            icon = "\uD83D\uDD0D"
        ),
        AgentConfig(
            id = "review",
            name = "ReviewAgent",
            description = "Reviews code",
            systemPrompt = "You are a code reviewer. Find bugs and vulnerabilities. Suggest improvements. Rate code quality 1-10.",
            provider = AiProvider.DEEPSEEK,
            modelName = "deepseek-chat",
            color = 0xFFE91E63,
            icon = "\uD83D\uDC41\uFE0F"
        ),
        AgentConfig(
            id = "translator",
            name = "TranslateAgent",
            description = "Translates languages",
            systemPrompt = "You are a professional translator. Translate accurately preserving style and terminology.",
            provider = AiProvider.QWEN,
            modelName = "qwen-turbo",
            color = 0xFF00BCD4,
            icon = "\uD83C\uDF10"
        )
    )

    suspend fun executeAgent(
        agentId: String,
        userMessage: String,
        history: List<ChatMessage> = emptyList()
    ): AgentResult {
        val agent = agents.find { it.id == agentId }
            ?: return AgentResult(agentId, "Agent not found", isError = true)

        val messages = history + ChatMessage("user", userMessage)

        return try {
            val response = repository.sendMessage(
                provider = agent.provider,
                model = agent.modelName,
                systemPrompt = agent.systemPrompt,
                messages = messages
            )
            val toolCalls = parseToolCalls(response)
            AgentResult(agentId, response, toolCalls)
        } catch (e: Exception) {
            AgentResult(agentId, "Error: ${e.message}", isError = true)
        }
    }

    suspend fun executeMultiAgent(userRequest: String): List<AgentResult> {
        val results = mutableListOf<AgentResult>()

        val planResult = executeAgent("planner", userRequest)
        results.add(planResult)

        val plan = planResult.content
        val agentMentions = agents.filter { it.id != "planner" }
            .filter { plan.contains("@" + it.id) || plan.contains(it.name) }

        for (mentionedAgent in agentMentions) {
            val result = executeAgent(
                mentionedAgent.id,
                "Plan: $plan. Task: $userRequest. Execute your part.",
                history = listOf(ChatMessage("assistant", plan))
            )
            results.add(result)
        }

        if (agentMentions.isEmpty()) {
            val fallback = executeAgent("coder", userRequest)
            results.add(fallback)
        }

        return results
    }

    private fun parseToolCalls(text: String): List<ToolCall> {
        val calls = mutableListOf<ToolCall>()
        val regex = """(EDIT_FILE|CREATE_FILE|READ_FILE|WRITE_FILE|LIST_DIR|ANALYZE)\|([^|]+)(?:\|(.+))?""".toRegex(RegexOption.DOT_MATCHES_ALL)

        regex.findAll(text).forEach { match ->
            calls.add(ToolCall(
                toolName = match.groupValues[1],
                parameters = mapOf(
                    "path" to match.groupValues[2].trim(),
                    "content" to match.groupValues[3].trim()
                )
            ))
        }
        return calls
    }
}
