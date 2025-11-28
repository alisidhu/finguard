package com.finguard.sdk.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

internal class KeystoreManager(
    private val requireStrongBox: Boolean = false,
    private val keySize: Int = 256,
) : KeyResolver {
    private val keyStore: KeyStore =
        KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
    private val lock = Any()

    override fun getOrCreateKey(alias: String): SecretKey =
        synchronized(lock) {
            val existing = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
            if (existing != null) return@synchronized existing.secretKey
            generateKey(alias)
        }

    override fun deleteKey(alias: String) {
        synchronized(lock) {
            runCatching { keyStore.deleteEntry(alias) }
        }
    }

    override fun keyExists(alias: String): Boolean =
        synchronized(lock) { keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry != null }

    fun rotate(alias: String): SecretKey {
        deleteKey(alias)
        return getOrCreateKey(alias)
    }

    fun isHardwareBacked(alias: String): Boolean {
        val entry = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry ?: return false
        val keyInfo =
            entry.secretKey?.let { secretKey ->
                val factory = javax.crypto.SecretKeyFactory.getInstance(secretKey.algorithm, ANDROID_KEY_STORE)
                factory.getKeySpec(secretKey, KeyInfo::class.java)
            } as? KeyInfo
        return keyInfo?.isInsideSecureHardware == true
    }

    private fun generateKey(alias: String): SecretKey {
        try {
            val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
            val builder =
                KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(keySize)
                    .setRandomizedEncryptionRequired(true)
                    .setUserAuthenticationRequired(false)

            if (requireStrongBox && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                builder.setIsStrongBoxBacked(true)
            }

            generator.init(builder.build())
            return generator.generateKey()
        } catch (ex: Exception) {
            throw KeyUnavailableException("Unable to generate keystore key", ex)
        }
    }

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    }
}
