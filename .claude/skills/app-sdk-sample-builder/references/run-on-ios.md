# Run the sample on a real iPhone

The interactive artifact is a faithful *simulation*. To see the actual Usercentrics banner on a real iPhone,
you install the sample app on the device. iOS is stricter than Android — every app must be **code-signed**, so
there's no "just send an .apk" equivalent. Two realistic paths:

- **A) Xcode + a free Apple ID** — best for a one-off: you (or a colleague) plug in an iPhone and run it from a
  Mac. No paid account needed.
- **B) TestFlight** — best for sharing with non-technical testers who only have their phone. Requires a paid
  Apple Developer account, but then testers just tap a link.

Both need a **Mac with Xcode** to produce the build. A non-technical tester with only an iPhone can't do it
alone — pair with someone who has a Mac (path A), or ask them to be added to TestFlight (path B).

---

## Path A — Xcode + free Apple ID (one device, ~10 min)

### What you'll need
- A **Mac** with **Xcode** installed (App Store).
- The repo cloned locally, and **XcodeGen** (`brew install xcodegen`).
- An **Apple ID** (any free account) and an **iPhone** + cable.

### Steps
1. **Open the project.**
   ```bash
   cd ios/UCTestApp
   xcodegen generate      # creates UCTestApp.xcodeproj from project.yml
   open UCTestApp.xcodeproj
   ```
2. **Add your Apple ID to Xcode.** Xcode → Settings → **Accounts** → **+** → Apple ID → sign in.
3. **Set signing.** Select the **UCTestApp** target → **Signing & Capabilities**:
   - Tick **Automatically manage signing**.
   - **Team:** pick your name (Personal Team).
   - If you see a bundle-identifier conflict, change the **Bundle Identifier** to something unique, e.g.
     `com.yourname.uctestapp`.
4. **Prepare the iPhone.**
   - Connect it and tap **Trust This Computer** on the phone.
   - iOS 16+: enable **Settings → Privacy & Security → Developer Mode**, toggle on, and restart when asked.
5. **Pick the device and run.** In Xcode's top toolbar, choose your iPhone as the run destination, then press
   **▶ Run** (⌘R). Xcode builds, installs, and launches it.
6. **Trust the developer profile** (first run only). If iOS blocks the app: **Settings → General → VPN &
   Device Management → [your Apple ID] → Trust**. Re-open the app.
7. **Try the banner.** The **first layer** appears on launch; use the on-screen controls to open the
   **second layer**, toggle categories/services, accept/deny, and reset — the real SDK, real config, real banner.

> Free-signing caveat: apps signed with a free Apple ID **expire after 7 days** and you can register a limited
> number of app IDs/devices per week. Re-run from Xcode to refresh. For anything longer-lived, use Path B.

---

## Path B — TestFlight (share with non-technical testers)

Best when the tester only has an iPhone. They install Apple's **TestFlight** app and tap a link — no Xcode, no
cable. Setup requires the **Apple Developer Program** ($99/yr) and an **App Store Connect** account.

### One-time setup (done by someone technical, on a Mac)
1. In **App Store Connect**, create the app record (unique bundle ID).
2. In Xcode, set the target's **Team** to the paid team and automatic signing.
3. **Product → Archive**, then in the Organizer **Distribute App → App Store Connect → Upload**.
4. In App Store Connect → **TestFlight**, wait for processing, then add the build to a tester group and enter
   testers' emails (or enable a **public link**).

### For the tester
1. Install **TestFlight** from the App Store.
2. Open the invite email or the public link → **Accept** → **Install**.
3. Open the app and try the banner as above. TestFlight builds last 90 days.

---

## Troubleshooting (Path A)

- **"Failed to register bundle identifier"** — the ID is taken; change the Bundle Identifier to something
  unique and run again.
- **"Untrusted Developer"** on launch — do Step 6 (trust the profile in Device Management).
- **Device not selectable / "not eligible"** — enable Developer Mode (Step 4), unlock the phone, and make sure
  it's trusted.
- **"Unable to install… 7-day"** — the free-signing certificate expired; just re-run from Xcode.
- **App runs but no banner** — the Settings ID has no active config or the phone is offline. Confirm the
  Settings ID and network.
