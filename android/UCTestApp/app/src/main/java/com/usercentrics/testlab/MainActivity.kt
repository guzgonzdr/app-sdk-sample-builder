package com.usercentrics.testlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.usercentrics.sdk.Usercentrics
import com.usercentrics.sdk.UsercentricsBanner
import com.usercentrics.sdk.UsercentricsOptions
import com.usercentrics.sdk.UsercentricsReadyStatus

// Settings IDs — GDPR is active; switch to TCF by uncommenting it (and commenting GDPR).
private const val SETTINGS_ID = "O7r4-zhZTP8NZ0"      // GDPR
// private const val SETTINGS_ID = "_UMMPEZE0OG27J"   // TCF

class MainActivity : ComponentActivity() {

    private var status by mutableStateOf("Initializing Usercentrics…")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Usercentrics.initialize(this, UsercentricsOptions(settingsId = SETTINGS_ID))
        Usercentrics.isReady(
            onSuccess = { ready: UsercentricsReadyStatus ->
                status = if (ready.shouldCollectConsent) {
                    "Ready — consent required (${ready.consents.size} services)"
                } else {
                    "Ready — consent already collected (${ready.consents.size} services)"
                }
            },
            onFailure = { error ->
                status = "Init failed: ${error.message}"
            },
        )

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("Usercentrics App SDK — Test Lab", style = MaterialTheme.typography.titleLarge)
                        Text("Settings ID: $SETTINGS_ID")
                        Text("Status: $status")
                        Button(onClick = { showFirstLayer() }) { Text("Show First Layer") }
                        Button(onClick = { showSecondLayer() }) { Text("Show Settings (Second Layer)") }
                    }
                }
            }
        }
    }

    private fun showFirstLayer() {
        UsercentricsBanner(this).showFirstLayer { response ->
            status = response?.let { "First Layer → ${it.userInteraction} (${it.consents.size} consents)" }
                ?: "First Layer dismissed (no interaction)"
        }
    }

    private fun showSecondLayer() {
        UsercentricsBanner(this).showSecondLayer { response ->
            status = response?.let { "Second Layer → ${it.userInteraction} (${it.consents.size} consents)" }
                ?: "Second Layer dismissed (no interaction)"
        }
    }
}
