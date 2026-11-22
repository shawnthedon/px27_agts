import Foundation
#if canImport(AppIntents)
import AppIntents

/// App Intent allowing Siri, Shortcuts, and px27m agents to query device status
public struct QueryDeviceStatusIntent: AppIntent {
    public static var title: LocalizedStringResource = "Query px27m Device Status"
    public static var description = IntentDescription("Returns device telemetry including battery and system version to px27m agents.")

    public init() {}

    public func perform() async throws -> some IntentResult & ReturnsValue<String> {
        let processInfo = ProcessInfo.processInfo
        let status = "iOS \(processInfo.operatingSystemVersionString) • Cores: \(processInfo.activeProcessorCount) • Memory: \(processInfo.physicalMemory / (1024 * 1024)) MB"
        return .result(value: status)
    }
}

/// App Intent allowing authorized execution of universal phone commands
public struct ExecutePhoneCommandIntent: AppIntent {
    public static var title: LocalizedStringResource = "Execute px27m Phone Command"
    public static var description = IntentDescription("Executes an authorized universal phone control command through px27m engine.")

    @Parameter(title: "Capability ID")
    public var capabilityId: String

    @Parameter(title: "Command ID", default: "intent_cmd")
    public var commandId: String

    public init() {}

    public func perform() async throws -> some IntentResult & ReturnsValue<String> {
        let engine = ExecutionEngine()
        let cmd = PhoneControlCommand(
            commandId: commandId,
            capability: capabilityId,
            parameters: [:],
            authorization: CommandAuthorization(agentId: "apple_shortcuts_agent", approvalToken: "intent_approved")
        )
        let result = await engine.executeCommand(command: cmd)
        return .result(value: "Status: \(result.status.rawValue), Verified: \(result.verification.verified)")
    }
}

/// App Intent to toggle device flashlight / torch
public struct ToggleTorchIntent: AppIntent {
    public static var title: LocalizedStringResource = "Toggle px27m Flashlight"
    public static var description = IntentDescription("Turns device camera flashlight on or off via px27m engine.")

    @Parameter(title: "State", default: "toggle")
    public var state: String

    public init() {}

    public func perform() async throws -> some IntentResult & ReturnsValue<String> {
        let engine = ExecutionEngine()
        let cmd = PhoneControlCommand(
            commandId: "intent_torch_\(Int(Date().timeIntervalSince1970))",
            capability: "device.camera.torch",
            parameters: ["state": state],
            authorization: CommandAuthorization(agentId: "apple_shortcuts_agent")
        )
        let result = await engine.executeCommand(command: cmd)
        return .result(value: result.verification.verified ? "Torch updated: \(state)" : "Torch command failed")
    }
}

/// App Intent for Emergency Session Disconnect (Kill-Switch)
public struct EmergencyDisconnectIntent: AppIntent {
    public static var title: LocalizedStringResource = "Emergency Disconnect px27m"
    public static var description = IntentDescription("Immediately revokes all session authorizations and terminates remote gateway connection.")

    public init() {}

    public func perform() async throws -> some IntentResult & ReturnsValue<String> {
        let engine = ExecutionEngine()
        let cmd = PhoneControlCommand(
            commandId: "intent_kill_\(Int(Date().timeIntervalSince1970))",
            capability: "device.security.disconnect",
            parameters: [:],
            authorization: CommandAuthorization(agentId: "user_emergency_kill_switch")
        )
        let result = await engine.executeCommand(command: cmd)
        return .result(value: "Emergency Disconnect Executed: \(result.status.rawValue)")
    }
}

/// App Intent to analyze a phone number using local OSINT rules
public struct AnalyzePhoneNumberIntent: AppIntent {
    public static var title: LocalizedStringResource = "Analyze Phone Intelligence"
    public static var description = IntentDescription("Parses phone number to E.164, identifies country, and returns OSINT queries.")

    @Parameter(title: "Phone Number")
    public var phoneNumber: String

    public init() {}

    public func perform() async throws -> some IntentResult & ReturnsValue<String> {
        let digits = phoneNumber.filter { "0123456789".contains($0) }
        guard digits.count >= 7 else {
            return .result(value: "Invalid phone number: minimum 7 digits required")
        }
        let e164 = "+\(digits)"
        let country = digits.hasPrefix("1") ? "US/CA (+1)" : (digits.hasPrefix("44") ? "UK (+44)" : "International (\(e164))")
        return .result(value: "E.164: \(e164) • Country: \(country) • Dorks ready")
    }
}

/// System-wide AppShortcuts provider exposing px27m actions to iOS Shortcuts
public struct Px27ShortcutsProvider: AppShortcutsProvider {
    public static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: QueryDeviceStatusIntent(),
            phrases: [
                "Query \(.applicationName) status",
                "Check phone telemetry with \(.applicationName)"
            ],
            shortTitle: "Device Status",
            systemImageName: "iphone.gen3"
        )
        AppShortcut(
            intent: ToggleTorchIntent(),
            phrases: [
                "Toggle flashlight with \(.applicationName)",
                "Turn torch on with \(.applicationName)"
            ],
            shortTitle: "Toggle Torch",
            systemImageName: "flashlight.on.fill"
        )
        AppShortcut(
            intent: EmergencyDisconnectIntent(),
            phrases: [
                "Emergency disconnect \(.applicationName)",
                "Kill all \(.applicationName) connections"
            ],
            shortTitle: "Kill Switch",
            systemImageName: "bolt.slash.fill"
        )
    }
}
#endif
