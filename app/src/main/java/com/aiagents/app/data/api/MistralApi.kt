package com.aiagents.app.data.api

import com.aiagents.app.data.model.ChatMessage
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class MistralApi(private val apiKey: String) {

    @Serializable
    data class MistralRequest(
        val model: String = "mistral-tiny",
        val messages: List<ChatMessage>,
        val temperature: Float = 0.7f,
        val max_tokens: Int = 4096
    )

    @Serializable
    data class MistralResponse(val choices: List<Choice>)

    @Serializable
    data class Choice(val message: ChatMessage)

    suspend fun chat(messages: List<ChatMessage>): String {
        val response: MistralResponse = HttpClientProvider.client.post(
            "https://api.mistral.ai/v1/chat/completions"
        ) {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(MistralRequest(messages = messages))
        }.body()

        return response.choices.firstOrNull()?.message?.content
            ?: throw Exception("Empty response from Mistral")
    }
}
