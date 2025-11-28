package com.finguard.sdk.auth

interface TimeProvider {
    fun now(): Long
}

object SystemTimeProvider : TimeProvider {
    override fun now(): Long = System.currentTimeMillis()
}
