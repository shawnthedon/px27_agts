import XCTest
@testable import Px27PhoneControl

final class ProtocolAndEngineTests: XCTestCase {

    func testManifestDiscoveryHonorsTruthfulSandboxing() {
        let discovery = CapabilityDiscovery()
        let manifest = discovery.discoverCapabilities(deviceId: "ios_test_01")

        XCTAssertEqual(manifest.platform, "ios")
        XCTAssertEqual(manifest.module, "px27m.phone-control")

        // Screen inspect must be truthfully marked as unavailable due to sandbox
        let screenInspect = manifest.capabilities.first { $0.id == "device.screen.inspect" }
        XCTAssertNotNil(screenInspect)
        XCTAssertFalse(screenInspect!.available)
        XCTAssertEqual(screenInspect!.unavailabilityReason, UnavailabilityReason.unsupportedPlatformSandbox.rawValue)

        // Screen tap must also be truthfully marked as unavailable
        let screenTap = manifest.capabilities.first { $0.id == "device.screen.tap" }
        XCTAssertNotNil(screenTap)
        XCTAssertFalse(screenTap!.available)
        XCTAssertEqual(screenTap!.unavailabilityReason, UnavailabilityReason.unsupportedPlatformSandbox.rawValue)
    }

    func testUniversalProtocolEncodingAndDecoding() throws {
        let command = PhoneControlCommand(
            commandId: "cmd_ios_99",
            capability: "device.info.system",
            parameters: ["format": "json"],
            execution: CommandExecution(mode: "foreground", timeoutMs: 5000, verifyResult: true),
            authorization: CommandAuthorization(agentId: "agent_ios_01", autonomyProfile: .operatorProfile)
        )

        let encoder = JSONEncoder()
        let data = try encoder.encode(command)

        let decoder = JSONDecoder()
        let decoded = try decoder.decode(PhoneControlCommand.self, from: data)

        XCTAssertEqual(decoded.commandId, "cmd_ios_99")
        XCTAssertEqual(decoded.capability, "device.info.system")
        XCTAssertEqual(decoded.authorization.agentId, "agent_ios_01")
    }

    func testExecutionEngineEnforcesSandboxRestrictionsTruthfully() async {
        let engine = ExecutionEngine()
        let restrictedCmd = PhoneControlCommand(
            commandId: "cmd_bad_tap",
            capability: "device.screen.tap",
            parameters: ["x": "200", "y": "300"],
            authorization: CommandAuthorization(agentId: "agent_01", approvalToken: "bypass")
        )

        let result = await engine.executeCommand(command: restrictedCmd)
        XCTAssertEqual(result.status, .unsupported)
        XCTAssertFalse(result.verification.verified)
        XCTAssertEqual(result.error?.code, "PLATFORM_SANDBOX_RESTRICTION")
    }

    func testExecutionEngineEnforcesRiskLevel3ApprovalRequirement() async {
        let engine = ExecutionEngine()
        // Shortcut execution is Risk Level 3
        let unapprovedCmd = PhoneControlCommand(
            commandId: "cmd_shortcut_01",
            capability: "device.automation.run_workflow",
            parameters: ["name": "MorningRoutine"],
            authorization: CommandAuthorization(agentId: "agent_01", approvalToken: nil)
        )

        let result = await engine.executeCommand(command: unapprovedCmd)
        XCTAssertEqual(result.status, .awaitingApproval)
        XCTAssertFalse(result.verification.verified)
        XCTAssertEqual(result.error?.code, "APPROVAL_REQUIRED")
    }

    func testExecutionEngineExecutesSupportedInfoCapabilityWithVerification() async {
        let engine = ExecutionEngine()
        let infoCmd = PhoneControlCommand(
            commandId: "cmd_sys_01",
            capability: "device.info.system",
            parameters: [:],
            authorization: CommandAuthorization(agentId: "agent_observer", autonomyProfile: .observer)
        )

        let result = await engine.executeCommand(command: infoCmd)
        XCTAssertEqual(result.status, .completed)
        XCTAssertTrue(result.verification.verified)
        XCTAssertNotNil(result.result?["processor_count"])
    }
}
