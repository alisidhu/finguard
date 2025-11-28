package com.finguard.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finguard.sample.support.InMemorySecureStorage
import com.finguard.sdk.auth.AuthEventListener
import com.finguard.sdk.auth.AuthInstaller
import com.finguard.sdk.auth.AuthPolicies
import com.finguard.sdk.core.AuthCredentials
import com.finguard.sdk.core.AuthSession
import com.finguard.sdk.core.LogLevel
import com.finguard.sdk.core.LoggingConfig
import com.finguard.sdk.crypto.CryptoConfig
import com.finguard.sdk.crypto.CryptoInstaller
import kotlinx.coroutines.delay

@Composable
@Suppress("FunctionName")
fun AuthDemoScreen() {
    val crypto =
        remember {
            CryptoInstaller.create(
                config = CryptoConfig(),
                logging = LoggingConfig(level = LogLevel.DEBUG),
            )
        }
    val storage = remember { InMemorySecureStorage() }
    val currentSession = remember { mutableStateOf<AuthSession?>(null) }
    val currentError = remember { mutableStateOf<String?>(null) }
    val auth =
        remember {
            val eventSink =
                object : AuthEventListener {
                    override fun onSessionExpired() {
                        currentError.value = "Session expired. Please log in again."
                        currentSession.value = null
                    }

                    override fun onForcedLogout() {
                        currentError.value = "Forced logout due to policy."
                        currentSession.value = null
                    }
                }
            AuthInstaller.create(
                crypto = crypto,
                storage = storage,
                policies =
                    AuthPolicies(
                        accessTokenTtlMillis = 30_000,
                    ),
                logging = LoggingConfig(level = LogLevel.DEBUG),
                listener = eventSink,
            )
        }

    LaunchedEffect(auth) {
        while (true) {
            currentSession.value = auth.session()
            delay(1000)
        }
    }

    fun loginDemo() {
        runCatching {
            val now = System.currentTimeMillis()
            auth.login(
                AuthCredentials.TokenCredentials(
                    userId = "user-123",
                    accessToken = "access-$now",
                    refreshToken = "refresh-$now",
                    issuer = "finguard.sample",
                    accessTokenExpiresAt = now + 30_000,
                ),
            )
        }.onSuccess {
            currentSession.value = it
            currentError.value = null
        }.onFailure { currentError.value = it.message }
    }

    fun refreshDemo() {
        runCatching { auth.refresh() }
            .onSuccess {
                currentSession.value = it
                currentError.value = null
            }.onFailure { currentError.value = it.message }
    }

    fun logoutDemo() {
        auth.logout()
        currentSession.value = null
        currentError.value = null
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Auth Module Demo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Button(onClick = { loginDemo() }) { Text("Login") }
        Button(onClick = { refreshDemo() }, enabled = currentSession.value != null) { Text("Refresh") }
        Button(onClick = { logoutDemo() }, enabled = currentSession.value != null) { Text("Logout") }

        currentSession.value?.let {
            Text(text = "User: ${it.userId}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Access: ${it.accessToken}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Refresh: ${it.refreshToken ?: "none"}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Expires: ${it.expiresAt}", style = MaterialTheme.typography.bodySmall)
        } ?: Text(text = "No active session", style = MaterialTheme.typography.bodyMedium)

        currentError.value?.let { Text(text = "Error: $it", color = MaterialTheme.colorScheme.error) }
    }
}
