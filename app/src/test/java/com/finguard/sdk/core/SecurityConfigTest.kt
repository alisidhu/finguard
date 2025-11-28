package com.finguard.sdk.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityConfigTest {
    @Test
    fun `default configuration favors secure defaults`() {
        val config = SecurityConfig.Default

        assertEquals(EnvironmentMode.RELEASE, config.environment.mode)
        assertTrue(config.environment.strictChecksEnabled)
        assertEquals(RuntimeMode.STRICT, config.runtimeMode)
        assertTrue(config.featureFlags.crypto)
        assertTrue(config.featureFlags.storage)
        assertFalse(config.featureFlags.networkDefense)
        assertEquals(LogLevel.INFO, config.logging.level)
        assertFalse(config.logging.enableSensitiveLogging)
    }

    @Test
    fun `feature flags can be relaxed per consumer choice`() {
        val flags = FeatureFlags(networkDefense = true, authentication = true, deviceIntegrity = true, secureLogging = true)
        val config =
            SecurityConfig(
                environment = EnvironmentConfig(EnvironmentMode.DEBUG, strictChecksEnabled = false, allowDebugLogging = true),
                featureFlags = flags,
                logging = LoggingConfig(level = LogLevel.DEBUG, enableSensitiveLogging = true),
                runtimeMode = RuntimeMode.RELAXED,
            )

        assertEquals(flags, config.featureFlags)
        assertEquals(LogLevel.DEBUG, config.logging.level)
        assertTrue(config.logging.enableSensitiveLogging)
        assertEquals(RuntimeMode.RELAXED, config.runtimeMode)
    }
}
