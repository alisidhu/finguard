package com.finguard.sdk.core

/**
 * Builder used by FinGuardBuilder to assemble a SecurityConfig without exposing
 * consumers to internal defaults.
 */
class SecurityConfigBuilder {
    private var environmentMode: EnvironmentMode = EnvironmentMode.RELEASE
    private var strictChecksEnabled: Boolean = true
    private var allowDebugLogging: Boolean = false
    private var featureFlags: FeatureFlags = FeatureFlags()
    private var loggingConfig: LoggingConfig = LoggingConfig()
    private var runtimeMode: RuntimeMode = RuntimeMode.STRICT

    fun environment(
        mode: EnvironmentMode,
        strictChecks: Boolean = mode == EnvironmentMode.RELEASE,
        allowDebugLogs: Boolean = mode == EnvironmentMode.DEBUG,
    ) = apply {
        environmentMode = mode
        strictChecksEnabled = strictChecks
        allowDebugLogging = allowDebugLogs
    }

    fun features(block: FeatureFlagsBuilder.() -> Unit) =
        apply {
            featureFlags = FeatureFlagsBuilder().apply(block).build()
        }

    fun logging(
        level: LogLevel = LogLevel.INFO,
        enableSensitiveLogging: Boolean = false,
    ) = apply {
        loggingConfig = LoggingConfig(level = level, enableSensitiveLogging = enableSensitiveLogging)
    }

    fun runtime(mode: RuntimeMode) =
        apply {
            runtimeMode = mode
        }

    fun build(): SecurityConfig =
        SecurityConfig(
            environment =
                EnvironmentConfig(
                    mode = environmentMode,
                    strictChecksEnabled = strictChecksEnabled,
                    allowDebugLogging = allowDebugLogging,
                ),
            featureFlags = featureFlags,
            logging = loggingConfig,
            runtimeMode = runtimeMode,
        )
}

class FeatureFlagsBuilder {
    private var flags: FeatureFlags = FeatureFlags()

    fun enableCrypto(enabled: Boolean = true) = apply { flags = flags.copy(crypto = enabled) }

    fun enableStorage(enabled: Boolean = true) = apply { flags = flags.copy(storage = enabled) }

    fun enableNetwork(enabled: Boolean = true) = apply { flags = flags.copy(networkDefense = enabled) }

    fun enableAuth(enabled: Boolean = true) = apply { flags = flags.copy(authentication = enabled) }

    fun enableDeviceIntegrity(enabled: Boolean = true) = apply { flags = flags.copy(deviceIntegrity = enabled) }

    fun enableSecureLogging(enabled: Boolean = true) = apply { flags = flags.copy(secureLogging = enabled) }

    internal fun build(): FeatureFlags = flags
}
