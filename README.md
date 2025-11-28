# FinGuard SDK

Enterprise-grade Android security SDK for fintech and banking applications.


# [![](https://jitpack.io/v/alisidhu/finguard.svg)](https://jitpack.io/#alisidhu/finguard)

---

## Why FinGuard?

FinGuard is designed to help Android developers build secure, production-ready financial applications using modern Android security standards.

---

## Core Features

- AES-GCM encryption
- Android Keystore integration
- Biometric authentication
- SSL certificate pinning
- Root and emulator detection
- Secure preference storage
- Token lifecycle management
- Encrypted application logging

## Modular install

Pick only the modules you need:

```kotlin
implementation("com.github.alisidhu:finguard:latest.release")

```

Quick start:

```kotlin
FinGuard.init(
    SecurityConfig.Default.copy(
        featureFlags =
            FeatureFlags(
                crypto = true,
                storage = true,
                networkDefense = true,
                authentication = false,
                deviceIntegrity = true,
                secureLogging = true
            )
    )
)

val encrypted = FinGuard.crypto().encrypt("secret".toByteArray())
```

---

## Status

🚧 Under active development  

---

## Author

**Muhammad Ali**  
FinTech Mobile Architecture & Security Specialist  
GitHub: https://github.com/alisidhu

---

## License

MIT
