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
): FinGuardBuilder =
    withService(CryptoService::class.java) { config ->
        val keystore = KeystoreManager(keyAlias = keyAlias, requireStrongBox = requireStrongBox)
        val aesManager = AESManager(keystore)
        CryptoServiceImpl(aesManager, config.logging)
    }
