import SwiftUI
import Usercentrics
import UsercentricsUI

struct ContentView: View {
    @State private var status = "Initializing Usercentrics…"

    var body: some View {
        VStack(spacing: 16) {
            Text("Usercentrics App SDK — Test Lab")
                .font(.title2).bold()
                .multilineTextAlignment(.center)
            Text("Settings ID: \(settingsID)")
                .font(.footnote)
            Text("Status: \(status)")
                .multilineTextAlignment(.center)
            Button("Show First Layer") { showFirstLayer() }
                .buttonStyle(.borderedProminent)
            Button("Show Settings (Second Layer)") { showSecondLayer() }
                .buttonStyle(.bordered)
        }
        .padding()
        .onAppear(perform: checkReady)
    }

    private func checkReady() {
        UsercentricsCore.isReady { ready in
            status = ready.shouldCollectConsent
                ? "Ready — consent required (\(ready.consents.count) services)"
                : "Ready — consent already collected (\(ready.consents.count) services)"
        } onFailure: { error in
            status = "Init failed: \(error.localizedDescription)"
        }
    }

    private func showFirstLayer() {
        let banner = UsercentricsBanner()
        banner.showFirstLayer { response in
            status = "First Layer → \(response.userInteraction) (\(response.consents.count) consents)"
        }
    }

    private func showSecondLayer() {
        let banner = UsercentricsBanner()
        banner.showSecondLayer { response in
            status = "Second Layer → \(response.userInteraction) (\(response.consents.count) consents)"
        }
    }
}
