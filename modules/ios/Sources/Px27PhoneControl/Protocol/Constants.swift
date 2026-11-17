import Foundation

/// Universal Phone Control Protocol Constants for iOS
public enum RiskLevel: Int, Codable, Sendable, Comparable {
    case level0Info = 0       // Non-sensitive device information
    case level1Ordinary = 1   // Ordinary application interaction
    case level2Sensitive = 2  // Sensitive data, camera, mic, documents
    case level3High = 3       // Calls, external shortcuts, high-impact actions
    case level4Critical = 4   // Device administration, emergency revocation

    public static func < (lhs: RiskLevel, rhs: RiskLevel) -> Bool {
        lhs.rawValue < rhs.rawValue
    }
}

public enum AutonomyProfile: String, Codable, Sendable {
    case observer = "observer"
    case assistant = "assistant"
    case operatorProfile = "operator"
    case powerUser = "power_user"
    case restricted = "restricted"
}

public enum CommandStatus: String, Codable, Sendable {
    case accepted = "accepted"
    case queued = "queued"
    case awaitingApproval = "awaiting_approval"
    case running = "running"
    case completed = "completed"
    case partiallyCompleted = "partially_completed"
    case failed = "failed"
    case cancelled = "cancelled"
    case timedOut = "timed_out"
    case unsupported = "unsupported"
    case permissionRequired = "permission_required"
}

public enum PermissionStatus: String, Codable, Sendable {
    case notRequired = "not_required"
    case granted = "granted"
    case notGranted = "not_granted"
    case permanentlyDenied = "permanently_denied"
    case restrictedByOS = "restricted_by_os"
}

public enum UnavailabilityReason: String, Codable, Sendable {
    case unsupportedPlatformSandbox = "unsupported_platform_sandbox"
    case missingHardware = "missing_hardware"
    case missingPermission = "missing_permission"
    case serviceDisabled = "service_disabled"
    case policyBlocked = "policy_blocked"
    case deviceLocked = "device_locked"
}
