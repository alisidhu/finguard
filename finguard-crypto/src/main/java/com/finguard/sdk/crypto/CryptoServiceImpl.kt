package com.finguard.sdk.crypto

import com.finguard.sdk.core.CryptoService
import com.finguard.sdk.core.LogLevel
import com.finguard.sdk.core.LoggingConfig

internal class CryptoServiceImpl(
    private val aesManager: AESManager,
    private val logging: LoggingConfig,
) : CryptoService {
    override fun encrypt(input: ByteArray): ByteArray {
        val cipher = aesManager.encrypt(input)
        log(LogLevel.DEBUG, "Encrypted ${input.size} bytes")
        return cipher
    }

    override fun decrypt(input: ByteArray): ByteArray {
        val plain = aesManager.decrypt(input)
        log(LogLevel.DEBUG, "Decrypted ${plain.size} bytes")
        return plain
    }

    private fun log(
        level: LogLevel,
        message: String,
    ) {
        val logger = logging.logger
        if (logging.level.priority <= level.priority) {
            logger.log(level, message)
        }
    }
}
