package com.finguard.sdk.core

/**
 * Controls runtime environment behavior for the SDK.
 */
data class EnvironmentConfig(
    val mode: EnvironmentMode,
    val strictChecksEnabled: Boolean = mode == EnvironmentMode.RELEASE,
    val allowDebugLogging: Boolean = mode == EnvironmentMode.DEBUG,
) {
    val isDebug: Boolean
        get() = mode == EnvironmentMode.DEBUG
}

enum class EnvironmentMode {
    DEBUG,
    RELEASE,
}
