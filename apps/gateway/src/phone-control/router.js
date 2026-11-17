/**
 * px27m Command Router
 * Dispatches commands to connected devices via hybrid connectivity (local LAN / remote gateway)
 */

import { validateCommand, CommandStatus } from "@px27m/phone-control-protocol";

export class CommandRouter {
  constructor(deviceRegistry, policyEngine, auditEngine) {
    this.deviceRegistry = deviceRegistry;
    this.policyEngine = policyEngine;
    this.auditEngine = auditEngine;
    this.deviceTransports = new Map(); // deviceId -> transport function/socket
  }

  registerTransport(deviceId, transportHandler) {
    this.deviceTransports.set(deviceId, transportHandler);
  }

  unregisterTransport(deviceId) {
    this.deviceTransports.delete(deviceId);
  }

  async dispatch(command, deviceId) {
    const startTime = Date.now();
    const val = validateCommand(command);
    if (!val.valid) {
      const result = {
        command_id: command?.command_id || "unknown",
        status: CommandStatus.FAILED,
        platform: "unknown",
        capability: command?.capability || "unknown",
        result: null,
        verification: { verified: false },
        error: { code: "VALIDATION_ERROR", message: val.errors.join("; ") },
        duration_ms: Date.now() - startTime
      };
      return result;
    }

    const device = this.deviceRegistry.getDevice(deviceId);
    if (!device) {
      return {
        command_id: command.command_id,
        status: CommandStatus.FAILED,
        platform: "unknown",
        capability: command.capability,
        result: null,
        verification: { verified: false },
        error: { code: "DEVICE_OFFLINE", message: `Device ${deviceId} is not registered or connected` },
        duration_ms: Date.now() - startTime
      };
    }

    const manifest = this.deviceRegistry.getManifest(deviceId);
    const capabilityDef = manifest?.capabilities.find(c => c.id === command.capability);
    if (!capabilityDef) {
      const result = {
        command_id: command.command_id,
        status: CommandStatus.UNSUPPORTED,
        platform: device.platform,
        capability: command.capability,
        result: null,
        verification: { verified: false },
        error: { code: "CAPABILITY_NOT_FOUND", message: `Capability ${command.capability} is not advertised by device` },
        duration_ms: Date.now() - startTime
      };
      this.auditEngine.logExecution({
        command_id: command.command_id,
        capability: command.capability,
        agent_id: command.authorization.agent_id,
        autonomy_profile: command.authorization.autonomy_profile,
        platform: device.platform,
        status: result.status,
        verified: false,
        duration_ms: result.duration_ms,
        error: result.error
      });
      return result;
    }

    // Policy check
    const policyResult = this.policyEngine.evaluateCommand(command, capabilityDef);
    if (!policyResult.allowed) {
      const status = policyResult.requires_approval
        ? CommandStatus.AWAITING_APPROVAL
        : (capabilityDef.permission_status !== "granted" ? CommandStatus.PERMISSION_REQUIRED : CommandStatus.FAILED);

      const result = {
        command_id: command.command_id,
        status,
        platform: device.platform,
        capability: command.capability,
        result: null,
        verification: { verified: false },
        error: { code: "POLICY_VIOLATION", message: policyResult.reason },
        duration_ms: Date.now() - startTime
      };

      this.auditEngine.logExecution({
        command_id: command.command_id,
        capability: command.capability,
        agent_id: command.authorization.agent_id,
        autonomy_profile: command.authorization.autonomy_profile,
        platform: device.platform,
        status: result.status,
        verified: false,
        duration_ms: result.duration_ms,
        error: result.error
      });
      return result;
    }

    // Dispatch via registered transport
    const transport = this.deviceTransports.get(deviceId);
    if (!transport) {
      return {
        command_id: command.command_id,
        status: CommandStatus.FAILED,
        platform: device.platform,
        capability: command.capability,
        result: null,
        verification: { verified: false },
        error: { code: "TRANSPORT_UNAVAILABLE", message: "No active local or gateway transport for device" },
        duration_ms: Date.now() - startTime
      };
    }

    try {
      const executionResult = await transport(command);
      this.auditEngine.logExecution({
        command_id: command.command_id,
        capability: command.capability,
        agent_id: command.authorization.agent_id,
        autonomy_profile: command.authorization.autonomy_profile,
        platform: device.platform,
        status: executionResult.status,
        verified: executionResult.verification?.verified ?? false,
        verification_method: executionResult.verification?.verification_method,
        duration_ms: executionResult.duration_ms || (Date.now() - startTime),
        error: executionResult.error
      });
      return executionResult;
    } catch (err) {
      const errResult = {
        command_id: command.command_id,
        status: CommandStatus.FAILED,
        platform: device.platform,
        capability: command.capability,
        result: null,
        verification: { verified: false },
        error: { code: "EXECUTION_EXCEPTION", message: err.message },
        duration_ms: Date.now() - startTime
      };
      this.auditEngine.logExecution({
        command_id: command.command_id,
        capability: command.capability,
        agent_id: command.authorization.agent_id,
        autonomy_profile: command.authorization.autonomy_profile,
        platform: device.platform,
        status: CommandStatus.FAILED,
        verified: false,
        duration_ms: Date.now() - startTime,
        error: errResult.error
      });
      return errResult;
    }
  }
}
