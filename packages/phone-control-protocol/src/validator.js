/**
 * Lightweight JSON schema and protocol validation utilities
 */

import { RiskLevel, AutonomyProfile, CommandStatus } from "./constants.js";

export function validateCommand(command) {
  const errors = [];
  if (!command || typeof command !== "object") {
    return { valid: false, errors: ["Command must be an object"] };
  }
  if (!command.command_id || typeof command.command_id !== "string") {
    errors.push("Missing or invalid 'command_id'");
  }
  if (!command.capability || typeof command.capability !== "string" || !command.capability.startsWith("device.")) {
    errors.push("Missing or invalid 'capability' format (must start with 'device.')");
  }
  if (!command.parameters || typeof command.parameters !== "object") {
    errors.push("Missing or invalid 'parameters' object");
  }
  if (!command.execution || typeof command.execution !== "object") {
    errors.push("Missing 'execution' configuration");
  } else {
    if (!["foreground", "background", "best_effort"].includes(command.execution.mode)) {
      errors.push("Invalid execution.mode");
    }
    if (typeof command.execution.timeout_ms !== "number" || command.execution.timeout_ms < 500) {
      errors.push("Invalid execution.timeout_ms (must be >= 500)");
    }
    if (typeof command.execution.verify_result !== "boolean") {
      errors.push("Missing execution.verify_result boolean flag");
    }
  }
  if (!command.authorization || typeof command.authorization !== "object") {
    errors.push("Missing 'authorization' block");
  } else {
    if (!command.authorization.agent_id) {
      errors.push("Missing authorization.agent_id");
    }
    if (!Object.values(AutonomyProfile).includes(command.authorization.autonomy_profile)) {
      errors.push("Invalid authorization.autonomy_profile");
    }
  }

  return {
    valid: errors.length === 0,
    errors
  };
}

export function validateManifest(manifest) {
  const errors = [];
  if (!manifest || typeof manifest !== "object") {
    return { valid: false, errors: ["Manifest must be an object"] };
  }
  if (manifest.module !== "px27m.phone-control") {
    errors.push("Invalid module identifier");
  }
  if (!["android", "ios"].includes(manifest.platform)) {
    errors.push("Platform must be 'android' or 'ios'");
  }
  if (!Array.isArray(manifest.capabilities)) {
    errors.push("Missing 'capabilities' array");
  } else {
    manifest.capabilities.forEach((cap, idx) => {
      if (!cap.id || !cap.name || !cap.category) {
        errors.push(`Capability at index ${idx} missing required fields (id, name, category)`);
      }
      if (typeof cap.available !== "boolean") {
        errors.push(`Capability ${cap.id || idx} missing 'available' boolean`);
      }
      if (typeof cap.risk_level !== "number" || cap.risk_level < 0 || cap.risk_level > 4) {
        errors.push(`Capability ${cap.id || idx} invalid risk_level (must be 0-4)`);
      }
    });
  }
  return {
    valid: errors.length === 0,
    errors
  };
}

export function validateResult(result) {
  const errors = [];
  if (!result || typeof result !== "object") {
    return { valid: false, errors: ["Result must be an object"] };
  }
  if (!result.command_id) errors.push("Missing command_id");
  if (!Object.values(CommandStatus).includes(result.status)) {
    errors.push(`Invalid command status '${result.status}'`);
  }
  if (!["android", "ios"].includes(result.platform)) errors.push("Invalid platform");
  if (!result.capability) errors.push("Missing capability");
  if (!result.verification || typeof result.verification.verified !== "boolean") {
    errors.push("Missing or invalid 'verification.verified' boolean");
  }
  if (typeof result.duration_ms !== "number") errors.push("Missing duration_ms");

  return {
    valid: errors.length === 0,
    errors
  };
}
