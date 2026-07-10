package com.usercentrics.testlab

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.ComponentActivity
import com.usercentrics.sdk.Usercentrics

// The web page must run Usercentrics BrowserUI (>= 1.4.0) with the SAME
// Settings ID as this app for continuity to take effect.
private const val WEBVIEW_URL = "https://guzgonzdr.github.io/"
private const val TAG = "UCWebView"

/**
 * WebView Continuity: expose the native SDK's user-session data to the page
 * through a `ucMobileSdk` JavaScript interface. The Browser CMP reads
 * `window.ucMobileSdk.getUserSessionData()` on load and restores that consent
 * instead of showing its own banner.
 */
class WebViewActivity : ComponentActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userSessionData = Usercentrics.instance.getUserSessionData()

        val webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            addJavascriptInterface(UCSessionInterface(userSessionData), "ucMobileSdk")
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(msg: ConsoleMessage): Boolean {
                    Log.d(TAG, "WEBVIEW: ${msg.message()}")
                    return true
                }
            }
            loadUrl(WEBVIEW_URL)
        }
        setContentView(webView)
    }

    /** Bridge object exposed to the page as `window.ucMobileSdk`. */
    private class UCSessionInterface(private val userSessionData: String?) {
        @JavascriptInterface
        fun getUserSessionData(): String? = userSessionData
    }
}
