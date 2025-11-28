package com.finguard.sdk.core

/**
 * SDK-wide configuration passed into [FinGuard.init].
 */
data class SecurityConfig(
    val environment: EnvironmentConfig,
    val featureFlags: FeatureFlags = FeatureFlags(),
    val logging: LoggingConfig = LoggingConfig(),
    val runtimeMode: RuntimeMode = RuntimeMode.STRICT,
) {
    companion object {
        val Default =
            SecurityConfig(
                environment = EnvironmentConfig(EnvironmentMode.RELEASE),
            )
    }
}

enum class RuntimeMode {
    STRICT,
    RELAXED,
}
