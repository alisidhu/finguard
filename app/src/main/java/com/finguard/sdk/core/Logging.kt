package com.finguard.sdk.core

/**
 * Logging abstraction so downstream modules can plug their own logger.
 */
interface FinGuardLogger {
    fun log(
        level: LogLevel,
        message: String,
        throwable: Throwable? = null,
    )

    class Console(private val minLevel: LogLevel = LogLevel.INFO) : FinGuardLogger {
        override fun log(
            level: LogLevel,
            message: String,
            throwable: Throwable?,
        ) {
            if (level.priority < minLevel.priority) return
            val rendered = "[FinGuard][${level.name}] $message"
            if (throwable != null) {
                println("$rendered\n${throwable.stackTraceToString()}")
            } else {
                println(rendered)
            }
        }
    }
}

enum class LogLevel(val priority: Int) {
    VERBOSE(1),
    DEBUG(2),
    INFO(3),
    WARN(4),
    ERROR(5),
}

data class LoggingConfig(
    val level: LogLevel = LogLevel.INFO,
    val enableSensitiveLogging: Boolean = false,
    val logger: FinGuardLogger = FinGuardLogger.Console(level),
)
