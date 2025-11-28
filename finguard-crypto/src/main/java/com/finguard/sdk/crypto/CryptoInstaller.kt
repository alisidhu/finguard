package com.finguard.sdk.crypto

import com.finguard.sdk.core.CryptoService
import com.finguard.sdk.core.FinGuardBuilder
import com.finguard.sdk.core.LoggingConfig

private const val DEFAULT_KEY_ALIAS = "FinGuard.AES.Main"

/**
 * Public factory to build the production-grade [CryptoService] without exposing internal classes.
 * Java/Kotlin friendly: call `CryptoInstaller.create(...)`.
 */
object CryptoInstaller {
    @JvmStatic
    @JvmOverloads
    fun create(
        config: CryptoConfig = CryptoConfig(keyAlias = DEFAULT_KEY_ALIAS),
        logging: LoggingConfig = LoggingConfig(),
        requireStrongBox: Boolean = false,
    ): CryptoService {
        val keystore = KeystoreManager(requireStrongBox = requireStrongBox, keySize = config.keySize)
        val aesManager = AESManager(keyResolver = keystore, config = config)
        return CryptoServiceImpl(aesManager, logging, config)
    }
}

/**
 * Registers the production crypto service with the FinGuardBuilder.
 * Keeps backward compatibility for Kotlin/Java builder usage.
 */
fun FinGuardBuilder.withCrypto(
    keyAlias: String = DEFAULT_KEY_ALIAS,
    requireStrongBox: Boolean = false,
    keySize: Int = 256,
    pbkdfIterations: Int = 120_000,
): FinGuardBuilder =
    withService(CryptoService::class.java) { securityConfig ->
        val cryptoConfig =
            CryptoConfig(
                keyAlias = keyAlias,
                keySize = keySize,
                pbkdfIterations = pbkdfIterations,
            )
        CryptoInstaller.create(
            config = cryptoConfig,
            logging = securityConfig.logging,
            requireStrongBox = requireStrongBox,
        )
    }
