package com.aiagents.app.data.api

import com.aiagents.app.data.model.ChatMessage
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class QwenApi(private val apiKey: String) {

    @Serializable
    data class QwenRequest(
        val model: String = "qwen-turbo",
        val input: Input,
        val parameters: Parameters = Parameters()
    )

    @Serializable
    data class Input(val messages: List<ChatMessage>)

    @Serializable
    data class Parameters(
        val temperature: Float = 0.7f,
        val max_tokens: Int = 8192,
        val result_format: String = "message"
    )

    @Serializable
    data class QwenResponse(val output: Output?)

    @Serializable
    data class Output(val choices: List<Choice>?)

    @Serializable
    data class Choice(val message: ChatMessage?)

    suspend fun chat(messages: List<ChatMessage>): String {
        val response: QwenResponse = HttpClientProvider.client.post(
            "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation"
        ) {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            header("X-DashScope-SSE", "disable")
            contentType(ContentType.Application.Json)
            setBody(QwenRequest(input = Input(messages)))
        }.body()

        return response.output?.choices?.firstOrNull()?.message?.content
            ?: throw Exception("Empty response from Qwen")
    }
}
