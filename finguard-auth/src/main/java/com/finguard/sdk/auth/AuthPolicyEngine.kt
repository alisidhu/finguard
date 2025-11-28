package com.finguard.sdk.auth

import com.finguard.sdk.core.AuthSession

internal class AuthPolicyEngine(
    private val policies: AuthPolicies,
    private val timeProvider: TimeProvider,
) {
    fun validateNewSession(session: AuthSession) {
        val now = timeProvider.now()
        if (session.expiresAt - now > policies.maxSessionAgeMillis) {
            throw AuthException("Session exceeds maximum age")
        }
        if (session.expiresAt <= now) {
            throw AuthException("Session expired on creation")
        }
    }

    fun ensureSessionActive(session: AuthSession) {
        val now = timeProvider.now()
        if (isExpired(session, now) || hardLogoutRequired(session, now)) {
            throw AuthException("Session expired")
        }
    }

    fun ensureRefreshAllowed(
        session: AuthSession,
        lastRefreshAt: Long?,
    ) {
        val now = timeProvider.now()
        if (session.refreshToken.isNullOrBlank()) throw AuthException("Refresh token unavailable")
        val refreshExpiry = session.createdAt + policies.refreshTokenTtlMillis
        if (now >= refreshExpiry) throw AuthException("Refresh token expired")
        if (lastRefreshAt != null && now - lastRefreshAt < policies.refreshWindowMillis) {
            throw AuthException("Refresh attempted too frequently")
        }
    }

    fun hardLogoutRequired(
        session: AuthSession,
        now: Long = timeProvider.now(),
    ): Boolean {
        val age = now - session.createdAt
        return age >= policies.forceLogoutAfterMillis
    }

    fun isExpired(
        session: AuthSession,
        now: Long = timeProvider.now(),
    ): Boolean = now >= session.expiresAt
}
