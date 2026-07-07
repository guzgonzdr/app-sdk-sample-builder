# App SDK Sample Builder — Usercentrics App SDK

A company-wide toolkit for building runnable **samples** of the Usercentrics App SDK from a plain-language
description — an existing feature to showcase, a customer bug to reproduce, or a workaround to prove out.

It bundles two things:
- **Two pristine, minimal host apps** (iOS SwiftUI + Android Kotlin/Compose) that every sample branches from.
- A **Claude skill** (`app-sdk-sample-builder`) that turns a description into a working sample and a traceable
  record — adapting to the requester:
  - **Technical** → a real GitHub branch with the sample code + an engineering changelog, handed off to open
    in Xcode / Android Studio.
  - **Non-technical** → an interactive banner simulation (rendered in a phone mockup, styled from the
    account's real CMP config) plus a plain-language summary delivered to Slack. No GitHub, no IDE.

Every sample shows the consent banner's **first layer** and lets you trigger the deeper layers (second layer,
category toggles, vendor/TCF detail) **granularly**.

## Getting started

1. **Clone this repo** and work from it — Claude Code auto-loads the skill from `.claude/skills`:
   ```bash
   git clone https://github.com/guzgonzdr/app-sdk-sample-builder.git
   cd app-sdk-sample-builder
   ```
2. **Grant the connectors** you'll use (Claude connector settings, or `claude mcp` / `/mcp`):
   GitHub (technical mode), Slack (non-technical mode), Atlassian/Jira + Document360 (bug/workaround research).
3. **Use it** — just describe what you want; the skill infers technical vs non-technical and confirms if
   unclear. Nothing is hardcoded to one machine or account: the repo, Jira cloudId, and Settings ID are all
   resolved at runtime. See [`.claude/skills/app-sdk-sample-builder/SKILL.md`](.claude/skills/app-sdk-sample-builder/SKILL.md)
   and its [`references/sdk-quickref.md`](.claude/skills/app-sdk-sample-builder/references/sdk-quickref.md).

## Layout

```
app-sdk-sample-builder/
├── README.md
├── .claude/skills/
│   ├── app-sdk-sample-builder/   # this skill (SKILL.md + references/sdk-quickref.md)
│   └── new-sdk-test/             # lighter reproduction-scaffolding workflow
├── ios/UCTestApp/                # SwiftUI host app (UsercentricsCore + UsercentricsUI, SPM)
└── android/UCTestApp/            # Kotlin/Compose host app (com.usercentrics.sdk, Maven)
```

Both apps initialize the SDK with a Settings ID (swap in a real one to load a live configuration and render
banners). SDK version pinned: **2.27.1** (both platforms). The base apps on the base branch stay pristine —
sample code goes on a new branch.

## Building / running

Compile-check only; the skill never launches the app for you — open it in your IDE to run.

### Android
```bash
cd android/UCTestApp
./gradlew :app:assembleDebug
```
If your system Java is too new for AGP, point `JAVA_HOME` at a compatible JDK (e.g. Android Studio's bundled
JBR). `local.properties` (gitignored) points at the Android SDK.

### iOS
```bash
cd ios/UCTestApp
xcodegen generate   # regenerate the .xcodeproj from project.yml (XcodeGen: brew install xcodegen)
xcodebuild -scheme UCTestApp -destination 'platform=iOS Simulator,name=<a simulator>' build
```
Set `DEVELOPER_DIR` to your Xcode if `xcodebuild` doesn't resolve it. `project.yml` is the source of truth;
the `.xcodeproj` is generated and not committed.
