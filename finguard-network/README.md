# FinGuard Network Module

Purpose: hardened HTTP client with TLS enforcement and certificate pinning.

Install:
```kotlin
implementation("io.github.alisidhu.finguard:core:1.0.0")
implementation("io.github.alisidhu.finguard:network:1.0.0")
```

Usage:
```kotlin
FinGuard.init(SecurityConfig.Default.copy(featureFlags = FeatureFlags(networkDefense = true)))
FinGuard.network().secureRequest("https://api.bank.com/secure", payload)
```

Notes:
- Requires `finguard-core`.
- Controlled by the `networkDefense` feature flag.
- Stub implementation just echoes payload; replace with OkHttp + TLS config + pinning.
