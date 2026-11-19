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

    func testPx27mAgentClientDiscoversAndExecutesCapabilities() async {
        let client = Px27mAgentClient(
            agentId: "test_agent_ios",
            autonomyProfile: .operatorProfile
        )

        let caps = await client.discoverAvailableCapabilities()
        XCTAssertFalse(caps.isEmpty)
        XCTAssertTrue(caps.allSatisfy { $0.available })

        let result = await client.executeCapability(capabilityId: "device.info.system")
        XCTAssertEqual(result.status, .completed)
        XCTAssertTrue(result.verification.verified)
    }

    func testHybridConnectivityProcessesIncomingPayloadWithTruthfulVerification() async {
        let engine = ExecutionEngine()
        let manager = HybridConnectivityManager(deviceId: "ios_test_hybrid", engine: engine)

        await manager.startLocalDaemon(port: 27028)
        let isRunning = await manager.isLocalDaemonRunning
        XCTAssertTrue(isRunning)

        let payloadJson = """
        {
          "command_id": "cmd_hybrid_01",
          "capability": "device.info.system",
          "parameters": {},
          "execution": { "mode": "foreground", "timeout_ms": 5000, "verify_result": true },
          "authorization": { "agent_id": "agent_hybrid", "autonomy_profile": "operator" }
        }
        """

        let response = await manager.processIncomingPayload(payloadJson)
        XCTAssertTrue(response.contains("completed") || response.contains("verified"))

        await manager.stopLocalDaemon()
        let isStopped = await manager.isLocalDaemonRunning
        XCTAssertFalse(isStopped)
    }

    func testNativeIOSHandlersExecuteSupportedCapabilitiesWithVerification() async {
        let handlers = CapabilityHandlers()

        let ttsCmd = PhoneControlCommand(
            commandId: "cmd_tts_01",
            capability: "device.audio.tts",
            parameters: ["text": "Testing TTS synthesis", "language": "en-US"]
        )
        let ttsRes = await handlers.execute(command: ttsCmd)
        XCTAssertEqual(ttsRes.status, .completed)
        XCTAssertTrue(ttsRes.verification.verified)

        let torchCmd = PhoneControlCommand(
            commandId: "cmd_torch_01",
            capability: "device.camera.torch",
            parameters: ["state": "on"]
        )
        let torchRes = await handlers.execute(command: torchCmd)
        XCTAssertEqual(torchRes.status, .completed)
        XCTAssertTrue(torchRes.verification.verified)

        let vibrateCmd = PhoneControlCommand(
            commandId: "cmd_vibrate_01",
            capability: "device.sensors.vibrate",
            parameters: ["style": "medium"]
        )
        let vibrateRes = await handlers.execute(command: vibrateCmd)
        XCTAssertEqual(vibrateRes.status, .completed)
        XCTAssertTrue(vibrateRes.verification.verified)

        let shareCmd = PhoneControlCommand(
            commandId: "cmd_share_01",
            capability: "device.messaging.share",
            parameters: ["text": "Share text"]
        )
        let shareRes = await handlers.execute(command: shareCmd)
        XCTAssertEqual(shareRes.status, .completed)
        XCTAssertTrue(shareRes.verification.verified)
    }

    func testNativeIOSHandlersRejectRestrictedCapabilitiesWithTruthfulReasons() async {
        let handlers = CapabilityHandlers()

        let smsCmd = PhoneControlCommand(
            commandId: "cmd_sms_01",
            capability: "device.messaging.send_sms",
            parameters: ["to": "+15550199", "message": "hello"]
        )
        let smsRes = await handlers.execute(command: smsCmd)
        XCTAssertEqual(smsRes.status, .unsupported)
        XCTAssertFalse(smsRes.verification.verified)
        XCTAssertEqual(smsRes.error?.code, "UNSUPPORTED_PLATFORM_SANDBOX")

        let notifCmd = PhoneControlCommand(
            commandId: "cmd_notif_01",
            capability: "device.notifications.list",
            parameters: [:]
        )
        let notifRes = await handlers.execute(command: notifCmd)
        XCTAssertEqual(notifRes.status, .unsupported)
        XCTAssertFalse(notifRes.verification.verified)
        XCTAssertEqual(notifRes.error?.code, "UNSUPPORTED_PLATFORM_SANDBOX")

        let volCmd = PhoneControlCommand(
            commandId: "cmd_vol_01",
            capability: "device.audio.volume",
            parameters: ["level": "80"]
        )
        let volRes = await handlers.execute(command: volCmd)
        XCTAssertEqual(volRes.status, .unsupported)
        XCTAssertFalse(volRes.verification.verified)
        XCTAssertEqual(volRes.error?.code, "UNSUPPORTED_PLATFORM_SANDBOX")
    }

    #if canImport(AppIntents)
    func testAppIntentsExecutionAndFormatting() async throws {
        let statusIntent = QueryDeviceStatusIntent()
        let statusResult = try await statusIntent.perform()
        XCTAssertTrue(statusResult.value.contains("iOS"))

        var phoneIntent = AnalyzePhoneNumberIntent()
        phoneIntent.phoneNumber = "+1 555-0199"
        let phoneResult = try await phoneIntent.perform()
        XCTAssertTrue(phoneResult.value.contains("+15550199"))
        XCTAssertTrue(phoneResult.value.contains("US/CA"))

        let disconnectIntent = EmergencyDisconnectIntent()
        let killResult = try await disconnectIntent.perform()
        XCTAssertTrue(killResult.value.contains("Emergency Disconnect Executed"))
    }
    #endif
}
