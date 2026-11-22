import SwiftUI

/// SwiftUI Pairing and Security View for iOS Companion App
public struct PairingSecurityView: View {
    @State private var pairingCode: String = "491-382"
    @State private var emergencyDisconnected: Bool = false
    @State private var selectedAutonomy: AutonomyProfile = .powerUser

    public init() {}

    public var body: some View {
        NavigationStack {
            Form {
                Section("Pairing with px27m Platform") {
                    VStack(alignment: .center, spacing: 10) {
                        Image(systemName: "qrcode")
                            .font(.system(size: 64))
                            .foregroundColor(.green)
                            .padding(.top, 8)
                        Text(pairingCode)
                            .font(.system(.title, design: .monospaced))
                            .bold()
                            .foregroundColor(.green)
                        Text("Enter this one-time code in px27m dashboard to pair")
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                }

                Section("Hybrid Connectivity") {
                    HStack {
                        Text("Local Daemon")
                        Spacer()
                        Text("Active (Port 27028)")
                            .foregroundColor(.green)
                            .font(.caption)
                    }
                    HStack {
                        Text("Remote px27m Gateway")
                        Spacer()
                        Text("Connected (WSS)")
                            .foregroundColor(.green)
                            .font(.caption)
                    }
                }

                Section("Autonomy Ceiling") {
                    Picker("Maximum Agent Role", selection: $selectedAutonomy) {
                        Text("Observer").tag(AutonomyProfile.observer)
                        Text("Assistant").tag(AutonomyProfile.assistant)
                        Text("Operator").tag(AutonomyProfile.operatorProfile)
                        Text("Power User").tag(AutonomyProfile.powerUser)
                    }
                    Text("Agents cannot exceed this privilege boundary regardless of role.")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }

                Section("Emergency Controls") {
                    Toggle("Emergency Disconnect", isOn: $emergencyDisconnected)
                        .tint(.red)
                    if emergencyDisconnected {
                        Text("All agent sessions immediately terminated. Capabilities suspended.")
                            .font(.caption)
                            .foregroundColor(.red)
                    }
                }
            }
            .navigationTitle("Pairing & Security")
        }
    }
}
