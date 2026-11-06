package com.nexora.ai.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Request payload for Google Gemini generateContent API.
 */
@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents")
    val contents: List<Content>,
    @Json(name = "systemInstruction")
    val systemInstruction: Content? = null,
    @Json(name = "generationConfig")
    val generationConfig: GenerationConfig? = null,
    @Json(name = "safetySettings")
    val safetySettings: List<SafetySetting>? = null
) {
    companion object {
        fun fromPrompt(
            prompt: String,
            systemInstruction: String? = null,
            temperature: Float? = 0.7f,
            maxOutputTokens: Int? = 4096
        ): GeminiRequest {
            val systemContent = systemInstruction?.let {
                Content(
                    role = "system",
                    parts = listOf(Part(text = it))
                )
            }
            return GeminiRequest(
                contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(Part(text = prompt))
                    )
                ),
                systemInstruction = systemContent,
                generationConfig = GenerationConfig(
                    temperature = temperature,
                    maxOutputTokens = maxOutputTokens
                )
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "role")
    val role: String? = null, // "user", "model", or "system"
    @Json(name = "parts")
    val parts: List<Part> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text")
    val text: String? = null,
    @Json(name = "inlineData")
    val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    @Json(name = "mimeType")
    val mimeType: String,
    @Json(name = "data")
    val data: String // Base64 encoded bytes
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature")
    val temperature: Float? = null,
    @Json(name = "topP")
    val topP: Float? = null,
    @Json(name = "topK")
    val topK: Int? = null,
    @Json(name = "maxOutputTokens")
    val maxOutputTokens: Int? = null,
    @Json(name = "stopSequences")
    val stopSequences: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class SafetySetting(
    @Json(name = "category")
    val category: String,
    @Json(name = "threshold")
    val threshold: String
)

/**
 * Response payload returned by Gemini API.
 */
@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates")
    val candidates: List<Candidate>? = null,
    @Json(name = "usageMetadata")
    val usageMetadata: UsageMetadata? = null,
    @Json(name = "promptFeedback")
    val promptFeedback: PromptFeedback? = null
) {
    /**
     * Extracts the combined text from the first candidate response.
     */
    fun firstTextOrNull(): String? {
        val firstCandidate = candidates?.firstOrNull() ?: return null
        val parts = firstCandidate.content?.parts ?: return null
        val textBuilder = StringBuilder()
        for (part in parts) {
            if (!part.text.isNullOrEmpty()) {
                textBuilder.append(part.text)
            }
        }
        val result = textBuilder.toString()
        return if (result.isNotBlank()) result else null
    }
}

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content")
    val content: Content? = null,
    @Json(name = "finishReason")
    val finishReason: String? = null,
    @Json(name = "index")
    val index: Int? = null
)

@JsonClass(generateAdapter = true)
data class UsageMetadata(
    @Json(name = "promptTokenCount")
    val promptTokenCount: Int? = null,
    @Json(name = "candidatesTokenCount")
    val candidatesTokenCount: Int? = null,
    @Json(name = "totalTokenCount")
    val totalTokenCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class PromptFeedback(
    @Json(name = "blockReason")
    val blockReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiErrorResponse(
    @Json(name = "error")
    val error: GeminiErrorDetails? = null
)

@JsonClass(generateAdapter = true)
data class GeminiErrorDetails(
    @Json(name = "code")
    val code: Int? = null,
    @Json(name = "message")
    val message: String? = null,
    @Json(name = "status")
    val status: String? = null
)

/**
 * Models for querying available models from Google Generative Language API
 */
@JsonClass(generateAdapter = true)
data class ModelListResponse(
    @Json(name = "models")
    val models: List<ModelInfo>? = null,
    @Json(name = "nextPageToken")
    val nextPageToken: String? = null
)

@JsonClass(generateAdapter = true)
data class ModelInfo(
    @Json(name = "name")
    val name: String, // e.g. "models/gemini-2.0-flash"
    @Json(name = "version")
    val version: String? = null,
    @Json(name = "displayName")
    val displayName: String? = null,
    @Json(name = "description")
    val description: String? = null,
    @Json(name = "supportedGenerationMethods")
    val supportedGenerationMethods: List<String>? = null
) {
    /**
     * Clean model name without "models/" prefix
     */
    fun cleanName(): String {
        return name.removePrefix("models/")
    }

    fun supportsGenerateContent(): Boolean {
        return supportedGenerationMethods?.contains("generateContent") == true
    }
}
