import Foundation
#if canImport(CryptoKit)
import CryptoKit
#endif

/// Hybrid Connectivity Manager for iOS
/// Manages local LAN communication and remote persistent WebSocket gateway connection.
public actor HybridConnectivityManager {

    public let deviceId: String
    private var sharedSecret: String?
    public private(set) var isGatewayConnected: Bool = false
    public private(set) var isLocalDaemonRunning: Bool = false

    private let discovery = CapabilityDiscovery()
    private let engine: ExecutionEngine

    public init(deviceId: String = "ios_\(UUID().uuidString.prefix(8))", engine: ExecutionEngine) {
        self.deviceId = deviceId
        self.engine = engine
    }

    public func setPairingSecret(_ secret: String) {
        self.sharedSecret = secret
    }

    public func startLocalDaemon(port: UInt16 = 27028) {
        isLocalDaemonRunning = true
    }

    public func stopLocalDaemon() {
        isLocalDaemonRunning = false
    }

    public func processIncomingPayload(_ jsonString: String) async -> String {
        guard let data = jsonString.data(using: .utf8) else {
            return "{\"status\":\"failed\",\"error\":\"Invalid string encoding\"}"
        }

        // 1. Check if discovery query
        if let root = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
           (root["action"] as? String == "discover" || root["type"] as? String == "manifest_request") {
            let manifest = discovery.discoverCapabilities(deviceId: deviceId)
            let encoder = JSONEncoder()
            encoder.outputFormatting = .prettyPrinted
            if let encoded = try? encoder.encode(manifest), let str = String(data: encoded, encoding: .utf8) {
                return str
            }
        }

        // 2. Decode Command
        let decoder = JSONDecoder()
        guard let command = try? decoder.decode(PhoneControlCommand.self, from: data) else {
            return "{\"status\":\"failed\",\"error\":\"JSON does not match PhoneControlCommand schema\"}"
        }

        // 3. Verify HMAC signature if secret configured
        #if canImport(CryptoKit)
        if let secret = sharedSecret, let signature = command.authorization.signature {
            let key = SymmetricKey(data: Data(secret.utf8))
            let calculated = HMAC<SHA256>.authenticationCode(for: Data(jsonString.utf8), using: key)
            let calculatedHex = calculated.map { String(format: "%02x", $0) }.joined()
            if calculatedHex.lowercased() != signature.lowercased() {
                let err = ExecutionResult(
                    commandId: command.commandId,
                    status: .failed,
                    capability: command.capability,
                    verification: VerificationInfo(verified: false, reason: "HMAC signature mismatch"),
                    durationMs: 0
                )
                let enc = try? JSONEncoder().encode(err)
                return enc != nil ? String(data: enc!, encoding: .utf8)! : "{}"
            }
        }
        #endif

        // 4. Execute through engine
        let result = await engine.executeCommand(command: command, deviceId: deviceId)
        let encoder = JSONEncoder()
        encoder.outputFormatting = .prettyPrinted
        if let encoded = try? encoder.encode(result), let str = String(data: encoded, encoding: .utf8) {
            return str
        }
        return "{\"status\":\"completed\"}"
    }

    public func getPublishedManifest() -> CapabilityManifest {
        discovery.discoverCapabilities(deviceId: deviceId)
    }
}
