/**
 * px27m Policy Engine
 * Enforces risk tiers, agent autonomy ceilings, and approval gates
 */

import { RiskLevel, AutonomyProfile } from "@px27m/phone-control-protocol";

export class PolicyEngine {
  constructor() {
    this.devicePolicies = new Map();
    this.emergencyDisconnectFlags = new Map();
  }

  setDevicePolicy(deviceId, policy) {
    this.devicePolicies.set(deviceId, {
      maxAutonomyAllowed: policy.maxAutonomyAllowed || AutonomyProfile.POWER_USER,
      requireApprovalForRisk3: policy.requireApprovalForRisk3 ?? true,
      requireApprovalForRisk4: policy.requireApprovalForRisk4 ?? true,
      blockedCapabilities: new Set(policy.blockedCapabilities || []),
      whitelistedAgents: new Set(policy.whitelistedAgents || [])
    });
  }

  triggerEmergencyDisconnect(deviceId) {
    this.emergencyDisconnectFlags.set(deviceId, true);
  }

  clearEmergencyDisconnect(deviceId) {
    this.emergencyDisconnectFlags.delete(deviceId);
  }

  /**
   * Evaluates if a command request is allowed to execute.
   * Returns: { allowed: boolean, requires_approval: boolean, reason?: string }
   */
  evaluateCommand(command, capabilityDef) {
    const deviceId = command.authorization?.device_id || "default";

    // 1. Check emergency disconnect
    if (this.emergencyDisconnectFlags.get(deviceId)) {
      return {
        allowed: false,
        requires_approval: false,
        reason: "Device is under emergency disconnect"
      };
    }

    const policy = this.devicePolicies.get(deviceId) || {
      maxAutonomyAllowed: AutonomyProfile.POWER_USER,
      requireApprovalForRisk3: true,
      requireApprovalForRisk4: true,
      blockedCapabilities: new Set(),
      whitelistedAgents: new Set()
    };

    // 2. Check capability blocklist
    if (policy.blockedCapabilities.has(command.capability)) {
      return {
        allowed: false,
        requires_approval: false,
        reason: `Capability '${command.capability}' is blocked by device policy`
      };
    }

    // 3. Check capability availability
    if (!capabilityDef.available) {
      return {
        allowed: false,
        requires_approval: false,
        reason: `Capability unavailable: ${capabilityDef.unavailability_reason || "unsupported"}`
      };
    }

    // 4. Check permissions
    if (capabilityDef.permission_status !== "granted" && capabilityDef.permission_status !== "not_required") {
      return {
        allowed: false,
        requires_approval: false,
        reason: `Permission required: ${capabilityDef.required_permissions?.join(", ") || "missing permission"}`
      };
    }

    // 5. Evaluate agent autonomy vs Risk Level
    const agentProfile = command.authorization.autonomy_profile || AutonomyProfile.RESTRICTED;
    const risk = capabilityDef.risk_level ?? RiskLevel.LEVEL_1_ORDINARY;
    const hasApproval = Boolean(command.authorization.approval_token);

    if (agentProfile === AutonomyProfile.OBSERVER && risk > RiskLevel.LEVEL_0_INFO) {
      return {
        allowed: false,
        requires_approval: false,
        reason: "Observer profile can only access non-sensitive info (Risk 0)"
      };
    }

    if (agentProfile === AutonomyProfile.ASSISTANT && risk > RiskLevel.LEVEL_1_ORDINARY) {
      return {
        allowed: false,
        requires_approval: false,
        reason: "Assistant profile cannot execute sensitive operations (Risk >= 2)"
      };
    }

    if (agentProfile === AutonomyProfile.OPERATOR && risk > RiskLevel.LEVEL_2_SENSITIVE && !hasApproval) {
      return {
        allowed: false,
        requires_approval: true,
        reason: "Operator profile requires approval for high-risk operations (Risk 3+)"
      };
    }

    // 6. Risk Level 3 & 4 checks
    if (risk === RiskLevel.LEVEL_3_HIGH && policy.requireApprovalForRisk3) {
      if (!hasApproval) {
        return {
          allowed: false,
          requires_approval: true,
          reason: "Risk Level 3 operation requires user approval"
        };
      }
    }

    if (risk === RiskLevel.LEVEL_4_CRITICAL) {
      if (!hasApproval) {
        return {
          allowed: false,
          requires_approval: true,
          reason: "Risk Level 4 operation strictly requires explicit user confirmation"
        };
      }
    }

    return { allowed: true, requires_approval: false };
  }
}
