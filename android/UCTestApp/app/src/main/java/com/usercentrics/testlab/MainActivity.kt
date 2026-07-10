package com.usercentrics.testlab

import android.content.Intent
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
import androidx.compose.material3.HorizontalDivider
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

// WebView-continuity showcase — GDPR settings ID shared by the app and the
// web page it loads (rNB9ZQeoVlZXl5). The app and the web page MUST use the
// same Settings ID for continuity to work.
private const val SETTINGS_ID = "rNB9ZQeoVlZXl5"

class MainActivity : ComponentActivity() {

    private var status by mutableStateOf("Initializing Usercentrics…")
    private var consentDump by mutableStateOf("")

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
                // First layer on launch when consent is still required.
                if (ready.shouldCollectConsent) showFirstLayer()
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
                        Text("Usercentrics App SDK — WebView Continuity", style = MaterialTheme.typography.titleLarge)
                        Text("Settings ID: $SETTINGS_ID")
                        Text("Status: $status")

                        HorizontalDivider()
                        Button(onClick = { showFirstLayer() }) { Text("Show First Layer") }
                        Button(onClick = { showSecondLayer() }) { Text("Show Settings (Second Layer)") }
                        Button(onClick = { readConsentState() }) { Text("Read Consent State") }

                        HorizontalDivider()
                        Text("WebView continuity", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Collect consent above, then open the WebView. The web page runs the " +
                                "same Settings ID; it should restore your consent instead of re-prompting.",
                        )
                        Button(onClick = { openWebView() }) { Text("Open WebView") }

                        if (consentDump.isNotEmpty()) {
                            HorizontalDivider()
                            Text("Consent state:", style = MaterialTheme.typography.titleMedium)
                            Text(consentDump)
                        }
                    }
                }
            }
        }
    }

    private fun openWebView() {
        startActivity(Intent(this, WebViewActivity::class.java))
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

    // Inspect the current per-service consent decisions held by the SDK.
    private fun readConsentState() {
        val consents = Usercentrics.instance.getConsents()
        consentDump = if (consents.isEmpty()) {
            "No consents yet — collect via First/Second Layer."
        } else {
            consents.joinToString("\n") { c ->
                "${if (c.status) "✔" else "–"} ${c.dataProcessor} (${c.templateId})"
            }
        }
    }
}
