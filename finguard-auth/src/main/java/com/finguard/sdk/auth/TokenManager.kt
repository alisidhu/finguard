package com.finguard.sdk.auth

import com.finguard.sdk.core.AuthCredentials
import com.finguard.sdk.core.AuthSession
import java.security.MessageDigest

internal class TokenManager(
    private val policies: AuthPolicies,
    private val timeProvider: TimeProvider,
) {
    fun establish(credentials: AuthCredentials): AuthSession {
        val session = policies.tokenProvider.issue(credentials)
        validate(session)
        return normalizeExpiry(session)
    }

    fun refresh(current: AuthSession): AuthSession {
        val refreshed = policies.tokenProvider.refresh(current)
        validate(refreshed)
        if (policies.requireRotation && current.refreshToken != null) {
            val previous = digest(current.refreshToken)
            val next = refreshed.refreshToken?.let { digest(it) } ?: previous
            if (MessageDigest.isEqual(previous, next)) {
                throw AuthException("Refresh token rotation required")
            }
        }
        return normalizeExpiry(refreshed)
    }

    fun digest(token: String?): ByteArray? = token?.toByteArray(Charsets.UTF_8)?.let { sha256(it) }

    private fun normalizeExpiry(session: AuthSession): AuthSession {
        val now = timeProvider.now()
        val enforcedExpiry = now + policies.accessTokenTtlMillis
        val expires =
            when {
                session.expiresAt <= now -> enforcedExpiry
                session.expiresAt > enforcedExpiry -> enforcedExpiry
                else -> session.expiresAt
            }
        return session.copy(
            createdAt = session.createdAt.takeIf { it > 0 } ?: now,
            expiresAt = expires,
        )
    }

    private fun validate(session: AuthSession) {
        val now = timeProvider.now()
        if (session.userId.isBlank()) throw AuthException("Invalid user id")
        if (session.accessToken.isBlank()) throw AuthException("Access token missing")
        if (session.expiresAt <= now) throw AuthException("Access token already expired")
    }

    private fun sha256(data: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(data)
}
