package com.finguard.sdk.core

interface CryptoService {
    fun encrypt(input: ByteArray): ByteArray

    fun decrypt(input: ByteArray): ByteArray
}

interface StorageService {
    fun save(
        key: String,
        value: ByteArray,
    )

    fun load(key: String): ByteArray?

    fun clear(key: String)
}

interface NetworkService {
    fun secureRequest(
        endpoint: String,
        payload: ByteArray,
    ): NetworkResult
}

data class NetworkResult(val statusCode: Int, val body: ByteArray)

interface AuthService {
    fun authenticate(reason: String? = null): AuthResult
}

sealed class AuthResult {
    data object Success : AuthResult()

    data class Failure(val reason: String) : AuthResult()
}

interface DeviceService {
    fun assessIntegrity(): DeviceIntegrity
}

data class DeviceIntegrity(
    val isTrusted: Boolean,
    val issues: List<String> = emptyList(),
)

interface SecureLoggerService {
    fun logSecure(
        event: String,
        attributes: Map<String, String> = emptyMap(),
    )
}
