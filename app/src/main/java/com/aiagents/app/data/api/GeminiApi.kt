package com.aiagents.app.data.api

import com.aiagents.app.data.model.ChatMessage
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class GeminiApi(private val apiKey: String) {

    @Serializable
    data class GeminiRequest(
        val contents: List<Content>,
        val systemInstruction: Content? = null,
        val generationConfig: GenerationConfig? = null
    )

    @Serializable
    data class Content(val parts: List<Part>, val role: String? = null)

    @Serializable
    data class Part(val text: String)

    @Serializable
    data class GenerationConfig(
        val temperature: Float = 0.7f,
        val maxOutputTokens: Int = 8192
    )

    @Serializable
    data class GeminiResponse(val candidates: List<Candidate>?)

    @Serializable
    data class Candidate(val content: Content?)

    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models"

    suspend fun generate(
        model: String = "gemini-1.5-flash",
        systemPrompt: String?,
        messages: List<ChatMessage>
    ): String {
        val geminiContents = messages.map { msg ->
            Content(
                parts = listOf(Part(msg.content)),
                role = if (msg.role == "user") "user" else "model"
            )
        }

        val request = GeminiRequest(
            contents = geminiContents,
            systemInstruction = systemPrompt?.let { Content(parts = listOf(Part(it))) },
            generationConfig = GenerationConfig(temperature = 0.7f, maxOutputTokens = 8192)
        )

        val response: GeminiResponse = HttpClientProvider.client.post(
            "$baseUrl/$model:generateContent?key=$apiKey"
        ) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Empty response from Gemini")
    }
}
