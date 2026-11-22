import test from "node:test";
import assert from "node:assert/strict";
import {
  DeviceRegistry,
  PolicyEngine,
  PairingManager,
  AuditEngine,
  CommandRouter
} from "../src/index.js";
import { RiskLevel, AutonomyProfile, CommandStatus } from "@px27m/phone-control-protocol";

test("PairingManager initiates and completes device pairing", () => {
  const pairing = new PairingManager();
  const init = pairing.initiatePairing("Pixel 9 Pro");
  assert.ok(init.pairing_code);
  assert.equal(init.pairing_code.length, 6);

  const complete = pairing.completePairing(init.pairing_code, "dev_001", "android");
  assert.equal(complete.success, true);
  assert.equal(complete.device_id, "dev_001");
  assert.ok(complete.auth_token);
});

test("DeviceRegistry stores manifest and generates agent tool definitions", () => {
  const reg = new DeviceRegistry();
  reg.registerDevice({
    device_id: "dev_001",
    device_name: "Pixel 9 Pro",
    platform: "android",
    os_version: "Android 15"
  });

  const manifest = {
    module: "px27m.phone-control",
    platform: "android",
    module_version: "1.0.0",
    protocol_version: "1.0",
    device_id: "dev_001",
    capabilities: [
      {
        id: "device.application.launch",
        category: "application",
        name: "Launch Application",
        description: "Open installed app",
        supported_platforms: ["android", "ios"],
        available: true,
        permission_status: "granted",
        risk_level: RiskLevel.LEVEL_1_ORDINARY,
        parameters_schema: {
          type: "object",
          properties: { package_name: { type: "string" } },
          required: ["package_name"]
        }
      },
      {
        id: "device.screen.tap",
        category: "screen",
        name: "Tap Screen Element",
        description: "Click accessible node or coordinate",
        supported_platforms: ["android"],
        available: true,
        permission_status: "granted",
        risk_level: RiskLevel.LEVEL_3_HIGH
      }
    ]
  };

  reg.updateManifest(manifest);
  const tools = reg.getAgentTools("dev_001", AutonomyProfile.OPERATOR);
  assert.equal(tools.length, 2);
  assert.equal(tools[0].function.name, "device_application_launch");
  assert.equal(tools[0].metadata.platform, "android");
});

test("PolicyEngine enforces Risk Level 3 user approval requirement", () => {
  const policy = new PolicyEngine();
  const cap = {
    id: "device.screen.tap",
    available: true,
    permission_status: "granted",
    risk_level: RiskLevel.LEVEL_3_HIGH
  };

  const commandWithoutApproval = {
    command_id: "cmd_01",
    capability: "device.screen.tap",
    parameters: { x: 100, y: 200 },
    execution: { mode: "foreground", timeout_ms: 5000, verify_result: true },
    authorization: {
      agent_id: "agent_01",
      autonomy_profile: AutonomyProfile.OPERATOR
    }
  };

  const eval1 = policy.evaluateCommand(commandWithoutApproval, cap);
  assert.equal(eval1.allowed, false);
  assert.equal(eval1.requires_approval, true);

  const commandWithApproval = {
    ...commandWithoutApproval,
    authorization: {
      ...commandWithoutApproval.authorization,
      approval_token: "user_approved_token_abc"
    }
  };
  const eval2 = policy.evaluateCommand(commandWithApproval, cap);
  assert.equal(eval2.allowed, true);
});

test("CommandRouter handles command dispatch, verified results, and audit trail", async () => {
  const registry = new DeviceRegistry();
  const policy = new PolicyEngine();
  const audit = new AuditEngine();
  const router = new CommandRouter(registry, policy, audit);

  registry.registerDevice({
    device_id: "dev_001",
    platform: "android",
    os_version: "15"
  });

  registry.updateManifest({
    module: "px27m.phone-control",
    platform: "android",
    module_version: "1.0.0",
    protocol_version: "1.0",
    device_id: "dev_001",
    capabilities: [
      {
        id: "device.info.battery",
        category: "device_info",
        name: "Battery Status",
        description: "Get battery level and charging state",
        supported_platforms: ["android", "ios"],
        available: true,
        permission_status: "granted",
        risk_level: RiskLevel.LEVEL_0_INFO
      }
    ]
  });

  // Register mock transport handler
  router.registerTransport("dev_001", async (cmd) => {
    return {
      command_id: cmd.command_id,
      status: CommandStatus.COMPLETED,
      platform: "android",
      capability: cmd.capability,
      result: { level_pct: 88, charging: true },
      verification: { verified: true, verification_method: "battery_broadcast" },
      duration_ms: 45
    };
  });

  const cmd = {
    command_id: "cmd_bat_01",
    capability: "device.info.battery",
    parameters: {},
    execution: { mode: "foreground", timeout_ms: 3000, verify_result: true },
    authorization: {
      agent_id: "agent_observer",
      autonomy_profile: AutonomyProfile.OBSERVER
    }
  };

  const res = await router.dispatch(cmd, "dev_001");
  assert.equal(res.status, CommandStatus.COMPLETED);
  assert.equal(res.verification.verified, true);
  assert.equal(res.result.level_pct, 88);

  const trail = audit.getAuditTrail();
  assert.equal(trail.length, 1);
  assert.equal(trail[0].command_id, "cmd_bat_01");
  assert.equal(trail[0].status, CommandStatus.COMPLETED);
});
