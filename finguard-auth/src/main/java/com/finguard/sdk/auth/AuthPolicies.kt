package com.finguard.sdk.auth

import com.finguard.sdk.core.AuthCredentials
import com.finguard.sdk.core.AuthSession
import com.finguard.sdk.core.Base64Compat
import java.security.SecureRandom
import kotlin.math.max

interface TokenProvider {
    fun issue(credentials: AuthCredentials): AuthSession

    fun refresh(session: AuthSession): AuthSession
}

data class AuthPolicies(
    val accessTokenTtlMillis: Long = 15 * 60 * 1000L,
    val refreshTokenTtlMillis: Long = 30L * 24 * 60 * 60 * 1000,
    val maxSessionAgeMillis: Long = 30L * 24 * 60 * 60 * 1000,
    val refreshWindowMillis: Long = 5 * 60 * 1000L,
    val forceLogoutAfterMillis: Long = 30L * 24 * 60 * 60 * 1000,
    val requireRotation: Boolean = true,
    val allowMultiSession: Boolean = false,
    val tokenProvider: TokenProvider = DefaultTokenProvider(),
)

class DefaultTokenProvider(
    private val random: SecureRandom = SecureRandom(),
) : TokenProvider {
    override fun issue(credentials: AuthCredentials): AuthSession =
        when (credentials) {
            is AuthCredentials.TokenCredentials ->
                buildSession(
                    userId = credentials.userId,
                    issuer = credentials.issuer,
                    accessToken = credentials.accessToken,
                    refreshToken = credentials.refreshToken,
                    expiresAt = credentials.accessTokenExpiresAt,
                )
            is AuthCredentials.PasswordCredentials -> {
                credentials.password.fill('\u0000')
                throw AuthException("Password credentials require a server-backed TokenProvider")
            }
            is AuthCredentials.BiometricCredentials -> throw AuthException("Biometric credentials require a server-backed TokenProvider")
        }

    override fun refresh(session: AuthSession): AuthSession {
        val newAccess = token()
        val newRefresh = session.refreshToken?.let { token() }
        val now = System.currentTimeMillis()
        return session.copy(
            accessToken = newAccess,
            refreshToken = newRefresh,
            createdAt = now,
            expiresAt = now + max(1, session.expiresAt - session.createdAt),
        )
    }

    private fun buildSession(
        userId: String,
        issuer: String,
        accessToken: String,
        refreshToken: String?,
        expiresAt: Long?,
    ): AuthSession {
        val now = System.currentTimeMillis()
        val expiry = expiresAt ?: now + 15 * 60 * 1000L
        return AuthSession(
            userId = userId,
            accessToken = accessToken,
            refreshToken = refreshToken,
            createdAt = now,
            expiresAt = expiry,
            issuer = issuer,
        )
    }

    private fun token(): String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64Compat.encodeUrlSafe(bytes)
    }
}
