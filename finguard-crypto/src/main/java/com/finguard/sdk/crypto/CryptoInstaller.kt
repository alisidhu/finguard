package com.finguard.sdk.crypto

import com.finguard.sdk.core.CryptoService
import com.finguard.sdk.core.FinGuardBuilder

private const val DEFAULT_KEY_ALIAS = "FinGuard.AES.Main"

/**
 * Registers the production crypto service with the FinGuardBuilder.
 */
fun FinGuardBuilder.withCrypto(
    keyAlias: String = DEFAULT_KEY_ALIAS,
    requireStrongBox: Boolean = false,
    keySize: Int = 256,
    pbkdfIterations: Int = 120_000,
): FinGuardBuilder =
    withService(CryptoService::class.java) { config ->
        val cryptoConfig =
            CryptoConfig(
                keyAlias = keyAlias,
                keySize = keySize,
                pbkdfIterations = pbkdfIterations,
            )
        val keystore = KeystoreManager(requireStrongBox = requireStrongBox, keySize = keySize)
        val aesManager = AESManager(keyResolver = keystore, config = cryptoConfig)
        CryptoServiceImpl(aesManager, config.logging, cryptoConfig)
    }
