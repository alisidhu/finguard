package com.finguard.sdk.core

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FinGuardTest {
    @After
    fun tearDown() {
        FinGuard.resetForTests()
    }

    @Test
    fun `init stores config and exposes it`() {
        val config =
            SecurityConfig(
                environment = EnvironmentConfig(EnvironmentMode.DEBUG, strictChecksEnabled = false),
                runtimeMode = RuntimeMode.RELAXED,
            )

        val result = FinGuard.install(FinGuardBuilder().config(config).build())

        assertTrue(result is FinGuardResult.Success)
        assertEquals(config, FinGuard.requireClient().config)
    }

    @Test
    fun `init is idempotent for the same config`() {
        val config = SecurityConfig(environment = EnvironmentConfig(EnvironmentMode.RELEASE))

        val first = FinGuard.install(FinGuardBuilder().config(config).build())
        val second = FinGuard.install(FinGuardBuilder().config(config).build())

        assertTrue(first is FinGuardResult.Success)
        assertTrue(second is FinGuardResult.Success)
        assertEquals(config, FinGuard.requireClient().config)
    }

    @Test
    fun `init rejects a different config after initialization`() {
        val releaseClient =
            FinGuardBuilder().config {
                environment(mode = EnvironmentMode.RELEASE)
            }.build()
        FinGuard.install(releaseClient)

        val result =
            FinGuard.install(
                FinGuardBuilder().config {
                    environment(mode = EnvironmentMode.DEBUG)
                }.build(),
            )

        assertTrue(result is FinGuardResult.Failure)
        val error = (result as FinGuardResult.Failure).error
        assertTrue(error is FinGuardError.AlreadyInitialized)
        assertEquals(releaseClient.config, (error as FinGuardError.AlreadyInitialized).existingConfig)
    }

    @Test
    fun `builder returns client view`() {
        val builder =
            FinGuardBuilder().config {
                environment(mode = EnvironmentMode.DEBUG, strictChecks = false, allowDebugLogs = true)
                features { enableCrypto(false) }
            }

        val client = builder.build()

        assertEquals(EnvironmentMode.DEBUG, client.config.environment.mode)
        try {
            client.crypto()
            fail("Expected module missing exception")
        } catch (ex: FinGuardException) {
            assertTrue(ex.finGuardError is FinGuardError.ModuleNotInstalled)
        }
    }
}
