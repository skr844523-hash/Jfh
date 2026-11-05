package com.nexora.ai.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.nexora.ai.BuildConfig
import com.nexora.ai.security.KeystoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository interface for secure management of sensitive API keys.
 */
interface SecureKeyRepository {
    suspend fun getGeminiApiKey(): String?
    suspend fun saveGeminiApiKey(apiKey: String)
    suspend fun clearGeminiApiKey()
    suspend fun hasApiKey(): Boolean
    suspend fun isCustomKeySet(): Boolean
}

/**
 * Implementation of [SecureKeyRepository] using [KeystoreManager]
 * for hardware-backed encryption of keys stored in private SharedPreferences,
 * with graceful fallback to BuildConfig.GEMINI_API_KEY if configured.
 */
class SecureKeyRepositoryImpl(
    context: Context,
    private val keystoreManager: KeystoreManager = KeystoreManager()
) : SecureKeyRepository {

    companion object {
        private const val PREFS_NAME = "nexora_secure_prefs"
        private const val KEY_ENCRYPTED_GEMINI_KEY = "encrypted_gemini_api_key"
    }

    private val sharedPreferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun getGeminiApiKey(): String? = withContext(Dispatchers.IO) {
        // 1. Try to fetch user-defined encrypted key from Keystore storage
        val encryptedKey = sharedPreferences.getString(KEY_ENCRYPTED_GEMINI_KEY, null)
        if (!encryptedKey.isNullOrBlank()) {
            val decrypted = keystoreManager.decrypt(encryptedKey)
            if (!decrypted.isNullOrBlank()) {
                return@withContext decrypted
            }
        }

        // 2. Fall back to BuildConfig.GEMINI_API_KEY injected at build time
        val buildConfigKey = BuildConfig.GEMINI_API_KEY
        if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
            return@withContext buildConfigKey
        }

        null
    }

    override suspend fun saveGeminiApiKey(apiKey: String): Unit = withContext(Dispatchers.IO) {
        val trimmed = apiKey.trim()
        if (trimmed.isEmpty()) {
            clearGeminiApiKey()
            return@withContext
        }
        val encrypted = keystoreManager.encrypt(trimmed)
        sharedPreferences.edit()
            .putString(KEY_ENCRYPTED_GEMINI_KEY, encrypted)
            .apply()
    }

    override suspend fun clearGeminiApiKey(): Unit = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .remove(KEY_ENCRYPTED_GEMINI_KEY)
            .apply()
    }

    override suspend fun hasApiKey(): Boolean = withContext(Dispatchers.IO) {
        !getGeminiApiKey().isNullOrBlank()
    }

    override suspend fun isCustomKeySet(): Boolean = withContext(Dispatchers.IO) {
        val encryptedKey = sharedPreferences.getString(KEY_ENCRYPTED_GEMINI_KEY, null)
        !encryptedKey.isNullOrBlank()
    }
}
