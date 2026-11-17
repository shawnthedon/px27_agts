import Foundation

/// Universal Protocol Codable Models for iOS
public struct CapabilityDefinition: Codable, Sendable {
    public let id: String
    public let category: String
    public let name: String
    public let description: String
    public let supportedPlatforms: [String]
    public let available: BooleanLiteralType
    public let unavailabilityReason: String?
    public let permissionStatus: String
    public let requiredPermissions: [String]
    public let riskLevel: RiskLevel
    public let supportsBackground: Bool

    enum CodingKeys: String, CodingKey {
        case id
        case category
        case name
        case description
        case supportedPlatforms = "supported_platforms"
        case available
        case unavailabilityReason = "unavailability_reason"
        case permissionStatus = "permission_status"
        case requiredPermissions = "required_permissions"
        case riskLevel = "risk_level"
        case supportsBackground = "supports_background"
    }

    public init(
        id: String,
        category: String,
        name: String,
        description: String,
        supportedPlatforms: [String] = ["ios"],
        available: Bool,
        unavailabilityReason: String? = nil,
        permissionStatus: String,
        requiredPermissions: [String] = [],
        riskLevel: RiskLevel = .level1Ordinary,
        supportsBackground: Bool = false
    ) {
        self.id = id
        self.category = category
        self.name = name
        self.description = description
        self.supportedPlatforms = supportedPlatforms
        self.available = available
        self.unavailabilityReason = unavailabilityReason
        self.permissionStatus = permissionStatus
        self.requiredPermissions = requiredPermissions
        self.riskLevel = riskLevel
        self.supportsBackground = supportsBackground
    }
}

public struct CapabilityManifest: Codable, Sendable {
    public let module: String
    public let platform: String
    public let moduleVersion: String
    public let protocolVersion: String
    public let deviceId: String
    public let deviceName: String
    public let osVersion: String
    public let connectionMode: String
    public let capabilities: [CapabilityDefinition]

    enum CodingKeys: String, CodingKey {
        case module
        case platform
        case moduleVersion = "module_version"
        case protocolVersion = "protocol_version"
        case deviceId = "device_id"
        case deviceName = "device_name"
        case osVersion = "os_version"
        case connectionMode = "connection_mode"
        case capabilities
    }

    public init(
        module: String = "px27m.phone-control",
        platform: String = "ios",
        moduleVersion: String = "1.0.0",
        protocolVersion: String = "1.0",
        deviceId: String,
        deviceName: String,
        osVersion: String,
        connectionMode: String = "hybrid",
        capabilities: [CapabilityDefinition]
    ) {
        self.module = module
        self.platform = platform
        self.moduleVersion = moduleVersion
        self.protocolVersion = protocolVersion
        self.deviceId = deviceId
        self.deviceName = deviceName
        self.osVersion = osVersion
        self.connectionMode = connectionMode
        self.capabilities = capabilities
    }
}

public struct CommandExecution: Codable, Sendable {
    public let mode: String
    public let timeoutMs: Int
    public let verifyResult: Bool
    public let retryCount: Int

    enum CodingKeys: String, CodingKey {
        case mode
        case timeoutMs = "timeout_ms"
        case verifyResult = "verify_result"
        case retryCount = "retry_count"
    }

    public init(mode: String = "foreground", timeoutMs: Int = 15000, verifyResult: Bool = true, retryCount: Int = 0) {
        self.mode = mode
        self.timeoutMs = timeoutMs
        self.verifyResult = verifyResult
        self.retryCount = retryCount
    }
}

public struct CommandAuthorization: Codable, Sendable {
    public let agentId: String
    public let agentName: String?
    public let autonomyProfile: AutonomyProfile
    public let approvalToken: String?
    public let signature: String?
    public let timestampMs: Int64

    enum CodingKeys: String, CodingKey {
        case agentId = "agent_id"
        case agentName = "agent_name"
        case autonomyProfile = "autonomy_profile"
        case approvalToken = "approval_token"
        case signature
        case timestampMs = "timestamp_ms"
    }

    public init(
        agentId: String,
        agentName: String? = nil,
        autonomyProfile: AutonomyProfile = .operatorProfile,
        approvalToken: String? = nil,
        signature: String? = nil,
        timestampMs: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    ) {
        self.agentId = agentId
        self.agentName = agentName
        self.autonomyProfile = autonomyProfile
        self.approvalToken = approvalToken
        self.signature = signature
        self.timestampMs = timestampMs
    }
}

public struct PhoneControlCommand: Codable, Sendable {
    public let commandId: String
    public let capability: String
    public let parameters: [String: String]
    public let execution: CommandExecution
    public let authorization: CommandAuthorization

    enum CodingKeys: String, CodingKey {
        case commandId = "command_id"
        case capability
        case parameters
        case execution
        case authorization
    }

    public init(
        commandId: String,
        capability: String,
        parameters: [String: String] = [:],
        execution: CommandExecution = CommandExecution(),
        authorization: CommandAuthorization
    ) {
        self.commandId = commandId
        self.capability = capability
        self.parameters = parameters
        self.execution = execution
        self.authorization = authorization
    }
}

public struct VerificationInfo: Codable, Sendable {
    public let verified: Bool
    public let verificationMethod: String?
    public let reason: String?

    enum CodingKeys: String, CodingKey {
        case verified
        case verificationMethod = "verification_method"
        case reason
    }

    public init(verified: Bool, verificationMethod: String? = nil, reason: String? = nil) {
        self.verified = verified
        self.verificationMethod = verificationMethod
        self.reason = reason
    }
}

public struct ExecutionError: Codable, Sendable {
    public let code: String
    public let message: String
    public let recoverable: Bool

    public init(code: String, message: String, recoverable: Bool = false) {
        self.code = code
        self.message = message
        self.recoverable = recoverable
    }
}

public struct ExecutionResult: Codable, Sendable {
    public let commandId: String
    public let status: CommandStatus
    public let platform: String
    public let capability: String
    public let result: [String: String]?
    public let verification: VerificationInfo
    public let error: ExecutionError?
    public let durationMs: Int64
    public let timestampMs: Int64

    enum CodingKeys: String, CodingKey {
        case commandId = "command_id"
        case status
        case platform
        case capability
        case result
        case verification
        case error
        case durationMs = "duration_ms"
        case timestampMs = "timestamp_ms"
    }

    public init(
        commandId: String,
        status: CommandStatus,
        platform: String = "ios",
        capability: String,
        result: [String: String]? = nil,
        verification: VerificationInfo,
        error: ExecutionError? = nullLiteral,
        durationMs: Int64,
        timestampMs: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    ) {
        self.commandId = commandId
        self.status = status
        self.platform = platform
        self.capability = capability
        self.result = result
        self.verification = verification
        self.error = error
        self.durationMs = durationMs
        self.timestampMs = timestampMs
    }
}

private let nullLiteral: ExecutionError? = nil
