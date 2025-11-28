package com.finguard.sdk.auth

import com.finguard.sdk.auth.MessageDigestHelper.constantTimeEquals
import com.finguard.sdk.core.AuthCredentials
import com.finguard.sdk.core.AuthService
import com.finguard.sdk.core.AuthSession
import com.finguard.sdk.core.LogLevel
import com.finguard.sdk.core.LoggingConfig

internal class AuthServiceImpl(
    private val tokenManager: TokenManager,
    private val vault: CredentialVault,
    private val policyEngine: AuthPolicyEngine,
    private val timeProvider: TimeProvider,
    private val logging: LoggingConfig,
    private val listener: AuthEventListener,
) : AuthService {
    override fun login(credentials: AuthCredentials): AuthSession {
        val session = tokenManager.establish(credentials)
        policyEngine.validateNewSession(session)
        val digest = tokenManager.digest(session.refreshToken)
        vault.store(
            PersistedRecord(
                session = session,
                currentRefreshDigest = digest,
                previousRefreshDigest = null,
                lastRefreshAt = null,
                rotationCount = 0,
            ),
        )
        audit(LogLevel.INFO, "Auth login established for issuer ${session.issuer}")
        wipeIfPassword(credentials)
        return session
    }

    override fun logout() {
        vault.clear()
        audit(LogLevel.INFO, "Auth logout executed")
    }

    override fun refresh(): AuthSession {
        val record =
            sessionRecordOrNull()
                ?: throw AuthException("No active session")
        policyEngine.ensureRefreshAllowed(record.session, record.lastRefreshAt)

        val refreshed = tokenManager.refresh(record.session)
        val newDigest = tokenManager.digest(refreshed.refreshToken)
        val oldDigest = record.currentRefreshDigest
        if (oldDigest != null && newDigest != null && record.previousRefreshDigest != null) {
            if (constantTimeEquals(record.previousRefreshDigest, newDigest)) {
                throw AuthException("Replay detected on rotated token")
            }
        }

        vault.store(
            PersistedRecord(
                session = refreshed,
                currentRefreshDigest = newDigest,
                previousRefreshDigest = oldDigest,
                lastRefreshAt = timeProvider.now(),
                rotationCount = record.rotationCount + 1,
            ),
        )
        audit(LogLevel.INFO, "Auth refresh completed for issuer ${refreshed.issuer}")
        return refreshed
    }

    override fun session(): AuthSession? {
        return sessionRecordOrNull()?.session
    }

    override fun isAuthenticated(): Boolean = session() != null

    private fun sessionRecordOrNull(): PersistedRecord? {
        val record = vault.load() ?: return null
        val now = timeProvider.now()
        if (policyEngine.hardLogoutRequired(record.session, now)) {
            vault.clear()
            listener.onForcedLogout()
            return null
        }
        if (policyEngine.isExpired(record.session, now)) {
            vault.clear()
            listener.onSessionExpired()
            return null
        }
        return record
    }

    private fun wipeIfPassword(credentials: AuthCredentials) {
        if (credentials is AuthCredentials.PasswordCredentials) {
            credentials.password.fill('\u0000')
        }
    }

    private fun audit(
        level: LogLevel,
        message: String,
    ) {
        val logger = logging.logger
        if (logging.level.priority <= level.priority) {
            logger.log(level, message)
        }
    }
}
