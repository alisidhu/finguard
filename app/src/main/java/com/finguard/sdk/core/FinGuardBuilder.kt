package com.finguard.sdk.core

/**
 * Builds a FinGuardClient in a Square/Stripe-style builder pattern.
 * Services are supplied via explicit registration to keep the SDK framework-agnostic.
 */
class FinGuardBuilder {
    private var config: SecurityConfig = SecurityConfig.Default
    private val providers = mutableMapOf<Class<*>, (SecurityConfig) -> Any>()

    fun config(block: SecurityConfigBuilder.() -> Unit): FinGuardBuilder = apply { config = SecurityConfigBuilder().apply(block).build() }

    fun config(config: SecurityConfig): FinGuardBuilder = apply { this.config = config }

    fun <T : Any> withService(
        type: Class<T>,
        provider: (SecurityConfig) -> T,
    ): FinGuardBuilder = apply { providers[type] = provider }

    fun build(): FinGuardClient {
        val services = providers.mapValues { (_, factory) -> factory.invoke(config) }
        val client = FinGuardClient(config = config, services = services)
        FinGuard.install(client)
        return client
    }
}
