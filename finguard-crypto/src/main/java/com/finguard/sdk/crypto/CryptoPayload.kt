package com.finguard.sdk.crypto

data class CryptoPayload(
    val iv: ByteArray,
    val cipherText: ByteArray,
    val version: Byte = 1,
)
