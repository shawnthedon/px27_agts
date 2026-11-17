import Foundation

/// 10-Component Power-User Execution Engine for iOS
/// Conforms strictly to Section 4 of px27m specification using modern Swift Concurrency.
public actor ExecutionEngine {

    private let discovery = CapabilityDiscovery()
    private let handlers = CapabilityHandlers()
    public private(set) var auditLog: [ExecutionResult] = []
    public var maxAutonomy: AutonomyProfile = .powerUser
    public var isEmergencyDisconnected: BooleanLiteralType = false

    public init() {}

    public func executeCommand(command: PhoneControlCommand, deviceId: String = "local_ios") async -> ExecutionResult {
        let startTime = Date()

        // 1. Resolve Capability
        let manifest = discovery.discoverCapabilities(deviceId: deviceId)
        guard let capDef = manifest.capabilities.first(where: { $0.id == command.capability }) else {
            let res = ExecutionResult(
                commandId: command.commandId,
                status: .unsupported,
                capability: command.capability,
                verification: VerificationInfo(verified: false, reason: "Capability not advertised by iOS module"),
                error: ExecutionError(code: "UNKNOWN_CAPABILITY", message: "Capability \(command.capability) not found"),
                durationMs: Int64(Date().timeIntervalSince(startTime) * 1000)
            )
            appendAudit(res)
            return res
        }

        // 2. Check Emergency Disconnect
        if isEmergencyDisconnected {
            let res = ExecutionResult(
                commandId: command.commandId,
                status: .failed,
                capability: command.capability,
                verification: VerificationInfo(verified: false, reason: "Emergency disconnect active"),
                error: ExecutionError(code: "DISCONNECTED", message: "Device is disconnected"),
                durationMs: Int64(Date().timeIntervalSince(startTime) * 1000)
            )
            appendAudit(res)
            return res
        }

        // 3. Check Platform Sandbox Availability
        if !capDef.available {
            let res = ExecutionResult(
                commandId: command.commandId,
                status: .unsupported,
                capability: command.capability,
                verification: VerificationInfo(verified: false, reason: capDef.unavailabilityReason ?? "Unavailable on iOS"),
                error: ExecutionError(code: "PLATFORM_SANDBOX_RESTRICTION", message: capDef.description),
                durationMs: Int64(Date().timeIntervalSince(startTime) * 1000)
            )
            appendAudit(res)
            return res
        }

        // 4. Policy Check
        let risk = capDef.riskLevel
        let agentAutonomy = command.authorization.autonomyProfile

        if agentAutonomy == .observer && risk > .level0Info {
            let res = ExecutionResult(
                commandId: command.commandId,
                status: .failed,
                capability: command.capability,
                verification: VerificationInfo(verified: false, reason: "Observer profile is restricted to non-sensitive info"),
                error: ExecutionError(code: "POLICY_VIOLATION", message: "Observer profile cannot invoke Risk > 0"),
                durationMs: Int64(Date().timeIntervalSince(startTime) * 1000)
            )
            appendAudit(res)
            return res
        }

        // 5. Approval Gate (Risk 3 & 4)
        if risk >= .level3High && (command.authorization.approvalToken == nil || command.authorization.approvalToken!.isEmpty) {
            let res = ExecutionResult(
                commandId: command.commandId,
                status: .awaitingApproval,
                capability: command.capability,
                verification: VerificationInfo(verified: false, reason: "Risk \(risk.rawValue) operation requires explicit user approval"),
                error: ExecutionError(code: "APPROVAL_REQUIRED", message: "Approval token required for high-risk action"),
                durationMs: Int64(Date().timeIntervalSince(startTime) * 1000)
            )
            appendAudit(res)
            return res
        }

        // 6. Workflow Executor (with Timeout)
        let timeoutNanos = UInt64(max(500, command.execution.timeoutMs)) * 1_000_000

        do {
            let result = try await withThrowingTaskGroup(of: ExecutionResult.self) { group in
                group.addTask {
                    await self.handlers.execute(command: command)
                }
                group.addTask {
                    try await Task.sleep(nanoseconds: timeoutNanos)
                    throw ExecutionTimeoutError()
                }

                let firstCompleted = try await group.next()!
                group.cancelAll()
                return firstCompleted
            }
            appendAudit(result)
            return result
        } catch is ExecutionTimeoutError {
            let elapsed = Int64(Date().timeIntervalSince(startTime) * 1000)
            let res = ExecutionResult(
                commandId: command.commandId,
                status: .timedOut,
                capability: command.capability,
                verification: VerificationInfo(verified: false, reason: "Command execution timed out after \(command.execution.timeoutMs)ms"),
                error: ExecutionError(code: "TIMEOUT", message: "Execution timed out"),
                durationMs: elapsed
            )
            appendAudit(res)
            return res
        } catch {
            let elapsed = Int64(Date().timeIntervalSince(startTime) * 1000)
            let res = ExecutionResult(
                commandId: command.commandId,
                status: .failed,
                capability: command.capability,
                verification: VerificationInfo(verified: false, reason: error.localizedDescription),
                error: ExecutionError(code: "EXECUTION_EXCEPTION", message: error.localizedDescription),
                durationMs: elapsed
            )
            appendAudit(res)
            return res
        }
    }

    private func appendAudit(_ result: ExecutionResult) {
        auditLog.insert(result, at: 0)
        if auditLog.count > 200 {
            auditLog.removeLast()
        }
    }
}

public struct ExecutionTimeoutError: Error {}
