package com.finguard.sdk.crypto

import android.util.Base64
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val IV_LENGTH_BYTES = 12
private const val TAG_LENGTH_BITS = 128
private const val PAYLOAD_VERSION: Byte = 1

internal class AESManager(
    private val keyResolver: KeyResolver,
    private val random: SecureRandom = SecureRandom(),
) {
    fun encrypt(
        plain: ByteArray,
        associatedData: ByteArray? = null,
    ): ByteArray {
        if (plain.isEmpty()) throw EncryptionFailedException("Empty payload not allowed")
        val secretKey =
            try {
                keyResolver.getOrCreate()
            } catch (ex: Exception) {
                throw KeyUnavailableException("Encryption key unavailable", ex)
            }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv ?: throw EncryptionFailedException("Cipher did not provide IV")
        if (iv.size != IV_LENGTH_BYTES) throw EncryptionFailedException("Unexpected IV length ${iv.size}")
        associatedData?.let { cipher.updateAAD(it) }
        val cipherText =
            try {
                cipher.doFinal(plain)
            } catch (ex: Exception) {
                throw EncryptionFailedException("AES-GCM encryption failed", ex)
            }
        val packed = pack(PAYLOAD_VERSION, iv, cipherText)
        return Base64.encode(packed, Base64.NO_WRAP)
    }

    fun decrypt(
        payload: ByteArray,
        associatedData: ByteArray? = null,
    ): ByteArray {
        if (payload.isEmpty()) throw DecryptionFailedException("Empty payload")
        val decoded =
            try {
                Base64.decode(payload, Base64.NO_WRAP)
            } catch (ex: IllegalArgumentException) {
                throw DecryptionFailedException("Invalid base64 payload", ex)
            }

        val (version, iv, cipherText) = unpack(decoded)
        if (version != PAYLOAD_VERSION) throw DecryptionFailedException("Unsupported payload version $version")

        val secretKey =
            try {
                keyResolver.getOrCreate()
            } catch (ex: Exception) {
                throw KeyUnavailableException("Decryption key unavailable", ex)
            }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            associatedData?.let { cipher.updateAAD(it) }
            return cipher.doFinal(cipherText)
        } catch (ex: Exception) {
            throw DecryptionFailedException("AES-GCM decryption failed", ex)
        }
    }

    fun rotateKey(): SecretKey = keyResolver.rotate()

    fun deriveKeyFromPassword(
        password: CharArray,
        salt: ByteArray,
        iterations: Int = 150_000,
        keyLengthBits: Int = 256,
    ): SecretKey {
        require(password.isNotEmpty()) { "Password must not be empty" }
        require(salt.size >= 16) { "Salt must be at least 128 bits" }
        val keySpec = PBEKeySpec(password, salt, iterations, keyLengthBits)
        return try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val keyBytes = factory.generateSecret(keySpec).encoded
            SecretKeySpec(keyBytes, "AES")
        } catch (ex: Exception) {
            throw KeyUnavailableException("Unable to derive key", ex)
        } finally {
            keySpec.clearPassword()
        }
    }

    fun generateSalt(lengthBytes: Int = 16): ByteArray {
        if (lengthBytes < 16) throw IllegalArgumentException("Salt too short")
        return ByteArray(lengthBytes).also { random.nextBytes(it) }
    }

    private fun pack(
        version: Byte,
        iv: ByteArray,
        cipherText: ByteArray,
    ): ByteArray {
        val buffer = ByteBuffer.allocate(1 + 1 + iv.size + cipherText.size)
        buffer.put(version)
        buffer.put(iv.size.toByte())
        buffer.put(iv)
        buffer.put(cipherText)
        return buffer.array()
    }

    private fun unpack(input: ByteArray): Triple<Byte, ByteArray, ByteArray> {
        if (input.size <= 2 + IV_LENGTH_BYTES) throw DecryptionFailedException("Payload too small")
        val buffer = ByteBuffer.wrap(input)
        val version = buffer.get()
        val ivLength = buffer.get().toInt()
        if (ivLength != IV_LENGTH_BYTES) throw DecryptionFailedException("Invalid IV length $ivLength")
        val iv = ByteArray(ivLength)
        buffer.get(iv)
        val cipherText = ByteArray(buffer.remaining())
        buffer.get(cipherText)
        if (cipherText.isEmpty()) throw DecryptionFailedException("Ciphertext missing")
        return Triple(version, iv, cipherText)
    }
}
