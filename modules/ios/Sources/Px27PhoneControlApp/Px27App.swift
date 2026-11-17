import SwiftUI
import Px27PhoneControl

@main
struct Px27App: App {
    var body: some Scene {
        WindowGroup {
            TabView {
                CapabilityExplorerView()
                    .tabItem {
                        Label("Capabilities", systemImage: "sparkles")
                    }

                PairingSecurityView()
                    .tabItem {
                        Label("Pairing", systemImage: "qrcode")
                    }

                DeviceStatusView()
                    .tabItem {
                        Label("Status", systemImage: "iphone")
                    }
            }
        }
    }
}
