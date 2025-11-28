# FinGuard Device Module

Purpose: detect root, emulator, tampering, and debugging.

Install:
```kotlin
implementation("io.github.alisidhu.finguard:core:1.0.0")
implementation("io.github.alisidhu.finguard:device:1.0.0")
```

Usage:
```kotlin
FinGuard.init(SecurityConfig.Default.copy(featureFlags = FeatureFlags(deviceIntegrity = true)))
val integrity = FinGuard.device().assessIntegrity()
```

Notes:
- Requires `finguard-core`.
- Controlled by the `deviceIntegrity` feature flag.
- Stub implementation always trusts; replace with root/emulator/tamper/debugger checks.
