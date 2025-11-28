package com.finguard.sdk.core

sealed class FinGuardError {
    data class AlreadyInitialized(val existingConfig: SecurityConfig) : FinGuardError()

    data object NotInitialized : FinGuardError()

    data class InvalidConfiguration(val reason: String) : FinGuardError()

    data class ModuleNotInstalled(val moduleName: String) : FinGuardError()

    data class ModuleRegistrationFailed(val moduleName: String, val cause: Throwable) : FinGuardError()
}

sealed class FinGuardResult<out T> {
    data class Success<T>(val value: T) : FinGuardResult<T>()

    data class Failure(val error: FinGuardError) : FinGuardResult<Nothing>()

    fun getOrThrow(): T =
        when (this) {
            is Success -> value
            is Failure -> throw FinGuardException(error)
        }
}

class FinGuardException(val finGuardError: FinGuardError) : IllegalStateException(finGuardError.describe()) {
    companion object {
        private fun FinGuardError.describe(): String =
            when (this) {
                is FinGuardError.AlreadyInitialized -> "FinGuard already initialized"
                FinGuardError.NotInitialized -> "FinGuard not initialized"
                is FinGuardError.InvalidConfiguration -> "Invalid configuration: $reason"
                is FinGuardError.ModuleNotInstalled -> "Module $moduleName is not installed"
                is FinGuardError.ModuleRegistrationFailed -> "Module $moduleName failed to register: ${cause.message}"
            }
    }
}
