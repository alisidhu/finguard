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
    fun login(credentials: AuthCredentials): AuthSession

    fun logout()

    fun refresh(): AuthSession

    fun session(): AuthSession?

    fun isAuthenticated(): Boolean
}

data class AuthSession(
    val userId: String,
    val accessToken: String,
    val refreshToken: String?,
    val createdAt: Long,
    val expiresAt: Long,
    val issuer: String,
)

sealed class AuthCredentials {
    data class PasswordCredentials(
        val userId: String,
        val password: CharArray,
        val issuer: String,
        val accessToken: String,
        val refreshToken: String?,
        val accessTokenExpiresAt: Long? = null,
    ) : AuthCredentials() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PasswordCredentials

            if (accessTokenExpiresAt != other.accessTokenExpiresAt) return false
            if (userId != other.userId) return false
            if (!password.contentEquals(other.password)) return false
            if (issuer != other.issuer) return false
            if (accessToken != other.accessToken) return false
            if (refreshToken != other.refreshToken) return false

            return true
        }

        override fun hashCode(): Int {
            var result = accessTokenExpiresAt?.hashCode() ?: 0
            result = 31 * result + userId.hashCode()
            result = 31 * result + password.contentHashCode()
            result = 31 * result + issuer.hashCode()
            result = 31 * result + accessToken.hashCode()
            result = 31 * result + (refreshToken?.hashCode() ?: 0)
            return result
        }
    }

    data class TokenCredentials(
        val userId: String,
        val accessToken: String,
        val refreshToken: String?,
        val issuer: String,
        val accessTokenExpiresAt: Long? = null,
    ) : AuthCredentials()

    data class BiometricCredentials(
        val userId: String,
        val issuer: String,
        val proof: String,
        val accessToken: String,
        val refreshToken: String?,
        val accessTokenExpiresAt: Long? = null,
    ) : AuthCredentials()
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
