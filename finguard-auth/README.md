# FinGuard Auth Module

Enterprise-grade authentication and session management for fintech/banking Android apps. Uses FinGuard crypto for encryption-at-rest and FinGuard storage for persistence. No DI frameworks, no reflection, Java/Kotlin friendly.

## Capabilities
- Access + refresh token lifecycle with rotation enforcement
- Encrypted session vault (AES-GCM via `CryptoService`)
- Replay detection on rotated refresh tokens
- Expiry enforcement (access + refresh + max session age)
- Force-logout threshold + refresh frequency guard
- Policy-driven behavior (`AuthPolicies`)
- Builder factory: `AuthInstaller.create(...)`

## Installation
```kotlin
implementation("io.github.alisidhu.finguard:core:1.0.0")
implementation("io.github.alisidhu.finguard:crypto:1.0.0")
implementation("io.github.alisidhu.finguard:storage:1.0.0")
implementation("io.github.alisidhu.finguard:auth:1.0.0")
```

## Public API
```kotlin
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
    data class TokenCredentials(...)
    data class PasswordCredentials(...)
    data class BiometricCredentials(...)
}
```

## Usage (Kotlin)
```kotlin
val auth: AuthService = AuthInstaller.create(
    crypto = cryptoService,         // from FinGuard crypto
    storage = secureStorage,        // from FinGuard storage
    policies = AuthPolicies(),      // tune TTL/rotation/token provider
    logging = LoggingConfig(level = LogLevel.INFO),
)

val session = auth.login(
    AuthCredentials.TokenCredentials(
        userId = "user-123",
        accessToken = "jwt-access",
        refreshToken = "jwt-refresh",
        issuer = "bank.example",
        accessTokenExpiresAt = System.currentTimeMillis() + 15 * 60 * 1000,
    ),
)

val refreshed = auth.refresh()
auth.logout()
```

## Usage (Java)
```java
AuthService auth = AuthInstaller.create(
    cryptoService,
    storageService,
    new AuthPolicies(),
    new LoggingConfig(LogLevel.INFO, false, new FinGuardLogger.Console(LogLevel.INFO)),
    SystemTimeProvider
);

AuthSession session = auth.login(
    new AuthCredentials.TokenCredentials(
        "user-123",
        "jwt-access",
        "jwt-refresh",
        "bank.example",
        null
    )
);
```

## Security Guarantees
- Encrypted session persistence (AES-GCM via `CryptoService`)
- Refresh token rotation enforcement
- Replay detection for rotated tokens
- Strict expiry + max-session-age enforcement
- Forced logout after policy threshold
- No secrets logged; logging is level-gated
- Password credentials zeroed in memory after use

## Extensibility
- `AuthPolicies` to tune TTLs, rotation, force-logout rules
- `TokenProvider` to integrate with real backends for login/refresh
- `TimeProvider` for deterministic testing

## Known Limitations
- Token issuance/refresh depends on the provided `TokenProvider`; default provider only supports token-based credentials.
- Multi-session storage not provided; current vault stores a single active session per client instance.
