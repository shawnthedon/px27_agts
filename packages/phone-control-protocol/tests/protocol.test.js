import test from "node:test";
import assert from "node:assert/strict";
import {
  validateCommand,
  validateManifest,
  validateResult,
  RiskLevel,
  AutonomyProfile,
  CommandStatus,
  PROTOCOL_VERSION
} from "../src/index.js";

test("protocol version is defined", () => {
  assert.equal(PROTOCOL_VERSION, "1.0");
});

test("validates compliant command payload", () => {
  const command = {
    command_id: "cmd_test_001",
    capability: "device.application.launch",
    parameters: {
      package_name: "com.example.app"
    },
    execution: {
      mode: "foreground",
      timeout_ms: 5000,
      verify_result: true
    },
    authorization: {
      agent_id: "agent_strike_01",
      autonomy_profile: AutonomyProfile.OPERATOR
    }
  };

  const res = validateCommand(command);
  assert.equal(res.valid, true);
  assert.equal(res.errors.length, 0);
});

test("rejects malformed command payload", () => {
  const command = {
    command_id: "",
    capability: "invalid_format",
    parameters: "not_an_object"
  };

  const res = validateCommand(command);
  assert.equal(res.valid, false);
  assert.ok(res.errors.length > 0);
});

test("validates compliant capability manifest", () => {
  const manifest = {
    module: "px27m.phone-control",
    platform: "android",
    module_version: "1.0.0",
    protocol_version: "1.0",
    device_id: "dev_pixel_9_pro",
    capabilities: [
      {
        id: "device.screen.inspect",
        category: "screen",
        name: "Inspect Accessible Screen Hierarchy",
        description: "Traverse accessibility element nodes",
        supported_platforms: ["android"],
        available: true,
        permission_status: "granted",
        risk_level: RiskLevel.LEVEL_2_SENSITIVE,
        supports_background: false
      },
      {
        id: "device.screen.tap",
        category: "screen",
        name: "Tap Screen Element",
        description: "Click accessible node or coordinate",
        supported_platforms: ["android"],
        available: true,
        permission_status: "granted",
        risk_level: RiskLevel.LEVEL_3_HIGH,
        supports_background: false
      }
    ]
  };

  const res = validateManifest(manifest);
  assert.equal(res.valid, true);
});

test("validates truthful execution result", () => {
  const result = {
    command_id: "cmd_test_001",
    status: CommandStatus.COMPLETED,
    platform: "android",
    capability: "device.application.launch",
    result: { launched: true, package_name: "com.example.app" },
    verification: {
      verified: true,
      verification_method: "foreground_package_check"
    },
    duration_ms: 320
  };

  const res = validateResult(result);
  assert.equal(res.valid, true);
});
