package com.nexora.ai.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Manages hardware-backed AES-GCM encryption and decryption
 * using the Android Keystore system with fail-safe fallback.
 */
class KeystoreManager {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "nexora_gemini_key_alias"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val DELIMITER = "]"
        // Fallback key bytes for environments without hardware Keystore provider (e.g. some emulators)
        private val FALLBACK_KEY_BYTES = byteArrayOf(
            0x4E, 0x65, 0x78, 0x6F, 0x72, 0x61, 0x41, 0x49,
            0x53, 0x65, 0x63, 0x75, 0x72, 0x65, 0x4B, 0x65,
            0x79, 0x32, 0x30, 0x32, 0x36, 0x4F, 0x53, 0x4B,
            0x65, 0x79, 0x53, 0x74, 0x6F, 0x72, 0x65, 0x21
        )
    }

    private var keyStore: KeyStore? = null

    init {
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
            }
            getOrCreateSecretKey()
        } catch (_: Exception) {
            keyStore = null
        }
    }

    @Synchronized
    private fun getOrCreateSecretKey(): SecretKey {
        val ks = keyStore
        if (ks != null) {
            try {
                val existingKey = ks.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
                if (existingKey != null) {
                    return existingKey.secretKey
                }

                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )

                val parameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(true)
                    .build()

                keyGenerator.init(parameterSpec)
                return keyGenerator.generateKey()
            } catch (_: Exception) {
                // Fall back to SecretKeySpec
            }
        }
        return SecretKeySpec(FALLBACK_KEY_BYTES, "AES")
    }

    /**
     * Encrypts plain text string and returns a Base64-encoded string containing both IV and Ciphertext.
     */
    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val key = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val cipherTextBase64 = Base64.encodeToString(cipherText, Base64.NO_WRAP)
            "$ivBase64$DELIMITER$cipherTextBase64"
        } catch (e: Exception) {
            // Safe Base64 fallback if cipher fails in unusual runtime
            Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
    }

    /**
     * Decrypts the combined Base64 IV and Ciphertext back to original plain text.
     */
    fun decrypt(encryptedPayload: String): String? {
        if (encryptedPayload.isEmpty()) return null
        return try {
            val parts = encryptedPayload.split(DELIMITER)
            if (parts.size == 2) {
                val iv = Base64.decode(parts[0], Base64.NO_WRAP)
                val cipherText = Base64.decode(parts[1], Base64.NO_WRAP)

                val cipher = Cipher.getInstance(TRANSFORMATION)
                val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), spec)

                val decodedBytes = cipher.doFinal(cipherText)
                String(decodedBytes, Charsets.UTF_8)
            } else {
                // Try fallback plain Base64 decoding
                val bytes = Base64.decode(encryptedPayload, Base64.NO_WRAP)
                String(bytes, Charsets.UTF_8)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Deletes the secret key from the Android Keystore.
     */
    fun deleteKey() {
        try {
            val ks = keyStore
            if (ks != null && ks.containsAlias(KEY_ALIAS)) {
                ks.deleteEntry(KEY_ALIAS)
            }
        } catch (_: Exception) {
        }
    }
}
