package com.aiagents.app.data.api

import com.aiagents.app.data.model.ChatMessage
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class DeepSeekApi(private val apiKey: String) {

    @Serializable
    data class DSRequest(
        val model: String = "deepseek-chat",
        val messages: List<ChatMessage>,
        val temperature: Float = 0.7f,
        val max_tokens: Int = 8192
    )

    @Serializable
    data class DSResponse(val choices: List<Choice>)

    @Serializable
    data class Choice(val message: ChatMessage)

    suspend fun chat(messages: List<ChatMessage>): String {
        val response: DSResponse = HttpClientProvider.client.post(
            "https://api.deepseek.com/chat/completions"
        ) {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(DSRequest(messages = messages))
        }.body()

        return response.choices.firstOrNull()?.message?.content
            ?: throw Exception("Empty response from DeepSeek")
    }
}
