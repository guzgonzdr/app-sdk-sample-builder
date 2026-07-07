# Run the sample on a real Android phone

The interactive artifact is a faithful *simulation*. To see the actual Usercentrics banner rendered by the
real SDK on a real device, install the sample app on an Android phone. The most reliable way is **adb**
(Android Debug Bridge); a no-cable "sideload" alternative is at the end.

Plan for ~10 minutes the first time. You need: an Android phone, a USB cable, and the app file (`.apk`).

## What you'll need

- **The app file** — `app-debug.apk`. Either:
  - ask the person who built the sample to send it to you, **or**
  - build it yourself from the repo: `cd android/UCTestApp && ./gradlew :app:assembleDebug` — the file lands
    at `android/UCTestApp/app/build/outputs/apk/debug/app-debug.apk`.
- **adb** on your computer:
  - macOS: `brew install --cask android-platform-tools`, **or**
  - it ships with Android Studio at `~/Library/Android/sdk/platform-tools` (add that folder to your `PATH`).
  - Check it works: `adb version`.

## Step 1 — Turn on Developer Options on the phone

1. Open **Settings → About phone**.
2. Tap **Build number** seven times (it counts down "you are now a developer").
3. Go back to **Settings → System → Developer options** and turn on **USB debugging**.

*(Exact menu names vary by manufacturer — search Settings for "Build number" / "USB debugging" if needed.)*

## Step 2 — Connect the phone

1. Plug the phone into the computer with the USB cable.
2. On the phone, when "Allow USB debugging?" pops up, tick **Always allow from this computer** and tap **OK**.
3. If prompted for USB mode, choose **File transfer / Android Auto** (not "charging only").

## Step 3 — Confirm the computer sees it

```bash
adb devices
```
You should see your device listed as `device`. If it says `unauthorized`, re-check the popup in Step 2. If
nothing shows, try another cable/port (some cables are charge-only).

## Step 4 — Install the app

```bash
adb install -r app-debug.apk
```
`-r` reinstalls over an existing copy. Success prints `Success`. (Run it from the folder holding the `.apk`, or
give the full path.)

## Step 5 — Open it and try the banner

1. Find **UC Test App** (or the sample's name) in the app drawer and open it.
2. The **consent banner's first layer** appears. Use the on-screen buttons to trigger the layers granularly —
   open the **second layer**, toggle categories/services, accept/deny, and reset — and watch the consent
   state update. This is the real SDK, real config, real banner.

## No cable? Wireless install (Android 11+)

1. Phone and computer on the same Wi-Fi. In **Developer options → Wireless debugging**, turn it on and tap
   **Pair device with pairing code** (shows an IP:port and a code).
2. On the computer:
   ```bash
   adb pair <ip>:<port>        # enter the 6-digit code when asked
   adb connect <ip>:<port>     # use the IP:port shown on the main Wireless debugging screen
   adb install -r app-debug.apk
   ```

## Simplest, no-adb path (sideload)

If you just want it on the phone with no tools: copy `app-debug.apk` to the phone (email it to yourself, or
via a USB "File transfer" window, or Google Drive), then tap it in the phone's **Files** app. Android will ask
to **allow installing unknown apps** for that source — allow it, then **Install**.

## Troubleshooting

- **`adb: no devices/emulators found`** — cable is charge-only, or USB debugging is off. Re-do Steps 1–2.
- **`unauthorized`** — accept the debugging prompt on the phone; if it never appears, run
  `adb kill-server && adb start-server` and reconnect.
- **`INSTALL_FAILED_UPDATE_INCOMPATIBLE`** — a different build is installed; remove it first:
  `adb uninstall com.usercentrics.testlab` then install again.
- **`INSTALL_FAILED_USER_RESTRICTED`** (common on Xiaomi/MIUI) — in Developer options also enable
  **Install via USB**.
- **App installs but no banner** — the Settings ID has no active configuration, or the device has no network.
  Confirm the Settings ID and that the phone is online.
