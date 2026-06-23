import SwiftUI
import Usercentrics

// Settings IDs — GDPR is active; switch to TCF by uncommenting it (and commenting GDPR).
let settingsID = "O7r4-zhZTP8NZ0"      // GDPR
// let settingsID = "_UMMPEZE0OG27J"   // TCF

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
