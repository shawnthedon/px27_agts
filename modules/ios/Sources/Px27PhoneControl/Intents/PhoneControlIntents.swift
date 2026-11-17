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
    }
}
#endif
