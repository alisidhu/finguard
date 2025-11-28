# FinGuard Core

The FinGuard core module hosts the client, builder, configuration objects, logging, and error model. It is framework-agnostic and provides the service interfaces used by all feature modules.

## Key classes
- `FinGuardClient` — immutable entry point exposing services (crypto, storage, network, auth, device, logging).
- `FinGuardBuilder` — Square/Stripe-style builder; registers services without DI frameworks.
- `SecurityConfig` / `SecurityConfigBuilder` — runtime configuration, feature flags, logging, and environment controls.
- `FinGuard` — installs a single client instance per process.
- Service interfaces — `CryptoService`, `StorageService`, `NetworkService`, `AuthService`, `DeviceService`, `SecureLoggerService`.

## Usage
```kotlin
val client = FinGuardBuilder()
    .config {
        environment(EnvironmentMode.RELEASE)
        logging(level = LogLevel.INFO)
    }
    // modules register via extensions, e.g. withCrypto()
    .build()

FinGuard.install(client) // optional default singleton
```

## Design principles
- Framework-agnostic (no Hilt/Koin inside the SDK)
- Single global instance only for `FinGuardClient`
- Explicit service registration (no reflection-based auto-loading)
- Testable and SOLID-friendly API surface
