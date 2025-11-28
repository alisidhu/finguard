package com.finguard.sdk.crypto

import android.annotation.SuppressLint

/**
 * Base64 helper that works on both Android runtime and JVM unit tests.
 * Prefers android.util.Base64 only when running on Android; otherwise uses java.util.Base64.
 */
internal object Base64Compat {
    private val isAndroidRuntime: Boolean =
        try {
            val vmName = System.getProperty("java.vm.name") ?: ""
            vmName.contains("Dalvik", ignoreCase = true) || vmName.contains("ART", ignoreCase = true)
        } catch (_: Exception) {
            false
        }

    private val androidBase64Available: Boolean =
        isAndroidRuntime && runCatching { Class.forName("android.util.Base64") }.isSuccess

    private const val NO_WRAP = 2 // android.util.Base64.NO_WRAP

    @SuppressLint("NewApi")
    fun encode(data: ByteArray): ByteArray {
        return if (androidBase64Available) {
            val clazz = Class.forName("android.util.Base64")
            val method = clazz.getMethod("encode", ByteArray::class.java, Int::class.javaPrimitiveType)
            method.invoke(null, data, NO_WRAP) as ByteArray
        } else {
            java.util.Base64.getEncoder().encode(data)
        }
    }

    @SuppressLint("NewApi")
    fun decode(data: ByteArray): ByteArray {
        return if (androidBase64Available) {
            val clazz = Class.forName("android.util.Base64")
            val method = clazz.getMethod("decode", ByteArray::class.java, Int::class.javaPrimitiveType)
            method.invoke(null, data, NO_WRAP) as ByteArray
        } else {
            java.util.Base64.getDecoder().decode(data)
        }
    }
}
