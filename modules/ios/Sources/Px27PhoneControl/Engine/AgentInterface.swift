import Foundation

/// Px27mAgentClient for iOS
/// The unified, secure, intelligent interface for px27m agents to discover
/// and operate phone capabilities on iOS devices.
public actor Px27mAgentClient {

    public let agentId: String
    public let agentName: String
    public let autonomyProfile: AutonomyProfile
    private let engine: ExecutionEngine
    private let discovery = CapabilityDiscovery()

    public init(
        agentId: String,
        agentName: String = "px27m iOS Agent",
        autonomyProfile: AutonomyProfile = .operatorProfile,
        engine: ExecutionEngine = ExecutionEngine()
    ) {
        self.agentId = agentId
        self.agentName = agentName
        self.autonomyProfile = autonomyProfile
        self.engine = engine
    }

    /// Automatically discovers available capabilities on this iOS device
    public func discoverAvailableCapabilities(deviceId: String = "ios_device") -> [CapabilityDefinition] {
        let manifest = discovery.discoverCapabilities(deviceId: deviceId)
        return manifest.capabilities.filter { $0.available }
    }

    /// Operates a discovered capability through the secure interface with verified state return
    public func executeCapability(
        capabilityId: String,
        parameters: [String: String] = [:],
        approvalToken: String? = nil,
        timeoutMs: Int = 15000,
        deviceId: String = "ios_device"
    ) async -> ExecutionResult {
        let commandId = "cmd_\(Int(Date().timeIntervalSince1970))_\(UUID().uuidString.prefix(6))"

        let command = PhoneControlCommand(
            commandId: commandId,
            capability: capabilityId,
            parameters: parameters,
            execution: CommandExecution(mode: "foreground", timeoutMs: timeoutMs, verifyResult: true),
            authorization: CommandAuthorization(
                agentId: agentId,
                agentName: agentName,
                autonomyProfile: autonomyProfile,
                approvalToken: approvalToken
            )
        )

        return await engine.executeCommand(command: command, deviceId: deviceId)
    }

    /// Executes a sequence of workflow steps with verification and fail-fast handling
    public func executeWorkflow(
        steps: [(capability: String, parameters: [String: String])],
        approvalToken: String? = nil,
        deviceId: String = "ios_device"
    ) async -> (completed: Bool, results: [ExecutionResult]) {
        var results: [ExecutionResult] = []

        for step in steps {
            let res = await executeCapability(
                capabilityId: step.capability,
                parameters: step.parameters,
                approvalToken: approvalToken,
                deviceId: deviceId
            )
            results.append(res)

            if res.status != .completed {
                return (completed: false, results: results)
            }
        }

        return (completed: true, results: results)
    }
}
