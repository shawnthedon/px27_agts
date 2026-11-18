import test from "node:test";
import assert from "node:assert/strict";
import {
  DeviceRegistry,
  PolicyEngine,
  PairingManager,
  AuditEngine,
  CommandRouter,
  Px27mAgentClient,
  HybridGatewayServer
} from "../src/index.js";
import { RiskLevel, AutonomyProfile, CommandStatus } from "@px27m/phone-control-protocol";

test("Px27mAgentClient automatically discovers capabilities and produces LLM tools", async () => {
  const registry = new DeviceRegistry();
  const policy = new PolicyEngine();
  const audit = new AuditEngine();
  const router = new CommandRouter(registry, policy, audit);

  registry.registerDevice({
    device_id: "android_pixel",
    device_name: "Pixel 9 Pro",
    platform: "android",
    os_version: "Android 15"
  });

  registry.updateManifest({
    module: "px27m.phone-control",
    platform: "android",
    module_version: "1.0.0",
    protocol_version: "1.0",
    device_id: "android_pixel",
    capabilities: [
      {
        id: "device.application.launch",
        category: "application",
        name: "Launch Application",
        description: "Launch an installed app by package name",
        supported_platforms: ["android"],
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
        id: "device.info.battery",
        category: "device_info",
        name: "Battery Telemetry",
        description: "Inspect battery charge level",
        supported_platforms: ["android"],
        available: true,
        permission_status: "granted",
        risk_level: RiskLevel.LEVEL_0_INFO
      }
    ]
  });

  const agentClient = new Px27mAgentClient({
    agentId: "agent_copilot_01",
    agentName: "Pocket Copilot",
    autonomyProfile: AutonomyProfile.OPERATOR,
    deviceRegistry: registry,
    commandRouter: router,
    policyEngine: policy,
    auditEngine: audit
  });

  // 1. Auto-discover available capabilities
  const discovered = await agentClient.discoverAvailableCapabilities();
  assert.equal(discovered.length, 2);
  assert.equal(discovered[0].device_id, "android_pixel");
  assert.equal(discovered[0].capability_id, "device.application.launch");

  // 2. Generate OpenAI Tools
  const openAiTools = await agentClient.getOpenAITools();
  assert.equal(openAiTools.length, 2);
  assert.ok(openAiTools[0].function.name.includes("android_pixel"));
  assert.equal(openAiTools[0].type, "function");

  // 3. Generate Anthropic Tools
  const anthropicTools = await agentClient.getAnthropicTools();
  assert.equal(anthropicTools.length, 2);
  assert.ok(anthropicTools[1].name.includes("device_info_battery"));

  // 4. Generate MCP Tools
  const mcpTools = await agentClient.getMcpTools();
  assert.equal(mcpTools.length, 2);
  assert.ok(mcpTools[0].inputSchema);
});

test("Px27mAgentClient operates discovered capability through secure interface with verified result", async () => {
  const registry = new DeviceRegistry();
  const policy = new PolicyEngine();
  const audit = new AuditEngine();
  const router = new CommandRouter(registry, policy, audit);

  registry.registerDevice({
    device_id: "iphone_16",
    device_name: "iPhone 16 Pro",
    platform: "ios",
    os_version: "iOS 18"
  });

  registry.updateManifest({
    module: "px27m.phone-control",
    platform: "ios",
    module_version: "1.0.0",
    protocol_version: "1.0",
    device_id: "iphone_16",
    capabilities: [
      {
        id: "device.info.system",
        category: "device_info",
        name: "System Info",
        description: "Hardware telemetry",
        supported_platforms: ["ios"],
        available: true,
        permission_status: "granted",
        risk_level: RiskLevel.LEVEL_0_INFO
      }
    ]
  });

  router.registerTransport("iphone_16", async (cmd) => {
    return {
      command_id: cmd.command_id,
      status: CommandStatus.COMPLETED,
      platform: "ios",
      capability: cmd.capability,
      result: { os_version: "iOS 18.1", cores: "6" },
      verification: { verified: true, verification_method: "uidevice_telemetry" },
      duration_ms: 18
    };
  });

  const agentClient = new Px27mAgentClient({
    agentId: "strike_agent_007",
    autonomyProfile: AutonomyProfile.OPERATOR,
    deviceRegistry: registry,
    commandRouter: router,
    policyEngine: policy,
    auditEngine: audit
  });

  const res = await agentClient.executeCapability({
    deviceId: "iphone_16",
    capabilityId: "device.info.system"
  });

  assert.equal(res.success, true);
  assert.equal(res.status, CommandStatus.COMPLETED);
  assert.equal(res.verification.verified, true);
  assert.equal(res.result.os_version, "iOS 18.1");
});

test("HybridGatewayServer handles health, pairing, manifest registration, and commands", async () => {
  const registry = new DeviceRegistry();
  const policy = new PolicyEngine();
  const audit = new AuditEngine();
  const pairing = new PairingManager();
  const router = new CommandRouter(registry, policy, audit);

  const server = new HybridGatewayServer({
    deviceRegistry: registry,
    commandRouter: router,
    pairingManager: pairing,
    auditEngine: audit,
    policyEngine: policy
  });

  const { port } = await server.start(0, "127.0.0.1");

  try {
    // 1. Check health
    const healthRes = await fetch(`http://127.0.0.1:${port}/health`);
    assert.equal(healthRes.status, 200);
    const healthData = await healthRes.json();
    assert.equal(healthData.status, "healthy");

    // 2. Initiate pairing
    const pairRes = await fetch(`http://127.0.0.1:${port}/api/pair/init`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ device_name: "Test Android" })
    });
    assert.equal(pairRes.status, 200);
    const pairData = await pairRes.json();
    assert.ok(pairData.pairing_code);

    // 3. Complete pairing
    const compRes = await fetch(`http://127.0.0.1:${port}/api/pair/complete`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        pairing_code: pairData.pairing_code,
        device_id: "android_test_123",
        platform: "android"
      })
    });
    assert.equal(compRes.status, 200);
    const compData = await compRes.json();
    assert.equal(compData.success, true);

    // 4. Register manifest
    registry.registerDevice({
      device_id: "android_test_123",
      platform: "android",
      os_version: "15"
    });

    const manifestRes = await fetch(`http://127.0.0.1:${port}/api/manifest`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        module: "px27m.phone-control",
        platform: "android",
        module_version: "1.0.0",
        protocol_version: "1.0",
        device_id: "android_test_123",
        capabilities: [
          {
            id: "device.info.battery",
            category: "device_info",
            name: "Battery",
            description: "Battery info",
            supported_platforms: ["android"],
            available: true,
            permission_status: "granted",
            risk_level: RiskLevel.LEVEL_0_INFO
          }
        ]
      })
    });
    assert.equal(manifestRes.status, 200);
    const manifestData = await manifestRes.json();
    assert.equal(manifestData.registered_capabilities, 1);
  } finally {
    await server.stop();
  }
});
