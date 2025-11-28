package com.finguard.sdk.auth

import java.security.MessageDigest

internal object MessageDigestHelper {
    fun constantTimeEquals(
        a: ByteArray,
        b: ByteArray,
    ): Boolean = MessageDigest.isEqual(a, b)
}
