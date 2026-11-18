import Foundation
#if canImport(CryptoKit)
import CryptoKit
#endif
#if canImport(Network)
import Network
#endif

/// Hybrid Connectivity Manager for iOS
/// Implements dual-transport connectivity:
/// 1. Local LAN TCP daemon (port 27028) via NWListener / sockets
/// 2. Remote Gateway WebSocket client with HMAC-SHA256 signature authentication
public actor HybridConnectivityManager {

    public let deviceId: String
    private var sharedSecret: String?
    public private(set) var isGatewayConnected: Bool = false
    public private(set) var isLocalDaemonRunning: Bool = false

    private let discovery = CapabilityDiscovery()
    private let engine: ExecutionEngine
    private var webSocketTask: URLSessionWebSocketTask?

    public init(deviceId: String = "ios_\(UUID().uuidString.prefix(8))", engine: ExecutionEngine) {
        self.deviceId = deviceId
        self.engine = engine
    }

    public func setPairingSecret(_ secret: String) {
        self.sharedSecret = secret
    }

    // MARK: - 1. Local LAN Daemon (Port 27028)

    public func startLocalDaemon(port: UInt16 = 27028) {
        #if canImport(Network)
        do {
            let params = NWParameters.tcp
            params.allowLocalEndpointReuse = true
            let listener = try NWListener(using: params, on: NWEndpoint.Port(rawValue: port)!)
            listener.newConnectionHandler = { [weak self] connection in
                connection.start(queue: .global())
                self?.handleIncomingLanConnection(connection)
            }
            listener.start(queue: .global())
            isLocalDaemonRunning = true
        } catch {
            isLocalDaemonRunning = false
        }
        #else
        isLocalDaemonRunning = true
        #endif
    }

    public func stopLocalDaemon() {
        isLocalDaemonRunning = false
    }

    #if canImport(Network)
    private nonisolated func handleIncomingLanConnection(_ connection: NWConnection) {
        connection.receive(minimumIncompleteLength: 1, maximumLength: 65536) { [weak self] data, _, isComplete, _ in
            guard let self = self, let data = data, let jsonString = String(data: data, encoding: .utf8) else {
                return
            }
            Task {
                let response = await self.processIncomingPayload(jsonString)
                if let respData = response.data(using: .utf8) {
                    connection.send(content: respData, completion: .contentProcessed({ _ in
                        if isComplete { connection.cancel() }
                    }))
                }
            }
        }
    }
    #endif

    // MARK: - 2. Remote Gateway WebSocket Client

    public func connectToGateway(url: URL) {
        let session = URLSession(configuration: .default)
        let task = session.webSocketTask(with: url)
        self.webSocketTask = task
        task.resume()
        isGatewayConnected = true

        // Send initial registration and capability manifest
        sendManifestToGateway()
        startWebSocketListenLoop()
    }

    public func disconnectFromGateway() {
        webSocketTask?.cancel(with: .normalClosure, reason: nil)
        webSocketTask = nullTask()
        isGatewayConnected = false
    }

    private func nullTask() -> URLSessionWebSocketTask? {
        return nil
    }

    private func sendManifestToGateway() {
        guard let task = webSocketTask else { return }
        let manifest = discovery.discoverCapabilities(deviceId: deviceId)
        if let data = try? JSONEncoder().encode(manifest),
           let str = String(data: data, encoding: .utf8) {
            let msg = URLSessionWebSocketTask.Message.string(str)
            task.send(msg) { _ in }
        }
    }

    private func startWebSocketListenLoop() {
        guard let task = webSocketTask else { return }

        task.receive { [weak self] result in
            guard let self = self else { return }
            switch result {
            case .success(let message):
                let payload: String? = {
                    switch message {
                    case .string(let str): return str
                    case .data(let data): return String(data: data, encoding: .utf8)
                    @unknown default: return nil
                    }
                }()

                if let text = payload {
                    Task {
                        let response = await self.processIncomingPayload(text)
                        if let task = await self.webSocketTask {
                            task.send(.string(response)) { _ in }
                        }
                    }
                }
                // Continue loop
                Task {
                    await self.startWebSocketListenLoop()
                }

            case .failure:
                Task {
                    await self.handleGatewayDisconnect()
                }
            }
        }
    }

    private func handleGatewayDisconnect() {
        isGatewayConnected = false
    }

    // MARK: - Payload Processing & Cryptographic Verification

    public func processIncomingPayload(_ jsonString: String) async -> String {
        guard let data = jsonString.data(using: .utf8) else {
            return "{\"status\":\"failed\",\"error\":{\"message\":\"Invalid string encoding\"}}"
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
            return "{\"status\":\"failed\",\"error\":{\"message\":\"JSON does not match PhoneControlCommand schema\"}}"
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

        // 4. Execute through 10-component engine
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
