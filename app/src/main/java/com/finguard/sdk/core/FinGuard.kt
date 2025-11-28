package com.finguard.sdk.core

/**
 * FinGuard holds the default client instance and enforces single initialization per process.
 * The only global singleton is the [FinGuardClient] reference.
 */
object FinGuard {
    private val lock = Any()

    @Volatile
    private var currentConfig: SecurityConfig? = null

    @Volatile
    private var activeLogger: FinGuardLogger = FinGuardLogger.Console()

    @Volatile
    private var client: FinGuardClient? = null

    /**
     * Install a prepared client as the default singleton instance.
     * Replacing an existing client with a different config is not allowed.
     */
    fun install(client: FinGuardClient): FinGuardResult<FinGuardClient> {
        synchronized(lock) {
            val existing = this.client
            if (existing != null) {
                return if (existing.config == client.config) {
                    FinGuardResult.Success(existing)
                } else {
                    FinGuardResult.Failure(FinGuardError.AlreadyInitialized(existing.config))
                }
            }
            this.client = client
            currentConfig = client.config
            activeLogger = client.config.logging.logger
        }
        activeLogger.log(
            level = LogLevel.INFO,
            message =
                "FinGuard client installed (${client.config.environment.mode}) " +
                    "runtime=${client.config.runtimeMode}, strict=${client.config.environment.strictChecksEnabled}",
        )
        return FinGuardResult.Success(client)
    }

    fun getClient(): FinGuardResult<FinGuardClient> {
        val instance = client ?: return FinGuardResult.Failure(FinGuardError.NotInitialized)
        return FinGuardResult.Success(instance)
    }

    fun requireClient(): FinGuardClient = getClient().getOrThrow()

    internal fun activeLogger(): FinGuardLogger = activeLogger

    internal fun resetForTests() {
        synchronized(lock) {
            currentConfig = null
            activeLogger = FinGuardLogger.Console()
            client = null
        }
    }
}
