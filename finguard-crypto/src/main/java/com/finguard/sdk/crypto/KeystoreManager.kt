package com.finguard.sdk.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

internal interface KeyResolver {
    fun getOrCreate(): SecretKey

    fun rotate(): SecretKey
}

internal class KeystoreManager(
    private val keyAlias: String,
    private val requireStrongBox: Boolean = false,
) : KeyResolver {
    private val keyStore: KeyStore =
        KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }

    override fun getOrCreate(): SecretKey {
        val existing = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
        if (existing != null) return existing.secretKey
        return generateKey()
    }

    override fun rotate(): SecretKey {
        deleteIfExists()
        return generateKey()
    }

    fun isHardwareBacked(): Boolean {
        val entry = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry ?: return false
        val keyInfo =
            entry.secretKey?.let { secretKey ->
                val factory = javax.crypto.SecretKeyFactory.getInstance(secretKey.algorithm, ANDROID_KEY_STORE)
                factory.getKeySpec(secretKey, KeyInfo::class.java)
            } as? KeyInfo
        return keyInfo?.isInsideSecureHardware == true
    }

    private fun generateKey(): SecretKey {
        try {
            val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
            val builder =
                KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
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

    private fun deleteIfExists() {
        runCatching { keyStore.deleteEntry(keyAlias) }
    }

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    }
}
