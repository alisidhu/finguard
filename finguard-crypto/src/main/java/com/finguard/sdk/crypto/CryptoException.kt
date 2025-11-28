package com.finguard.sdk.crypto

sealed class CryptoException(message: String, cause: Throwable? = null) : Exception(message, cause)

class KeyUnavailableException(message: String, cause: Throwable? = null) : CryptoException(message, cause)

class EncryptionFailedException(message: String, cause: Throwable? = null) : CryptoException(message, cause)

class DecryptionFailedException(message: String, cause: Throwable? = null) : CryptoException(message, cause)
