package com.finguard.sdk.auth

import com.finguard.sdk.core.AuthService
import com.finguard.sdk.core.CryptoService
import com.finguard.sdk.core.FinGuardBuilder
import com.finguard.sdk.core.LoggingConfig
import com.finguard.sdk.core.StorageService

object AuthInstaller {
    @JvmStatic
    @JvmOverloads
    fun create(
        crypto: CryptoService,
        storage: StorageService,
        policies: AuthPolicies = AuthPolicies(),
        logging: LoggingConfig = LoggingConfig(),
        timeProvider: TimeProvider = SystemTimeProvider,
        listener: AuthEventListener = AuthEventListener.NONE,
    ): AuthService {
        val serializer = SessionSerializer(crypto)
        val vault = CredentialVault(storage, serializer)
        val policyEngine = AuthPolicyEngine(policies, timeProvider)
        val tokenManager = TokenManager(policies, timeProvider)
        return AuthServiceImpl(
            tokenManager = tokenManager,
            vault = vault,
            policyEngine = policyEngine,
            timeProvider = timeProvider,
            logging = logging,
            listener = listener,
        )
    }
}

fun FinGuardBuilder.withAuth(
    crypto: CryptoService,
    storage: StorageService,
    policies: AuthPolicies = AuthPolicies(),
    listener: AuthEventListener = AuthEventListener.NONE,
): FinGuardBuilder =
    withService(AuthService::class.java) { securityConfig ->
        AuthInstaller.create(
            crypto = crypto,
            storage = storage,
            policies = policies,
            logging = securityConfig.logging,
            listener = listener,
        )
    }
