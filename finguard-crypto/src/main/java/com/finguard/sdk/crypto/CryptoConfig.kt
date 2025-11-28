package com.finguard.sdk.crypto

data class CryptoConfig(
    val keyAlias: String = "finguard-default",
    val keySize: Int = 256,
    val pbkdfIterations: Int = 120_000,
)
