# Android — WebView Continuity showcase

| Field        | Value |
|--------------|-------|
| Date         | 2026-07-10 |
| Platform     | Android |
| Branch       | test/2026-07-10-android-webview-continuity |
| Settings ID  | rNB9ZQeoVlZXl5 |
| Status       | Implemented |

## Description
> "Create an android project that shows the webview continuity feature with this settingsID rNB9ZQeoVlZXl5"

A minimal Android sample on top of the base UC Test App that demonstrates **WebView Continuity**:
consent collected by the native App SDK is handed to a web page's Browser CMP so the page restores
that consent instead of showing its own banner.

## Intent
Feature showcase (not a bug repro or workaround).

## Customer / context
- **SDK version:** `com.usercentrics.sdk:usercentrics(-ui):2.27.1` (base app).
- **Settings ID:** `rNB9ZQeoVlZXl5` — GDPR framework, config version `2.1.39`, categories
  Marketing / Functional / Essential, 7 services (fetched live from
  `config.eu.usercentrics.eu/settings/rNB9ZQeoVlZXl5/latest/en.json`).
- **Web side:** the page loaded in the WebView (`https://guzgonzdr.github.io/`) runs Usercentrics
  BrowserUI with the **same** Settings ID.

## What the sample demonstrates
1. **Initialize** the App SDK once with `settingsId = rNB9ZQeoVlZXl5`.
2. **First layer** shown automatically on launch when consent is still required (and via a button).
3. **Second layer** (granular per-service toggles) via a button.
4. **Read consent state** — a button dumps the current per-service decisions from
   `Usercentrics.instance.getConsents()`.
5. **WebView continuity** — "Open WebView" launches a `WebView` that exposes the native session to
   the page; the Browser CMP restores it instead of re-prompting.

## How it's solved (the continuity mechanism)
The Browser CMP (BrowserUI ≥ 1.4.0) looks for a JS bridge named `ucMobileSdk` and calls
`window.ucMobileSdk.getUserSessionData()` on load. `WebViewActivity`:
- reads the native session with `Usercentrics.instance.getUserSessionData()`,
- registers it as a JavaScript interface named **`ucMobileSdk`** via `addJavascriptInterface(...)`,
  exposing a `@JavascriptInterface getUserSessionData()` that returns that string,
- enables JavaScript + DOM storage and loads the page.

Requirements for continuity to take effect: the web page must run Usercentrics BrowserUI ≥ 1.4.0 and
use the **same** Settings ID as the app. See the
[WebView Continuity docs](https://docs.usercentrics.com/cmp_in_app_sdk/latest/features/webview-continuity/).

## Changes made
- `android/.../MainActivity.kt` — Settings ID set to `rNB9ZQeoVlZXl5`; first layer auto-shown when
  consent is required; added **Show Second Layer**, **Read Consent State**, and **Open WebView**
  controls plus an on-screen consent dump.
- `android/.../WebViewActivity.kt` — new activity: `WebView` with JS + DOM storage and the
  `ucMobileSdk` bridge returning `getUserSessionData()`; forwards console logs to logcat (`UCWebView`).
- `android/.../AndroidManifest.xml` — registered `WebViewActivity` (INTERNET permission already present).

Verified: `./gradlew :app:assembleDebug` succeeds (SDK 2.27.1, `compileDebugKotlin` clean).

## How to run
1. Open `android/UCTestApp` in **Android Studio** (base branch `main`, this branch
   `test/2026-07-10-android-webview-continuity`). Entry point: `MainActivity`.
2. Run on an emulator or device. Collect consent via First/Second Layer, then tap **Open WebView**.
3. The page should load without its own banner (continuity worked). Watch logcat tag `UCWebView` for
   the page's console output.

> This skill does not launch an emulator or device. To put it on a physical phone, see
> `references/run-on-android.md` (adb install of `app/build/outputs/apk/debug/app-debug.apk`).

## Related Jira ticket(s)
Atlassian was **not** queried live this session (the connector isn't authorized in this
non-interactive run). The following tickets are the ones the repo already documents for this exact
feature (carried over from `test/2026-06-23-implement-webview-to-https-guzgonzdr`) and remain the
canonical references:
- [MSDK-3850](https://usercentrics.atlassian.net/browse/MSDK-3850) — [WebView] GDPR — consent state propagated to Browser CMP.
- [MSDK-3851](https://usercentrics.atlassian.net/browse/MSDK-3851) — [WebView] TCF — consent state propagated to Browser CMP.
- [MSDK-3852](https://usercentrics.atlassian.net/browse/MSDK-3852) — [WebView] US/CCPA — consent state propagated to Browser CMP.
- [CTS-4353](https://usercentrics.atlassian.net/browse/CTS-4353) — Browser SDK returns 403 on `en.json` when a draft version exists (a real gotcha for continuity setups).
- [CTS-4521](https://usercentrics.atlassian.net/browse/CTS-4521) — Platform-scoped service configuration for a shared app + web Settings ID.
