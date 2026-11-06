package com.nexora.ai.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.nexora.ai.BuildConfig
import com.nexora.ai.security.KeystoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Data model representing a saved API Key in NEXORA AI.
 */
data class ApiKeyItem(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val apiKey: String,
    val preferredModel: String = "gemini-2.0-flash",
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Returns a safe masked version of the key for display (e.g. AIza••••9AbX)
     */
    fun getMaskedKey(): String {
        val trimmed = apiKey.trim()
        if (trimmed.length <= 8) return "••••••••"
        return "${trimmed.take(4)}••••${trimmed.takeLast(4)}"
    }
}

/**
 * Repository interface for secure management of sensitive API keys.
 * Supports multiple API keys, custom model preferences, and active key switching.
 */
interface SecureKeyRepository {
    suspend fun getGeminiApiKey(): String?
    suspend fun saveGeminiApiKey(apiKey: String)
    suspend fun clearGeminiApiKey()
    suspend fun hasApiKey(): Boolean
    suspend fun isCustomKeySet(): Boolean

    // Multi-key capabilities
    suspend fun getAllKeys(): List<ApiKeyItem>
    suspend fun addOrUpdateKey(label: String, apiKey: String, preferredModel: String = "gemini-2.0-flash", setAsActive: Boolean = true): ApiKeyItem
    suspend fun deleteKey(id: String)
    suspend fun setActiveKey(id: String)
    suspend fun getActiveKeyItem(): ApiKeyItem?
    suspend fun getActiveModel(): String
    suspend fun setActiveModel(model: String)
}

/**
 * Implementation of [SecureKeyRepository] using [KeystoreManager]
 * for hardware-backed encryption of keys stored in private SharedPreferences.
 */
class SecureKeyRepositoryImpl(
    context: Context,
    private val keystoreManager: KeystoreManager = KeystoreManager()
) : SecureKeyRepository {

    companion object {
        private const val PREFS_NAME = "nexora_secure_prefs"
        private const val KEY_ENCRYPTED_GEMINI_KEY = "encrypted_gemini_api_key"
        private const val KEY_ENCRYPTED_KEYS_JSON = "encrypted_keys_json_v2"
        private const val KEY_ACTIVE_MODEL = "active_gemini_model"
        private const val DEFAULT_MODEL = "gemini-2.0-flash"
    }

    private val sharedPreferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun getGeminiApiKey(): String? = withContext(Dispatchers.IO) {
        // 1. Check if an active key item exists in the multi-key list
        val keys = getAllKeys()
        val activeKey = keys.firstOrNull { it.isActive }?.apiKey
            ?: keys.firstOrNull()?.apiKey

        if (!activeKey.isNullOrBlank()) {
            return@withContext activeKey
        }

        // 2. Legacy fallback from single encrypted key
        val legacyEncrypted = sharedPreferences.getString(KEY_ENCRYPTED_GEMINI_KEY, null)
        if (!legacyEncrypted.isNullOrBlank()) {
            val decrypted = keystoreManager.decrypt(legacyEncrypted)
            if (!decrypted.isNullOrBlank()) {
                return@withContext decrypted
            }
        }

        // 3. Fall back to BuildConfig.GEMINI_API_KEY injected at build time
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
        addOrUpdateKey(
            label = "Default Key",
            apiKey = trimmed,
            preferredModel = getActiveModel(),
            setAsActive = true
        )
    }

    override suspend fun clearGeminiApiKey(): Unit = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .remove(KEY_ENCRYPTED_GEMINI_KEY)
            .remove(KEY_ENCRYPTED_KEYS_JSON)
            .apply()
    }

    override suspend fun hasApiKey(): Boolean = withContext(Dispatchers.IO) {
        !getGeminiApiKey().isNullOrBlank()
    }

    override suspend fun isCustomKeySet(): Boolean = withContext(Dispatchers.IO) {
        getAllKeys().isNotEmpty() || !sharedPreferences.getString(KEY_ENCRYPTED_GEMINI_KEY, null).isNullOrBlank()
    }

    override suspend fun getAllKeys(): List<ApiKeyItem> = withContext(Dispatchers.IO) {
        val encryptedJson = sharedPreferences.getString(KEY_ENCRYPTED_KEYS_JSON, null)
        if (encryptedJson.isNullOrBlank()) {
            // Check legacy key
            val legacyEncrypted = sharedPreferences.getString(KEY_ENCRYPTED_GEMINI_KEY, null)
            if (!legacyEncrypted.isNullOrBlank()) {
                val decrypted = keystoreManager.decrypt(legacyEncrypted)
                if (!decrypted.isNullOrBlank()) {
                    return@withContext listOf(
                        ApiKeyItem(
                            label = "Main Key",
                            apiKey = decrypted,
                            preferredModel = getActiveModel(),
                            isActive = true
                        )
                    )
                }
            }
            return@withContext emptyList()
        }

        try {
            val decryptedJson = keystoreManager.decrypt(encryptedJson) ?: return@withContext emptyList()
            val jsonArray = JSONArray(decryptedJson)
            val list = mutableListOf<ApiKeyItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ApiKeyItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        label = obj.optString("label", "API Key"),
                        apiKey = obj.optString("apiKey", ""),
                        preferredModel = obj.optString("preferredModel", DEFAULT_MODEL),
                        isActive = obj.optBoolean("isActive", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun addOrUpdateKey(
        label: String,
        apiKey: String,
        preferredModel: String,
        setAsActive: Boolean
    ): ApiKeyItem = withContext(Dispatchers.IO) {
        val currentKeys = getAllKeys().toMutableList()
        val trimmedKey = apiKey.trim()
        val trimmedLabel = label.trim().ifEmpty { "API Key #${currentKeys.size + 1}" }

        val newId = UUID.randomUUID().toString()
        val updatedList = mutableListOf<ApiKeyItem>()

        if (setAsActive) {
            currentKeys.forEach {
                updatedList.add(it.copy(isActive = false))
            }
        } else {
            updatedList.addAll(currentKeys)
        }

        val newItem = ApiKeyItem(
            id = newId,
            label = trimmedLabel,
            apiKey = trimmedKey,
            preferredModel = preferredModel,
            isActive = setAsActive || updatedList.isEmpty(),
            createdAt = System.currentTimeMillis()
        )
        updatedList.add(0, newItem)

        persistKeys(updatedList)

        // Also update legacy single key for backwards compatibility
        if (newItem.isActive) {
            val encrypted = keystoreManager.encrypt(trimmedKey)
            sharedPreferences.edit()
                .putString(KEY_ENCRYPTED_GEMINI_KEY, encrypted)
                .putString(KEY_ACTIVE_MODEL, preferredModel)
                .apply()
        }

        newItem
    }

    override suspend fun deleteKey(id: String): Unit = withContext(Dispatchers.IO) {
        val currentKeys = getAllKeys().toMutableList()
        val itemToDelete = currentKeys.firstOrNull { it.id == id } ?: return@withContext
        currentKeys.remove(itemToDelete)

        if (itemToDelete.isActive && currentKeys.isNotEmpty()) {
            val first = currentKeys[0].copy(isActive = true)
            currentKeys[0] = first
            val encrypted = keystoreManager.encrypt(first.apiKey)
            sharedPreferences.edit()
                .putString(KEY_ENCRYPTED_GEMINI_KEY, encrypted)
                .putString(KEY_ACTIVE_MODEL, first.preferredModel)
                .apply()
        } else if (currentKeys.isEmpty()) {
            sharedPreferences.edit()
                .remove(KEY_ENCRYPTED_GEMINI_KEY)
                .apply()
        }

        persistKeys(currentKeys)
    }

    override suspend fun setActiveKey(id: String): Unit = withContext(Dispatchers.IO) {
        val currentKeys = getAllKeys()
        val updatedList = currentKeys.map {
            if (it.id == id) {
                // Set active and sync active model
                sharedPreferences.edit()
                    .putString(KEY_ACTIVE_MODEL, it.preferredModel)
                    .apply()
                val encrypted = keystoreManager.encrypt(it.apiKey)
                sharedPreferences.edit()
                    .putString(KEY_ENCRYPTED_GEMINI_KEY, encrypted)
                    .apply()
                it.copy(isActive = true)
            } else {
                it.copy(isActive = false)
            }
        }
        persistKeys(updatedList)
    }

    override suspend fun getActiveKeyItem(): ApiKeyItem? = withContext(Dispatchers.IO) {
        val keys = getAllKeys()
        keys.firstOrNull { it.isActive } ?: keys.firstOrNull()
    }

    override suspend fun getActiveModel(): String = withContext(Dispatchers.IO) {
        val activeItem = getActiveKeyItem()
        if (activeItem != null && activeItem.preferredModel.isNotBlank()) {
            return@withContext activeItem.preferredModel
        }
        sharedPreferences.getString(KEY_ACTIVE_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
    }

    override suspend fun setActiveModel(model: String): Unit = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .putString(KEY_ACTIVE_MODEL, model)
            .apply()

        // Also update active key's preferred model if present
        val currentKeys = getAllKeys()
        val active = currentKeys.firstOrNull { it.isActive }
        if (active != null) {
            val updated = currentKeys.map {
                if (it.id == active.id) it.copy(preferredModel = model) else it
            }
            persistKeys(updated)
        }
    }

    private fun persistKeys(keys: List<ApiKeyItem>) {
        val jsonArray = JSONArray()
        for (item in keys) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("label", item.label)
                put("apiKey", item.apiKey)
                put("preferredModel", item.preferredModel)
                put("isActive", item.isActive)
                put("createdAt", item.createdAt)
            }
            jsonArray.put(obj)
        }
        val rawJson = jsonArray.toString()
        val encrypted = keystoreManager.encrypt(rawJson)
        sharedPreferences.edit()
            .putString(KEY_ENCRYPTED_KEYS_JSON, encrypted)
            .apply()
    }
}
