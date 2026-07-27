package com.aiagents.app.data.repository

import com.aiagents.app.data.api.*
import com.aiagents.app.data.model.*

class AiRepository(
    private val geminiKey: String?,
    private val deepSeekKey: String?,
    private val qwenKey: String?,
    private val mistralKey: String?
) {
    private val gemini = geminiKey?.let(::GeminiApi)
    private val deepSeek = deepSeekKey?.let(::DeepSeekApi)
    private val qwen = qwenKey?.let(::QwenApi)
    private val mistral = mistralKey?.let(::MistralApi)

    suspend fun sendMessage(
        provider: AiProvider,
        model: String,
        systemPrompt: String?,
        messages: List<ChatMessage>
    ): String = when (provider) {
        AiProvider.GEMINI -> gemini?.generate(model, systemPrompt, messages)
        AiProvider.DEEPSEEK -> deepSeek?.chat(
            (systemPrompt?.let { listOf(ChatMessage("system", it)) } ?: emptyList()) + messages
        )
        AiProvider.QWEN -> qwen?.chat(
            (systemPrompt?.let { listOf(ChatMessage("system", it)) } ?: emptyList()) + messages
        )
        AiProvider.MISTRAL -> mistral?.chat(
            (systemPrompt?.let { listOf(ChatMessage("system", it)) } ?: emptyList()) + messages
        )
    } ?: throw Exception("API key for $provider not configured")

    fun getAvailableProviders(): List<AiProvider> {
        return buildList {
            if (geminiKey != null) add(AiProvider.GEMINI)
            if (deepSeekKey != null) add(AiProvider.DEEPSEEK)
            if (qwenKey != null) add(AiProvider.QWEN)
            if (mistralKey != null) add(AiProvider.MISTRAL)
        }
    }
}
