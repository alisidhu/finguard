# FinGuard Auth Module

Purpose: biometric + session abstraction for secure authentication.

Install:
```kotlin
implementation("io.github.alisidhu.finguard:core:1.0.0")
implementation("io.github.alisidhu.finguard:auth:1.0.0")
```

Usage:
```kotlin
FinGuard.init(SecurityConfig.Default.copy(featureFlags = FeatureFlags(authentication = true)))
FinGuard.auth().authenticate("Confirm payment")
```

Notes:
- Requires `finguard-core`.
- Controlled by the `authentication` feature flag.
- Stub implementation always succeeds; replace with BiometricPrompt + session state.
