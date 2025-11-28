package com.finguard.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finguard.sample.screens.AuthDemoScreen
import com.finguard.sample.screens.CryptoDemoScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ModuleListScreen(
                        demos =
                            listOf(
                                ModuleDemo(
                                    name = "Crypto",
                                    description = "Encrypt/decrypt using FinGuard crypto module.",
                                    content = { CryptoDemoScreen() },
                                ),
                                ModuleDemo(
                                    name = "Auth",
                                    description =
                                        "Auth demo will showcase token lifecycle (login/refresh/logout) backed by storage + crypto.",
                                    content = { AuthDemoScreen() },
                                ),
                            ),
                    )
                }
            }
        }
    }
}

private data class ModuleDemo(
    val name: String,
    val description: String,
    val content: @Composable () -> Unit,
)

@Composable
@Suppress("FunctionName")
private fun ModuleListScreen(demos: List<ModuleDemo>) {
    val selected = remember { mutableStateOf<ModuleDemo?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (selected.value == null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp),
            ) {
                items(demos) { demo ->
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { selected.value = demo },
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = demo.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = demo.description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Button(onClick = { selected.value = null }, modifier = Modifier.padding(16.dp)) {
                    Text("Back")
                }
                selected.value?.content?.invoke()
            }
        }
    }
}
