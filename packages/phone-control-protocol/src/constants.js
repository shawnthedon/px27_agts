/**
 * Universal Phone Control Protocol Constants
 * Core specification definition for px27m platform
 */

export const PROTOCOL_VERSION = "1.0";
export const MODULE_IDENTIFIER = "px27m.phone-control";

export const RiskLevel = Object.freeze({
  LEVEL_0_INFO: 0,       // Non-sensitive device info (read-only, auto)
  LEVEL_1_ORDINARY: 1,   // Ordinary app launch, dialler prepare (policy controlled)
  LEVEL_2_SENSITIVE: 2,  // SMS send, mic record, storage read/write, notification read
  LEVEL_3_HIGH: 3,       // Screen taps/gestures, call initiation, external automation run
  LEVEL_4_CRITICAL: 4    // Device admin, emergency wipe/reset, destructive modifications
});

export const AutonomyProfile = Object.freeze({
  OBSERVER: "observer",       // Inspect permitted info only (Risk 0)
  ASSISTANT: "assistant",     // Execute ordinary approved operations (Risk 0-1)
  OPERATOR: "operator",       // Multi-step workflows within policy (Risk 0-2)
  POWER_USER: "power_user",   // Advanced automation, screen actions (Risk 0-3)
  RESTRICTED: "restricted"    // Whitelisted commands only
});

export const CommandStatus = Object.freeze({
  ACCEPTED: "accepted",
  QUEUED: "queued",
  AWAITING_APPROVAL: "awaiting_approval",
  RUNNING: "running",
  COMPLETED: "completed",
  PARTIALLY_COMPLETED: "partially_completed",
  FAILED: "failed",
  CANCELLED: "cancelled",
  TIMED_OUT: "timed_out",
  UNSUPPORTED: "unsupported",
  PERMISSION_REQUIRED: "permission_required"
});

export const PermissionStatus = Object.freeze({
  NOT_REQUIRED: "not_required",
  GRANTED: "granted",
  NOT_GRANTED: "not_granted",
  PERMANENTLY_DENIED: "permanently_denied",
  RESTRICTED_BY_OS: "restricted_by_os"
});

export const UnavailabilityReason = Object.freeze({
  UNSUPPORTED_PLATFORM_SANDBOX: "unsupported_platform_sandbox",
  MISSING_HARDWARE: "missing_hardware",
  MISSING_PERMISSION: "missing_permission",
  SERVICE_DISABLED: "service_disabled",
  POLICY_BLOCKED: "policy_blocked",
  DEVICE_LOCKED: "device_locked"
});

export const CapabilityCategory = Object.freeze({
  SCREEN: "screen",
  APPLICATION: "application",
  DEVICE_INFO: "device_info",
  CONNECTIVITY: "connectivity",
  TELEPHONY: "telephony",
  MESSAGING: "messaging",
  AUDIO: "audio",
  CAMERA: "camera",
  STORAGE: "storage",
  NOTIFICATIONS: "notifications",
  LOCATION: "location",
  SENSORS: "sensors",
  SETTINGS: "settings",
  AUTOMATION: "automation",
  SECURITY: "security"
});
