package com.finguard.sample.screens

import android.util.Base64
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finguard.sdk.core.EnvironmentMode
import com.finguard.sdk.core.FinGuardBuilder
import com.finguard.sdk.core.LogLevel
import com.finguard.sdk.crypto.withCrypto

@Composable
@Suppress("FunctionName")
fun CryptoDemoScreen() {
    val client =
        remember {
            FinGuardBuilder().config {
                environment(
                    mode = EnvironmentMode.DEBUG,
                    strictChecks = false,
                    allowDebugLogs = true,
                )
                logging(level = LogLevel.DEBUG, enableSensitiveLogging = false)
            }
                .withCrypto()
                .build()
        }

    var plain by remember { mutableStateOf("Hello FinGuard") }
    var cipher by remember { mutableStateOf<String?>(null) }
    var decrypted by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Crypto Module Demo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = "Plain text: $plain")
        Button(onClick = {
            try {
                val enc = client.crypto().encrypt(plain.toByteArray())
                cipher = Base64.encodeToString(enc, Base64.NO_WRAP)
                error = null
            } catch (t: Throwable) {
                error = t.message
            }
        }) { Text("Encrypt") }
        cipher?.let { Text(text = "Cipher (Base64): $it") }
        Button(onClick = {
            try {
                val enc = cipher?.let { Base64.decode(it, Base64.NO_WRAP) } ?: return@Button
                decrypted = client.crypto().decrypt(enc).decodeToString()
                error = null
            } catch (t: Throwable) {
                error = t.message
            }
        }) { Text("Decrypt") }
        decrypted?.let { Text(text = "Decrypted: $it") }
        error?.let { Text(text = "Error: $it", color = MaterialTheme.colorScheme.error) }
    }
}
