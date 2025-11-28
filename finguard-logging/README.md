# FinGuard Logging Module

Purpose: secure, encrypted logging and audit trails.

Install:
```kotlin
implementation("io.github.alisidhu.finguard:core:1.0.0")
implementation("io.github.alisidhu.finguard:logging:1.0.0")
```

Usage:
```kotlin
FinGuard.init(SecurityConfig.Default.copy(featureFlags = FeatureFlags(secureLogging = true)))
FinGuard.secureLogger().logSecure("user_login", mapOf("user" to "123"))
```

Notes:
- Requires `finguard-core`.
- Controlled by the `secureLogging` feature flag.
- Stub implementation writes to stdout; replace with encrypted, size-bounded logs.
