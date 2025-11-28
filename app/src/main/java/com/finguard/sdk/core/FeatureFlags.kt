package com.finguard.sdk.core

/**
 * Feature toggles for SDK modules. Defaults favor minimal surface until
 * a consumer opts-in to additional components.
 */
data class FeatureFlags(
    val crypto: Boolean = true,
    val storage: Boolean = true,
    val networkDefense: Boolean = false,
    val authentication: Boolean = false,
    val deviceIntegrity: Boolean = false,
    val secureLogging: Boolean = false,
)
