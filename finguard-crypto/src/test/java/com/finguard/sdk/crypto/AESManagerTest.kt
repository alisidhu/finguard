package com.finguard.sdk.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

private class InMemoryKeyResolver : KeyResolver {
    private var key: SecretKey = SecretKeySpec(ByteArray(32) { it.toByte() }, "AES")

    override fun getOrCreateKey(alias: String): SecretKey = key

    override fun deleteKey(alias: String) {
        // no-op for in-memory
    }

    override fun keyExists(alias: String): Boolean = true

    fun rotate(): SecretKey {
        key = SecretKeySpec(ByteArray(32) { (it + 1).toByte() }, "AES")
        return key
    }
}

class AESManagerTest {
    private val resolver = InMemoryKeyResolver()
    private val aesManager = AESManager(resolver, CryptoConfig(keyAlias = "test-alias"))

    @Test
    fun `encrypt then decrypt returns original`() {
        val plain = "fin-guard-secret".toByteArray()
        val cipher = aesManager.encrypt(plain)
        val roundTrip = aesManager.decrypt(cipher)

        assertArrayEquals(plain, roundTrip)
    }

    @Test
    fun `tampered payload is rejected`() {
        val plain = "tamper-test".toByteArray()
        val cipher = aesManager.encrypt(plain)
        cipher[0] = cipher[0].inc()

        assertThrows(DecryptionFailedException::class.java) {
            aesManager.decrypt(cipher)
        }
    }
}
