/**
 * Px27mAgentClient
 * The single, secure, intelligent interface for px27m agents to discover
 * and operate phone capabilities across Android and iOS companion devices.
 */

import { validateCommand, CommandStatus, AutonomyProfile, RiskLevel } from "@px27m/phone-control-protocol";
import crypto from "node:crypto";

export class Px27mAgentClient {
  constructor({
    agentId,
    agentName = "px27m Agent",
    autonomyProfile = AutonomyProfile.OPERATOR,
    deviceRegistry,
    commandRouter,
    policyEngine,
    auditEngine
  }) {
    if (!agentId) throw new Error("agentId is required for Px27mAgentClient");
    this.agentId = agentId;
    this.agentName = agentName;
    this.autonomyProfile = autonomyProfile;
    this.deviceRegistry = deviceRegistry;
    this.commandRouter = commandRouter;
    this.policyEngine = policyEngine;
    this.auditEngine = auditEngine;
  }

  /**
   * Automatically discovers all capabilities across connected devices.
   * Returns a unified catalog of available capabilities with platform, availability, and risk metadata.
   */
  async discoverAvailableCapabilities(targetDeviceId = null) {
    const devices = targetDeviceId
      ? [this.deviceRegistry.getDevice(targetDeviceId)].filter(Boolean)
      : this.deviceRegistry.listConnectedDevices().filter(d => d.status === "connected");

    const available = [];
    for (const dev of devices) {
      const manifest = this.deviceRegistry.getManifest(dev.device_id);
      if (!manifest) continue;

      for (const cap of manifest.capabilities) {
        if (cap.available && cap.permission_status === "granted") {
          available.push({
            capability_id: cap.id,
            category: cap.category,
            name: cap.name,
            description: cap.description,
            platform: manifest.platform,
            device_id: dev.device_id,
            device_name: dev.device_name,
            risk_level: cap.risk_level,
            supports_background: cap.supports_background,
            parameters_schema: cap.parameters_schema || { type: "object", properties: {} }
          });
        }
      }
    }
    return available;
  }

  /**
   * Generates tool definitions formatted for OpenAI Function Calling (GPT-4o, etc.)
   */
  async getOpenAITools(targetDeviceId = null) {
    const caps = await this.discoverAvailableCapabilities(targetDeviceId);
    return caps.map(cap => ({
      type: "function",
      function: {
        name: `${cap.device_id.replace(/[^a-zA-Z0-9_]/g, "_")}__${cap.capability_id.replace(/\./g, "_")}`,
        description: `[${cap.platform.toUpperCase()} - ${cap.device_name}] ${cap.name}: ${cap.description}. Risk Level: ${cap.risk_level}`,
        parameters: cap.parameters_schema
      }
    }));
  }

  /**
   * Generates tool definitions formatted for Anthropic Claude Tools API
   */
  async getAnthropicTools(targetDeviceId = null) {
    const caps = await this.discoverAvailableCapabilities(targetDeviceId);
    return caps.map(cap => ({
      name: `${cap.device_id.replace(/[^a-zA-Z0-9_]/g, "_")}__${cap.capability_id.replace(/\./g, "_")}`,
      description: `[${cap.platform.toUpperCase()}] ${cap.name}: ${cap.description}`,
      input_schema: cap.parameters_schema
    }));
  }

  /**
   * Generates tool definitions formatted for Model Context Protocol (MCP)
   */
  async getMcpTools(targetDeviceId = null) {
    const caps = await this.discoverAvailableCapabilities(targetDeviceId);
    return caps.map(cap => ({
      name: `${cap.device_id.replace(/[^a-zA-Z0-9_]/g, "_")}__${cap.capability_id.replace(/\./g, "_")}`,
      description: `[${cap.platform.toUpperCase()}] ${cap.name}: ${cap.description}`,
      inputSchema: cap.parameters_schema
    }));
  }

  /**
   * Operates a discovered device capability through one secure, intelligent method.
   * Handles parameter normalization, signing, policy verification, routing, and state verification.
   */
  async executeCapability({
    deviceId,
    capabilityId,
    parameters = {},
    approvalToken = null,
    timeoutMs = 15000,
    verifyResult = true
  }) {
    const commandId = `cmd_${Date.now()}_${crypto.randomBytes(4).toString("hex")}`;

    const command = {
      command_id: commandId,
      capability: capabilityId,
      parameters: parameters,
      execution: {
        mode: "foreground",
        timeout_ms: timeoutMs,
        verify_result: verifyResult
      },
      authorization: {
        agent_id: this.agentId,
        agent_name: this.agentName,
        autonomy_profile: this.autonomyProfile,
        approval_token: approvalToken,
        timestamp_ms: Date.now()
      }
    };

    const val = validateCommand(command);
    if (!val.valid) {
      return {
        success: false,
        command_id: commandId,
        status: CommandStatus.FAILED,
        error: { code: "COMMAND_SCHEMA_INVALID", message: val.errors.join("; ") },
        verification: { verified: false }
      };
    }

    const executionResult = await this.commandRouter.dispatch(command, deviceId);

    return {
      success: executionResult.status === CommandStatus.COMPLETED,
      command_id: executionResult.command_id,
      status: executionResult.status,
      platform: executionResult.platform,
      capability: executionResult.capability,
      result: executionResult.result,
      verification: executionResult.verification,
      error: executionResult.error,
      duration_ms: executionResult.duration_ms
    };
  }

  /**
   * Helper to execute a tool call string produced by an LLM (e.g. from function calling)
   */
  async executeToolCall(toolName, parameters = {}, approvalToken = null) {
    const parts = toolName.split("__");
    if (parts.length < 2) {
      throw new Error(`Invalid tool name format: '${toolName}'. Expected 'deviceId__capabilityId'`);
    }
    const deviceId = parts[0];
    const capabilityId = parts.slice(1).join("__").replace(/_/g, ".");

    return this.executeCapability({
      deviceId,
      capabilityId,
      parameters,
      approvalToken
    });
  }

  /**
   * Executes a multi-step structured workflow sequence with verification and fail-fast protection.
   */
  async executeWorkflow(steps, deviceId, options = {}) {
    const results = [];
    const workflowId = `wf_${Date.now()}`;

    for (let i = 0; i < steps.length; i++) {
      const step = steps[i];
      const res = await this.executeCapability({
        deviceId,
        capabilityId: step.capability,
        parameters: step.parameters || {},
        approvalToken: step.approval_token || options.approvalToken || null,
        timeoutMs: step.timeout_ms || 15000,
        verifyResult: step.verify_result ?? true
      });

      results.push({
        step_index: i,
        capability: step.capability,
        response: res
      });

      if (!res.success && options.stopOnError !== false) {
        return {
          workflow_id: workflowId,
          completed: false,
          failed_at_step: i,
          reason: res.error?.message || "Step execution failed",
          step_results: results
        };
      }
    }

    return {
      workflow_id: workflowId,
      completed: true,
      step_results: results
    };
  }
}
