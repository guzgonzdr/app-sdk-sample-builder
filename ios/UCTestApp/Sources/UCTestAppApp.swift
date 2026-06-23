import SwiftUI
import Usercentrics

// Swap in a real Settings ID to load a live configuration.
let settingsID = "PLACEHOLDER_SETTINGS_ID"

@main
struct UCTestAppApp: App {
    init() {
        let options = UsercentricsOptions(settingsId: settingsID)
        UsercentricsCore.configure(options: options)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
