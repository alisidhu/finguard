package com.finguard.sdk.core

/**
 * FinGuardClient is the single entry point for all SDK services.
 * It is immutable after build and safe to share across threads.
 */
class FinGuardClient internal constructor(
    val config: SecurityConfig,
    private val services: Map<Class<*>, Any>,
) {
    fun crypto(): CryptoService = resolve(CryptoService::class.java, "crypto")

    fun storage(): StorageService = resolve(StorageService::class.java, "storage")

    fun network(): NetworkService = resolve(NetworkService::class.java, "network")

    fun auth(): AuthService = resolve(AuthService::class.java, "auth")

    fun device(): DeviceService = resolve(DeviceService::class.java, "device")

    fun secureLogger(): SecureLoggerService = resolve(SecureLoggerService::class.java, "logging")

    private fun <T> resolve(
        clazz: Class<T>,
        moduleName: String,
    ): T {
        val instance = services[clazz]?.let { clazz.cast(it) }
        return instance ?: throw FinGuardException(FinGuardError.ModuleNotInstalled(moduleName))
    }
}
