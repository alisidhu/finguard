package com.finguard.sdk.crypto

import javax.crypto.SecretKey

/**
 * Resolves symmetric keys for encryption. Implementations can back keys
 * by Android Keystore, hardware-backed stores, or test doubles.
 */
internal interface KeyResolver {
    fun getOrCreateKey(alias: String): SecretKey

    fun deleteKey(alias: String)

    fun keyExists(alias: String): Boolean
}
