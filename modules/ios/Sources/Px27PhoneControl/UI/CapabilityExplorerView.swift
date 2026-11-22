import SwiftUI

/// SwiftUI Capability Explorer for iOS Companion App
public struct CapabilityExplorerView: View {
    @State private var manifest: CapabilityManifest
    @State private var selectedCategory: String = "all"
    @State private var testResultText: String? = nil

    private let discovery = CapabilityDiscovery()
    private let engine = ExecutionEngine()

    public init() {
        _manifest = State(initialValue: CapabilityDiscovery().discoverCapabilities(deviceId: "ios_device"))
    }

    private var categories: [String] {
        ["all", "screen", "application", "device_info", "connectivity", "telephony", "messaging", "audio", "camera", "storage", "notifications", "sensors", "settings", "automation", "security"]
    }

    private var filteredCapabilities: [CapabilityDefinition] {
        if selectedCategory == "all" {
            return manifest.capabilities
        }
        return manifest.capabilities.filter { $0.category == selectedCategory }
    }

    public var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Category Filter Chips
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(categories, id: \.self) { cat in
                            Button(action: { selectedCategory = cat }) {
                                Text(cat.replacingOccurrences(of: "_", with: " ").capitalized)
                                    .font(.caption)
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(selectedCategory == cat ? Color.green.opacity(0.2) : Color(.systemGray6))
                                    .foregroundColor(selectedCategory == cat ? .green : .primary)
                                    .clipShape(Capsule())
                                    .overlay(
                                        Capsule().stroke(selectedCategory == cat ? Color.green : Color.clear, lineWidth: 1)
                                    )
                            }
                        }
                    }
                    .padding(.horizontal)
                    .padding(.vertical, 8)
                }

                if let testResultText {
                    Text(testResultText)
                        .font(.caption)
                        .fontDesign(.monospaced)
                        .padding(10)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color(.systemGray6))
                        .cornerRadius(8)
                        .padding(.horizontal)
                        .padding(.bottom, 6)
                }

                // Capability List
                List(filteredCapabilities, id: \.id) { cap in
                    VStack(alignment: .leading, spacing: 6) {
                        HStack {
                            Image(systemName: cap.available ? "checkmark.circle.fill" : "exclamationmark.triangle.fill")
                                .foregroundColor(cap.available ? .green : .orange)
                            Text(cap.name)
                                .font(.headline)
                            Spacer()
                            Text("Risk \(cap.riskLevel.rawValue)")
                                .font(.caption2)
                                .bold()
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(riskColor(cap.riskLevel).opacity(0.2))
                                .foregroundColor(riskColor(cap.riskLevel))
                                .cornerRadius(4)
                        }

                        Text(cap.description)
                            .font(.caption)
                            .foregroundColor(.secondary)

                        HStack {
                            Text(cap.id)
                                .font(.caption2)
                                .fontDesign(.monospaced)
                                .foregroundColor(.gray)
                            Spacer()
                            if cap.available {
                                Button("Test") {
                                    Task {
                                        let cmd = PhoneControlCommand(
                                            commandId: "test_ios_\(Int(Date().timeIntervalSince1970))",
                                            capability: cap.id,
                                            parameters: [:],
                                            authorization: CommandAuthorization(agentId: "tester", approvalToken: "bypass")
                                        )
                                        let res = await engine.executeCommand(command: cmd)
                                        testResultText = "Executed \(cap.id): \(res.status.rawValue) (Verified: \(res.verification.verified)) in \(res.durationMs)ms"
                                    }
                                }
                                .buttonStyle(.borderedProminent)
                                .tint(.green)
                                .controlSize(.mini)
                            } else {
                                Text(cap.unavailabilityReason ?? "Sandbox Restricted")
                                    .font(.caption2)
                                    .foregroundColor(.orange)
                            }
                        }
                    }
                    .padding(.vertical, 4)
                }
            }
            .navigationTitle("Capability Explorer")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button(action: {
                        manifest = discovery.discoverCapabilities(deviceId: "ios_device")
                    }) {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
        }
    }

    private func riskColor(_ risk: RiskLevel) -> Color {
        switch risk {
        case .level0Info: return .green
        case .level1Ordinary: return .blue
        case .level2Sensitive: return .orange
        case .level3High: return .purple
        case .level4Critical: return .red
        }
    }
}
