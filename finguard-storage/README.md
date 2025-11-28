# FinGuard Storage Module

Purpose: encrypted storage wrappers for preferences and files.

Install:
```kotlin
implementation("io.github.alisidhu.finguard:core:1.0.0")
implementation("io.github.alisidhu.finguard:storage:1.0.0")
```

Usage:
```kotlin
FinGuard.init(SecurityConfig.Default)
FinGuard.storage().save("token", tokenBytes)
```

Notes:
- Requires `finguard-core`.
- Throws `FinGuardException` if the module is missing or disabled.
- Stub implementation uses in-memory storage; replace with EncryptedSharedPreferences and encrypted file store.
