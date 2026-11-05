package com.nexora.ai.data.api

import android.content.Context
import com.nexora.ai.data.api.model.Content
import com.nexora.ai.data.api.model.GeminiErrorResponse
import com.nexora.ai.data.api.model.GeminiRequest
import com.nexora.ai.data.api.model.GeminiResponse
import com.nexora.ai.data.api.model.Part
import com.nexora.ai.data.repository.SecureKeyRepository
import com.nexora.ai.data.repository.SecureKeyRepositoryImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Retrofit REST interface for Google Gemini Generative Language API.
 */
interface GeminiApi {

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContentWithQuery(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}

/**
 * High-level Gemini AI Service that integrates Retrofit and the secure Android Keystore
 * repository to safely execute Gemini requests.
 */
class GeminiService(
    private val api: GeminiApi,
    private val secureKeyRepository: SecureKeyRepository,
    private val moshi: Moshi
) {

    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/"
        const val DEFAULT_MODEL = "gemini-2.0-flash"
        const val FALLBACK_MODEL = "gemini-1.5-flash"
        const val PRO_MODEL = "gemini-1.5-pro"

        /**
         * Factory function to create an instance of [GeminiService] with default Retrofit configuration
         * and hardware-backed Keystore security.
         */
        fun create(
            context: Context,
            secureKeyRepository: SecureKeyRepository = SecureKeyRepositoryImpl(context)
        ): GeminiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            val api = retrofit.create(GeminiApi::class.java)
            return GeminiService(api, secureKeyRepository, moshi)
        }
    }

    /**
     * Checks if a valid API key is present either in Android Keystore or BuildConfig.
     */
    suspend fun isConfigured(): Boolean {
        return secureKeyRepository.hasApiKey()
    }

    /**
     * Gets the currently active API key from the secure keystore repository.
     */
    suspend fun getActiveApiKey(): String? {
        return secureKeyRepository.getGeminiApiKey()
    }

    /**
     * Executes a raw [GeminiRequest] using the API key securely retrieved from Keystore.
     * Includes automatic fallback for header/query parameter authentication and models.
     */
    suspend fun generateContent(
        request: GeminiRequest,
        model: String = DEFAULT_MODEL
    ): Result<GeminiResponse> = withContext(Dispatchers.IO) {
        val apiKey = secureKeyRepository.getGeminiApiKey()
        if (apiKey.isNullOrBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is missing. Please set your key in NEXORA Settings.")
            )
        }

        try {
            // First attempt: Header-based authentication with requested model
            var response = api.generateContent(
                model = model,
                apiKey = apiKey,
                request = request
            )

            // If model not found or header rejected, attempt query param with requested or fallback model
            if (!response.isSuccessful && response.code() in listOf(400, 404)) {
                val fallbackModel = if (model != FALLBACK_MODEL) FALLBACK_MODEL else DEFAULT_MODEL
                response = api.generateContentWithQuery(
                    model = fallbackModel,
                    apiKey = apiKey,
                    request = request
                )
            }

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(IllegalStateException("Empty response body from Gemini API."))
                }
            } else {
                val errorMsg = parseErrorMessage(response)
                Result.failure(Exception("Gemini API Error (${response.code()}): $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Convenience method to send a simple text prompt and get the response text.
     */
    suspend fun generateText(
        prompt: String,
        systemInstruction: String? = null,
        model: String = DEFAULT_MODEL
    ): Result<String> {
        val request = GeminiRequest.fromPrompt(
            prompt = prompt,
            systemInstruction = systemInstruction
        )

        return generateContent(request, model).mapCatching { response ->
            response.firstTextOrNull()
                ?: throw IllegalStateException("Gemini returned no text content.")
        }
    }

    /**
     * Multi-turn chat generation with conversation history and system instructions.
     */
    suspend fun generateChatResponse(
        history: List<Content>,
        userMessage: String,
        systemInstruction: String? = null,
        model: String = DEFAULT_MODEL
    ): Result<String> {
        val updatedContents = ArrayList(history).apply {
            add(
                Content(
                    role = "user",
                    parts = listOf(Part(text = userMessage))
                )
            )
        }

        val systemContent = systemInstruction?.let {
            Content(
                role = "system",
                parts = listOf(Part(text = it))
            )
        }

        val request = GeminiRequest(
            contents = updatedContents,
            systemInstruction = systemContent
        )

        return generateContent(request, model).mapCatching { response ->
            response.firstTextOrNull()
                ?: throw IllegalStateException("Gemini returned no candidate response.")
        }
    }

    /**
     * Parses error response body from Gemini API.
     */
    private fun parseErrorMessage(response: Response<*>): String {
        return try {
            val errorBodyString = response.errorBody()?.string()
            if (!errorBodyString.isNullOrBlank()) {
                val adapter = moshi.adapter(GeminiErrorResponse::class.java)
                val parsed = adapter.fromJson(errorBodyString)
                parsed?.error?.message ?: errorBodyString
            } else {
                "HTTP ${response.code()} ${response.message()}"
            }
        } catch (_: Exception) {
            "HTTP ${response.code()} ${response.message()}"
        }
    }
}
