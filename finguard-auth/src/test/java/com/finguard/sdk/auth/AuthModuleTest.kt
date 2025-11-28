package com.finguard.sdk.auth

import com.finguard.sdk.core.AuthCredentials
import com.finguard.sdk.core.AuthSession
import com.finguard.sdk.core.CryptoService
import com.finguard.sdk.core.StorageService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val KEY_SIZE = 256

class AuthModuleTest {
    private val time = FakeTimeProvider()
    private val storage = InMemoryStorage()
    private val crypto = FakeCryptoService()

    @Test
    fun `session persisted encrypted`() {
        val service =
            AuthInstaller.create(
                crypto = crypto,
                storage = storage,
                policies = AuthPolicies(tokenProvider = rotatingProvider()),
                timeProvider = time,
            )

        val session = service.login(tokenCreds("u1"))
        assertNotNull(session)

        val raw = storage.load("auth.session")
        assertNotNull(raw)
        val storedString = String(raw!!, Charsets.UTF_8)
        assertTrue("Ciphertext should not expose user id", !storedString.contains("u1"))

        val restored = service.session()
        assertEquals("u1", restored?.userId)
    }

    @Test
    fun `expired session is cleared`() {
        val policies =
            AuthPolicies(
                accessTokenTtlMillis = 1_000,
                refreshTokenTtlMillis = 10_000,
                maxSessionAgeMillis = 10_000,
                tokenProvider = rotatingProvider(),
            )
        val service =
            AuthInstaller.create(
                crypto = crypto,
                storage = storage,
                policies = policies,
                timeProvider = time,
            )

        service.login(tokenCreds("u2"))
        time.advance(2_000)

        assertNull(service.session())
    }

    @Test
    fun `refresh rotates token`() {
        val provider = rotatingProvider()
        val service =
            AuthInstaller.create(
                crypto = crypto,
                storage = storage,
                policies = AuthPolicies(tokenProvider = provider),
                timeProvider = time,
            )

        val initial = service.login(tokenCreds("u3"))
        val refreshed = service.refresh()

        assertNotEquals(initial.accessToken, refreshed.accessToken)
        assertNotEquals(initial.refreshToken, refreshed.refreshToken)
    }

    @Test(expected = AuthException::class)
    fun `replay refresh token is rejected`() {
        val provider =
            object : TokenProvider {
                private var counter = 0

                override fun issue(credentials: AuthCredentials): AuthSession =
                    (credentials as AuthCredentials.TokenCredentials).toSession()

                override fun refresh(session: AuthSession): AuthSession {
                    counter++
                    val refreshToken = if (counter == 1) "r1" else "r1" // replay same refresh token
                    val access = "a$counter"
                    val now = System.currentTimeMillis()
                    return session.copy(
                        accessToken = access,
                        refreshToken = refreshToken,
                        createdAt = now,
                        expiresAt = now + 10_000,
                    )
                }
            }
        val service =
            AuthInstaller.create(
                crypto = crypto,
                storage = storage,
                policies = AuthPolicies(tokenProvider = provider),
                timeProvider = time,
            )

        service.login(tokenCreds("u4", refresh = "r1"))
        service.refresh() // first refresh ok
        service.refresh() // replay should throw
    }

    @Test
    fun `logout wipes vault`() {
        val service =
            AuthInstaller.create(
                crypto = crypto,
                storage = storage,
                policies = AuthPolicies(tokenProvider = rotatingProvider()),
                timeProvider = time,
            )

        service.login(tokenCreds("u5"))
        service.logout()

        assertNull(storage.load("auth.session"))
    }

    private fun AuthCredentials.TokenCredentials.toSession(): AuthSession {
        val now = System.currentTimeMillis()
        val expiry = accessTokenExpiresAt ?: now + 10_000
        return AuthSession(
            userId = userId,
            accessToken = accessToken,
            refreshToken = refreshToken,
            createdAt = now,
            expiresAt = expiry,
            issuer = issuer,
        )
    }

    private fun tokenCreds(
        userId: String,
        refresh: String = "refresh-token",
    ): AuthCredentials.TokenCredentials =
        AuthCredentials.TokenCredentials(
            userId = userId,
            accessToken = "access-$userId",
            refreshToken = refresh,
            issuer = "issuer",
        )

    private fun rotatingProvider(): TokenProvider =
        object : TokenProvider {
            private var counter = 0

            override fun issue(credentials: AuthCredentials): AuthSession = (credentials as AuthCredentials.TokenCredentials).toSession()

            override fun refresh(session: AuthSession): AuthSession {
                counter++
                val now = System.currentTimeMillis()
                return session.copy(
                    accessToken = "access-$counter",
                    refreshToken = "refresh-$counter",
                    createdAt = now,
                    expiresAt = now + 10_000,
                )
            }
        }
}

private class InMemoryStorage : StorageService {
    private val data = mutableMapOf<String, ByteArray>()

    override fun save(
        key: String,
        value: ByteArray,
    ) {
        data[key] = value
    }

    override fun load(key: String): ByteArray? = data[key]

    override fun clear(key: String) {
        data.remove(key)
    }
}

private class FakeCryptoService : CryptoService {
    private val key: SecretKey =
        KeyGenerator.getInstance("AES").apply { init(KEY_SIZE) }.generateKey()
    private val random = SecureRandom()

    override fun encrypt(input: ByteArray): ByteArray {
        val iv = ByteArray(12).also { random.nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(input)
        return iv + encrypted
    }

    override fun decrypt(input: ByteArray): ByteArray {
        require(input.size > 12)
        val iv = input.copyOfRange(0, 12)
        val cipherText = input.copyOfRange(12, input.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return cipher.doFinal(cipherText)
    }
}

private class FakeTimeProvider : TimeProvider {
    private var timeMs: Long = System.currentTimeMillis()

    override fun now(): Long = timeMs

    fun advance(delta: Long) {
        timeMs += delta
    }
}
