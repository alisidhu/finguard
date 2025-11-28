package com.finguard.sample

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.finguard.sdk.core.EnvironmentMode
import com.finguard.sdk.core.FinGuardBuilder
import com.finguard.sdk.core.LogLevel
import com.finguard.sdk.crypto.withCrypto

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val client = FinGuardBuilder().config {
                    environment(
                        mode = EnvironmentMode.DEBUG,
                        strictChecks = false,
                        allowDebugLogs = true,
                    )
                    logging(level = LogLevel.DEBUG, enableSensitiveLogging = false)
                }
                .withCrypto()
                .build()

        val encrypted = client.crypto().encrypt("Hello FinGuard".toByteArray())
        val decrypted = client.crypto().decrypt(encrypted).decodeToString()

        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.sample_text).text =
            buildString {
                appendLine("FinGuard Sample")
                appendLine("Encrypted bytes: ${encrypted.size}")
                appendLine("Decrypted text: $decrypted")
            }
    }
}
