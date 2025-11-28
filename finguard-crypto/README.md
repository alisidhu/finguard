# FinGuard Crypto Module

Purpose: hardware-backed key generation and symmetric crypto for tokens, secrets, and secure random.

Install:
```kotlin
implementation("io.github.alisidhu.finguard:core:1.0.0")
implementation("io.github.alisidhu.finguard:crypto:1.0.0")
```

Usage (Square/Stripe-style builder):
```kotlin
val client = FinGuardBuilder()
    .config { environment(EnvironmentMode.RELEASE) }
    .withCrypto(requireStrongBox = false) // Keystore AES-GCM with hardware if available
    .build()

val cipherText = client.crypto().encrypt("secret-token".toByteArray())
val plainText = client.crypto().decrypt(cipherText)
```

Security characteristics:
- AES-GCM with 256-bit keys
- Keys stored in Android Keystore (hardware-backed when available)
- Fresh 12-byte IV per encryption, randomized via `SecureRandom`
- Authenticated encryption (128-bit tag)
- Minimal logging; no sensitive data emitted

Advanced:
- Key rotation: `client.crypto()` is backed by `AESManager.rotateKey()`
- PBKDF2 (optional challenge mode): `AESManager.deriveKeyFromPassword` with salt and strong iteration count
- Strict input validation; malformed or tampered payloads throw `DecryptionFailedException`
