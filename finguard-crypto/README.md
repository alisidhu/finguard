# FinGuard Crypto Module

Hardware-backed symmetric crypto for fintech/banking apps: AES-GCM, Keystore key management, PBKDF2, and strict payload validation.

## Features
- **Android Keystore AES keys**: 256-bit keys, hardware-backed/StrongBox when available, per-alias management.
- **AES-GCM encryption**: 12-byte IV per operation, 128-bit auth tag, IV embedded in payload.
- **Base64 payloads**: versioned payload packing with IV + ciphertext.
- **Key lifecycle**: alias-based get/create, delete, rotation.
- **PBKDF2 (optional)**: `PBKDF2WithHmacSHA256`, configurable iterations (default 120k), salt length ≥ 16 bytes.
- **Tamper detection**: invalid/tampered payloads throw `DecryptionFailedException`.
- **Thread-safe keystore ops**: synchronized key create/delete.
- **Configurable defaults**: `CryptoConfig` for alias, key size, PBKDF2 iterations.

## Installation
```kotlin
implementation("io.github.alisidhu.finguard:core:1.0.0")
implementation("io.github.alisidhu.finguard:crypto:1.0.0")
```

## Usage (Kotlin — builder style)
```kotlin
val client = FinGuardBuilder()
    .config { environment(EnvironmentMode.RELEASE) }
    .withCrypto(
        keyAlias = "finguard-main",
        requireStrongBox = false, // enable if you require StrongBox devices
        keySize = 256,
        pbkdfIterations = 120_000
    )
    .build()

val cipherText = client.crypto().encrypt("secret-token".toByteArray())
val plainText = client.crypto().decrypt(cipherText)
```

## Usage (factory — Kotlin or Java friendly)
```kotlin
val crypto = CryptoInstaller.create(
    config = CryptoConfig(
        keyAlias = "finguard-main",
        keySize = 256,
        pbkdfIterations = 150_000,
    ),
    logging = LoggingConfig(level = LogLevel.INFO),
    requireStrongBox = false,
)

val cipher = crypto.encrypt("secret-token".toByteArray())
val plain = crypto.decrypt(cipher)
```

### Java sample
```java
CryptoService crypto = CryptoInstaller.create(
    new CryptoConfig("finguard-main", 256, 150_000),
    new LoggingConfig(LogLevel.INFO, false, new FinGuardLogger.Console(LogLevel.INFO)),
    false
);

byte[] cipher = crypto.encrypt("secret-token".getBytes(StandardCharsets.UTF_8));
byte[] plain = crypto.decrypt(cipher);
```

## API at a glance
- `CryptoService.encrypt(plain: ByteArray): ByteArray`
- `CryptoService.decrypt(payload: ByteArray): ByteArray`

Internals:
- `CryptoConfig` — alias, key size, PBKDF2 iterations.
- `KeystoreManager` — implements `KeyResolver`, alias-based get/create/delete/exists, StrongBox optional.
- `AESManager` — AES-GCM, payload packing/unpacking, PBKDF2 helpers, salt generation.
- `CryptoPayload` — versioned IV + ciphertext structure.
- `Base64Compat` — Android/JVM-safe Base64.
- Exceptions: `KeyUnavailableException`, `EncryptionFailedException`, `DecryptionFailedException`.

## Key management
- Keys live in Android Keystore; not exportable.
- Per-alias operations: create, exists, delete, rotate.
- StrongBox: opt-in via `requireStrongBox`; falls back if unavailable.
- Hardware-backed check: `KeystoreManager.isHardwareBacked(alias)`.

## Security characteristics
- AES-GCM, 256-bit keys, 12-byte IV per encrypt.
- Authenticated encryption; tamper → `DecryptionFailedException`.
- No plaintext secrets or keys logged; logging gated by core `LoggingConfig`.
- PBKDF2 only via explicit call; never the default encryption path.

## Testing
- JVM unit tests use an in-memory `KeyResolver` to avoid device keystore.
- Base64Compat switches to java.util.Base64 on JVM to keep tests stable.

## Misuse warnings
- Do not reuse payloads or IVs; always use `encrypt` per message.
- Do not hardcode secrets; rely on Keystore-managed keys.
- Do not lower PBKDF iterations or salt length without clear justification.
