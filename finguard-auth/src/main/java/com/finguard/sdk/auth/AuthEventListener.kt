package com.finguard.sdk.auth

interface AuthEventListener {
    fun onSessionExpired() {}

    fun onForcedLogout() {}

    companion object {
        val NONE: AuthEventListener = object : AuthEventListener {}
    }
}
