package com.nexora.ai.data.api

import android.content.Context
import com.nexora.ai.data.api.model.Content
import com.nexora.ai.data.api.model.GeminiErrorResponse
import com.nexora.ai.data.api.model.GeminiRequest
import com.nexora.ai.data.api.model.GeminiResponse
import com.nexora.ai.data.api.model.ModelListResponse
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
import retrofit2.http.GET
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

    @POST("v1/models/{model}:generateContent")
    suspend fun generateContentV1(
        @Path("model") model: String,
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>

    @GET("v1beta/models")
    suspend fun listModels(
        @Header("x-goog-api-key") apiKey: String
    ): Response<ModelListResponse>

    @GET("v1beta/models")
    suspend fun listModelsWithQuery(
        @Query("key") apiKey: String
    ): Response<ModelListResponse>
}

/**
 * High-level Gemini AI Service that integrates Retrofit and the secure Android Keystore
 * repository to safely execute Gemini requests with multi-model resilience.
 */
class GeminiService(
    private val api: GeminiApi,
    private val secureKeyRepository: SecureKeyRepository,
    private val moshi: Moshi
) {

    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/"
        const val DEFAULT_MODEL = "gemini-2.0-flash"
        val CANDIDATE_MODELS = listOf(
            "gemini-2.0-flash",
            "gemini-1.5-flash",
            "gemini-1.5-flash-latest",
            "gemini-2.5-flash",
            "gemini-1.5-pro",
            "gemini-pro"
        )

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
     * Tests a specific API key against Google Generative Language API and returns
     * the list of supported generateContent model names if valid.
     */
    suspend fun testApiKey(apiKey: String): Result<List<String>> = withContext(Dispatchers.IO) {
        val trimmed = apiKey.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("API key cannot be empty"))
        }

        try {
            // First attempt with header
            var response = api.listModels(trimmed)
            if (!response.isSuccessful) {
                // Second attempt with query parameter
                response = api.listModelsWithQuery(trimmed)
            }

            if (response.isSuccessful) {
                val models = response.body()?.models
                    ?.filter { it.supportsGenerateContent() }
                    ?.map { it.cleanName() }
                    ?: emptyList()
                Result.success(models)
            } else {
                val errorMsg = parseErrorMessage(response)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Executes a raw [GeminiRequest] using the API key securely retrieved from Keystore.
     * Includes automatic fallback for candidate model names and v1/v1beta versions.
     */
    suspend fun generateContent(
        request: GeminiRequest,
        model: String? = null
    ): Result<GeminiResponse> = withContext(Dispatchers.IO) {
        val apiKey = secureKeyRepository.getGeminiApiKey()
        if (apiKey.isNullOrBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is missing. Please add your key in NEXORA Settings.")
            )
        }

        val primaryModel = model ?: secureKeyRepository.getActiveModel()
        val modelsToTry = LinkedHashSet<String>().apply {
            add(primaryModel)
            addAll(CANDIDATE_MODELS)
        }.toList()

        var lastError: Exception? = null

        for (candidateModel in modelsToTry) {
            try {
                // 1. Try v1beta with header
                var response = api.generateContent(
                    model = candidateModel,
                    apiKey = apiKey,
                    request = request
                )

                // 2. If 401 or 403, try query parameter authentication
                if (!response.isSuccessful && response.code() in listOf(401, 403)) {
                    response = api.generateContentWithQuery(
                        model = candidateModel,
                        apiKey = apiKey,
                        request = request
                    )
                }

                // 3. If 404, try v1 endpoint
                if (!response.isSuccessful && response.code() == 404) {
                    response = api.generateContentV1(
                        model = candidateModel,
                        apiKey = apiKey,
                        request = request
                    )
                }

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        // Persist successful model as active model preference
                        if (candidateModel != primaryModel) {
                            secureKeyRepository.setActiveModel(candidateModel)
                        }
                        return@withContext Result.success(body)
                    }
                } else {
                    val errorMsg = parseErrorMessage(response)
                    lastError = Exception("Gemini API (${response.code()}): $errorMsg")
                    // If error is 404 (model not found), loop to next candidate model!
                    if (response.code() != 404) {
                        // For authentication or quota errors, don't keep cycling models pointlessly
                        return@withContext Result.failure(lastError)
                    }
                }
            } catch (e: Exception) {
                lastError = e
            }
        }

        Result.failure(
            lastError ?: Exception("Could not find a compatible Gemini model for your API key.")
        )
    }

    /**
     * Convenience method to send a simple text prompt and get the response text.
     */
    suspend fun generateText(
        prompt: String,
        systemInstruction: String? = null,
        model: String? = null
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
        model: String? = null
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
     * Parses error response body from Gemini API with user-friendly Arabic guidance.
     */
    private fun parseErrorMessage(response: Response<*>): String {
        return try {
            val errorBodyString = response.errorBody()?.string()
            val rawMsg = if (!errorBodyString.isNullOrBlank()) {
                val adapter = moshi.adapter(GeminiErrorResponse::class.java)
                val parsed = adapter.fromJson(errorBodyString)
                parsed?.error?.message ?: errorBodyString
            } else {
                "HTTP ${response.code()} ${response.message()}"
            }

            when {
                response.code() == 400 && (rawMsg.contains("API key not valid", ignoreCase = true) || rawMsg.contains("API_KEY_INVALID", ignoreCase = true)) -> {
                    "مفتاح API غير صالح (API Key Invalid). تأكد من إنشاء ونسخ المفتاح الصحيح من Google AI Studio (يبدأ عادةً بـ AIzaSy...)."
                }
                response.code() in listOf(401, 403) || rawMsg.contains("unregistered callers", ignoreCase = true) -> {
                    "المفتاح المدخل غير مسجل أو لا يملك صلاحيات Google Generative AI. يرجى الحصول على مفتاح رسمي من Google AI Studio."
                }
                response.code() == 429 || rawMsg.contains("quota", ignoreCase = true) || rawMsg.contains("RESOURCE_EXHAUSTED", ignoreCase = true) -> {
                    "تم تجاوز الحد المجاني للمفتاح مؤقتاً (Rate Limit / Quota). انتظر بضع لحظات أو أدخل مفتاحاً بديلاً."
                }
                response.code() == 404 -> {
                    "النموذج غير متوفر لهذا الإصدار. جاري المحاولة بنموذج بديل متوافق تلقائياً."
                }
                else -> rawMsg
            }
        } catch (_: Exception) {
            "HTTP ${response.code()} ${response.message()}"
        }
    }
}
