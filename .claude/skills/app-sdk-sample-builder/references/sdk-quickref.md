# Usercentrics App SDK — Quick Reference

Ground every generated sample in the APIs below. They are transcribed from the official docs
(usercentrics.com/docs/apps). If a call you need is not listed here and you cannot confirm it via
the Document360 connector or the live docs, mark it `// VERIFY:` in the output rather than
inventing it.

## Contents
- [Two components](#two-components)
- [Confidence levels](#confidence-levels)
- [Initialize](#initialize)
- [Check status with isReady](#check-status-with-isready)
- [Show the first layer](#show-the-first-layer)
- [Show the second layer](#show-the-second-layer)
- [Handle the userResponse](#handle-the-userresponse)
- [Read consent state](#read-consent-state)
- [Build-your-own-UI helpers](#build-your-own-ui-helpers)
- [TCF / US-framework helpers](#tcf--us-framework-helpers)
- [Key objects](#key-objects)
- [Config the sample consumes](#config-the-sample-consumes)
- [Fetch the real CMP config](#fetch-the-real-cmp-config)
- [Version & compliance gotchas](#version--compliance-gotchas)
- [Environment & access](#environment--access)
- [Verification allowlist](#verification-allowlist)

## Two components
- **UsercentricsCore** — collect/document/manage consent, language detection, geolocation. Use
  alone if building your own UI.
- **UsercentricsUI** — ready-made consent banner (first + second layer) that hooks into the Core.

Three presentation strategies: use **UsercentricsUI** out of the box, **build your own UI** on the
Core as a data source, or **hybrid** (e.g. your own first layer, UsercentricsUI second layer).

## Confidence levels
- **Fully confirmed (with code samples in docs):** `configure` / `initialize`, `isReady` /
  `status`, `UsercentricsBanner`, `showFirstLayer`, `showSecondLayer`, the `userResponse` shape,
  `shouldCollectConsent`, `consents`, `geolocationRuleset`.
- **Confirmed by name (Core API index; verify exact signature before relying on it):**
  `getConsents`, `getControllerId`, `acceptAll`, `denyAll`, `saveDecisions`, `getCMPData`,
  `getTCFData`, `getTCString`, `getAdditionalConsentModeData`, `changeLanguage`,
  `restoreUserSession`, `clearUserSession`, `getUserSessionData`, `track`,
  `getABTestingVariant`, `setABTestingVariant`.

## Initialize
Initialize **once per app lifecycle**, as early as possible (background "cold init" caches data).
Configure with either a `settingsId` **or** a `ruleSetId` (not both). Never enable third-party
tracking before consent.

**iOS (Swift)** — in `AppDelegate`:
```swift
import Usercentrics

let options = UsercentricsOptions(settingsId: "<SettingsID>")
UsercentricsCore.configure(options: options)
```

**Android (Kotlin)** — in `Application`:
```kotlin
import com.usercentrics.sdk.*

val options = UsercentricsOptions(settingsId = "<SettingsID>")
Usercentrics.initialize(this, options)
```

**Flutter (Dart)** — e.g. in the entry-point widget's `initState`:
```dart
import 'package:usercentrics_sdk/usercentrics_sdk.dart';

Usercentrics.initialize(settingsId: "<SettingsID>");
```

**React Native (TypeScript)** — at the app entry point:
```ts
import { Usercentrics, UsercentricsOptions } from '@usercentrics/react-native-sdk';

const options = new UsercentricsOptions("<SettingsID>");
Usercentrics.configure(options);
```

For geolocation-driven setups, configure `ruleSetId` instead of `settingsId` (e.g.
`UsercentricsOptions(ruleSetId = "<RulesetID>")`).

## Check status with isReady
Wait for `isReady` before calling any other SDK method — calling early can crash. Use the returned
status to decide whether to collect or just apply consent, and to respect geolocation.

**iOS:**
```swift
UsercentricsCore.isReady { status in
    if let ruleset = status.geolocationRuleset, ruleset.bannerRequiredAtLocation == false {
        return // banner not required at this location
    }
    if status.shouldCollectConsent {
        self.collectConsent()        // present the banner
    } else {
        // apply consent with status.consents
    }
} onFailure: { error in
    // handle error
}
```

**Android:**
```kotlin
Usercentrics.isReady({ status ->
    if (status.geolocationRuleset != null && status.geolocationRuleset?.bannerRequiredAtLocation == false) {
        return@isReady
    }
    if (status.shouldCollectConsent) {
        collectConsent()
    } else {
        // apply consent with status.consents
    }
}, { error -> /* handle error */ })
```

**Flutter / React Native:** `final status = await Usercentrics.status;` /
`const status = await Usercentrics.status();` then branch on `status.shouldCollectConsent`.

## Show the first layer
Friendly, compact, customizable. Presentable as Popup, Sheet, or Full. Use it for first-time
collection or to prompt consent updates. The user can navigate from here into the second layer.

```swift
let banner = UsercentricsBanner()
banner.showFirstLayer { userResponse in /* handle userResponse */ }
```
```kotlin
val banner = UsercentricsBanner(context) // context should be an Activity or wrapper; keep it alive
banner.showFirstLayer { userResponse -> /* handle userResponse */ }
```
```dart
final userResponse = await Usercentrics.showFirstLayer();
```
```ts
const userResponse = await Usercentrics.showFirstLayer();
```

## Show the second layer
Owns full compliance detail and enables **granular** per-service choices. Use it to let users
review/change earlier decisions from your settings screen. Same `userResponse` is returned.

```swift
let banner = UsercentricsBanner()
banner.showSecondLayer { userResponse in /* handle userResponse */ }
```
```kotlin
val banner = UsercentricsBanner(context)
banner.showSecondLayer { userResponse -> /* handle userResponse */ }
```
```dart
final userResponse = await Usercentrics.showSecondLayer();
```
```ts
const userResponse = await Usercentrics.showSecondLayer();
```

**iOS host view (optional):** pass a specific host controller, e.g.
`showSecondLayer(hostView: <UIViewController>)`. If omitted, the SDK resolves the host from the
first window's presented controller. **SwiftUI** support is available from **v2.7.6**.

**Android:** the banner uses the `Dialog` API — use an `Activity` (or wrapper) `Context` that is
alive while the dialog shows.

## Handle the userResponse
Returned by both layers. Shape:

| Property | Type | Notes |
| --- | --- | --- |
| `consents` | `[UsercentricsServiceConsent]` | The user's per-service choices; feed these into Apply Consent. |
| `userInteraction` | enum | `AcceptAll`, `DenyAll`, `Granular`, or `NoInteraction` (banner dismissed with no response). |
| `controllerId` | String | Usercentrics-generated user identifier; required for Cross-Device Consent Sharing. |

After collecting, you **must** apply the choices to your third-party SDKs (see Apply Consent).

## Read consent state
- `shouldCollectConsent` — whether the banner still needs to be shown.
- `getConsents()` — current list of `UsercentricsServiceConsent`.
- `getControllerId()` — the controller ID for the current user.
- On the `status` object from `isReady`: `status.consents`, `status.shouldCollectConsent`,
  `status.geolocationRuleset`.

## Build-your-own-UI helpers
When rendering a custom banner on top of the Core:
- `getCMPData()` — all data needed to render UI (services, categories, labels, localization).
- `acceptAll()` / `denyAll()` — record blanket decisions.
- `saveDecisions(...)` — persist granular per-service decisions.

## TCF / US-framework helpers
- TCF: `getTCFData()`, `getTCString()`, `getAdditionalConsentModeData()`, `setCMPId(...)`,
  `acceptAllForTCF()`, `denyAllForTCF()`, `saveDecisionsForTCF(...)`.
- US/CCPA: `getUSPData()` and the CCPA `saveDecisions(...)` variant.

## Key objects
- **UsercentricsOptions** — init config; carries `settingsId` or `ruleSetId` (+ other device options).
- **UsercentricsReadyStatus** — `shouldCollectConsent`, `consents`, `geolocationRuleset`.
- **UsercentricsServiceConsent** — one service's consent decision (the unit of `consents`).
- **GeolocationRuleset** — includes `bannerRequiredAtLocation`.
- **UsercentricsCMPData** — full CMP dataset for build-your-own-UI.

## Config the sample consumes
For the non-technical artifact's "real SDK configuration JSON", model the initialization inputs the
SDK actually takes, e.g.:
```json
{ "settingsId": "<SettingsID>", "ruleSetId": null, "language": "en" }
```
Use exactly one of `settingsId` / `ruleSetId`. Reflect the scenario (e.g. a `ruleSetId` when the
request is about geolocation-driven banner display).

## Fetch the real CMP config
Never invent categories, services, banner copy, or theme — the SDK loads all of it from the config
CDN at init, so a sample with made-up services (or the wrong colors) is simply wrong. Fetch the
account's real configuration for the Settings ID first and build the sample from it.

**Endpoint** (verified against the 2.27.1 SDK binary — host `config.eu.usercentrics.eu`):
```
https://config.eu.usercentrics.eu/settings/<SETTINGS_ID>/latest/<LANG>.json
```
e.g. `curl -s https://config.eu.usercentrics.eu/settings/USFjQXTEYrUlsw/latest/en.json` (~26 KB for a
typical GDPR account). Do **not** use `app.usercentrics.eu/latest/settings/...` or the
`aggregator.*/aggregate/...` paths — they 403/400 for these IDs. The endpoint is public and
unauthenticated, so you can ground samples in real data for any Settings ID a requester gives,
including a customer's own.

**What to extract:**
- `framework` — `GDPR` / `TCF` / (US via the `ccpa` block).
- `categories[]` — `categorySlug`, `label`, `description`, `isEssential`.
- `consentTemplates[]` — each is a service / data processor: `dataProcessor` (name), `categorySlug`,
  `description`. Group these under their category.
- `bannerMessage`, `labels.btnAcceptAll` / `btnDeny` / `btnSave` / `btnMoreInfo`, `secondLayerTitle` —
  the real copy to render.
- `version` — the config version (shows the sample is built from live data).
- **`customization`** — the account's real **theme**; style the banner from it, don't use generic colors:
  - `color.primary`, `color.acceptBtnBackground` / `acceptBtnText`, `denyBtnBackground` / `denyBtnText`,
    `saveBtnBackground` / `saveBtnText`, `color.text`, `color.layerBackground`, `color.border`.
  - `borderRadiusLayer`, `borderRadiusButton`, `overlayOpacity`, `font.family`, `font.size`, `logoUrl`.
  - (The older `styles` object is the legacy v1 web theme — the App SDK v2 uses `customization`.)

**Rendering (non-technical artifact):** render the banner **inside a phone mockup** — device frame,
status bar, bottom sheet matching `firstLayerMobileVariant` — and apply the `customization` theme so it
matches the real device appearance. Keep the banner fixed to the configured appearance; only the page
around it follows the viewer's light/dark theme. If an account has a `logoUrl`, inline it as a data URI
(the artifact sandbox blocks external images). If the fetch fails (auth / network / unknown ID), say so
and fall back to a clearly-labelled generic example — don't pass invented services off as the account's
real config.

## Version & compliance gotchas
- **TCF 2.3** requires SDK **>= 2.24.1**; older versions are not TCF compliant after 28 Feb 2026.
- **SwiftUI** support starts at **v2.7.6**.
- Initialize **once**; re-initializing with a new `settingsId` triggers a reset.
- If first init fails, call `initialize()` again to clear local storage and release the instance.
- Frameworks supported include GDPR, TCF 2.3, CCPA/CPRA and other US frameworks, and LGPD.

## Environment & access
This skill is company-wide, so nothing here is tied to one person's machine or account — resolve each value
at runtime from the environment.

- **Base-apps repo:** the git repo this skill ships in. Get the local root from the skill's own location
  (the repo root above `.claude/`) and the GitHub slug from `git remote get-url origin`. Both apps live in
  it: `android/UCTestApp`, `ios/UCTestApp`. Keep the base branch pristine — all sample code goes on a new
  branch. Pull latest before a session. (For company-wide rollout, host this repo under a shared org so
  everyone pulls the same base.)
- **Connectors to grant** (see SKILL.md → Prerequisites): GitHub (technical mode), Slack (non-technical
  mode), Atlassian/Jira + Document360 (bug/workaround research).
- **Settings IDs:** use the requester's own whenever given. Shared demo defaults bundled for quick starts:
  GDPR `USFjQXTEYrUlsw`, TCF `5PK-ZGuYVcbHKP`.
- **Jira (Step 3):** resolve the cloudId at runtime with `getAccessibleAtlassianResources` — don't hardcode
  it. App SDK projects: `MSDK` (engineering) + `CTS` (support). Use `searchJiraIssuesUsingJql` with minimal
  `fields` and small `maxResults`; never `getVisibleJiraProjects` with expand.
- **Build (compile-check only; never launch on the technical path)** — from the platform app dir:
  - Android: `./gradlew :app:assembleDebug`. If your system Java is too new for AGP, point `JAVA_HOME` at a
    compatible JDK (e.g. Android Studio's bundled JBR).
  - iOS: `xcodegen generate` then `xcodebuild -scheme UCTestApp -destination 'platform=iOS Simulator,name=<a simulator>' build`. Set `DEVELOPER_DIR` to your Xcode if `xcodebuild` doesn't resolve it.

## Verification allowlist
Treat these identifiers as known-good when running the skill's verification pass. Anything outside
this set that isn't confirmed via live docs should be flagged `// VERIFY:`.

`UsercentricsCore`, `Usercentrics`, `UsercentricsOptions`, `UsercentricsBanner`,
`configure`, `initialize`, `isReady`, `status`, `showFirstLayer`, `showSecondLayer`,
`shouldCollectConsent`, `getConsents`, `getControllerId`, `acceptAll`, `denyAll`, `saveDecisions`,
`getCMPData`, `getTCFData`, `getTCString`, `getAdditionalConsentModeData`, `setCMPId`,
`acceptAllForTCF`, `denyAllForTCF`, `saveDecisionsForTCF`, `getUSPData`, `changeLanguage`,
`restoreUserSession`, `clearUserSession`, `getUserSessionData`, `track`, `getABTestingVariant`,
`setABTestingVariant`, `UsercentricsReadyStatus`, `UsercentricsServiceConsent`,
`GeolocationRuleset`, `UsercentricsLocation`, `UsercentricsCMPData`, `bannerRequiredAtLocation`,
`userInteraction` (`AcceptAll` / `DenyAll` / `Granular` / `NoInteraction`), `controllerId`,
`settingsId`, `ruleSetId`.
