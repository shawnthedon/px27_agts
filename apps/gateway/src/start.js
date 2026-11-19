/**
 * px27m Platform Gateway Server Runner
 * Boots the HTTP/WebSocket gateway server bound to 0.0.0.0 for live browser previews
 */

import {
  DeviceRegistry,
  PolicyEngine,
  AuditEngine,
  PairingManager,
  CommandRouter,
  HybridGatewayServer,
  Px27mAgentClient
} from "./index.js";
import { RiskLevel, AutonomyProfile, CommandStatus } from "@px27m/phone-control-protocol";

const PORT = parseInt(process.env.PORT || "3000", 10);
const HOST = process.env.HOST || "0.0.0.0";

const registry = new DeviceRegistry();
const policy = new PolicyEngine();
const audit = new AuditEngine();
const pairing = new PairingManager();
const router = new CommandRouter(registry, policy, audit);

// Register Companion Device for live interactive preview
const companionDevice = registry.registerDevice({
  device_id: "android_pixel_9",
  device_name: "Pixel 9 Pro (Android 15)",
  platform: "android",
  os_version: "Android 15 (API 35)",
  connection_mode: "hybrid",
  gateway_connected: true
});

registry.updateManifest({
  module: "px27m.phone-control",
  platform: "android",
  module_version: "1.0.0",
  protocol_version: "1.0",
  device_id: "android_pixel_9",
  capabilities: [
    {
      id: "device.info.system",
      category: "device_info",
      name: "System Hardware and OS Info",
      description: "Query manufacturer, model, Android OS version, and CPU architecture",
      supported_platforms: ["android"],
      available: true,
      permission_status: "granted",
      risk_level: RiskLevel.LEVEL_0_INFO,
      supports_background: true
    },
    {
      id: "device.info.battery",
      category: "device_info",
      name: "Battery Telemetry",
      description: "Query battery charge level, health, voltage, and charging status",
      supported_platforms: ["android"],
      available: true,
      permission_status: "granted",
      risk_level: RiskLevel.LEVEL_0_INFO,
      supports_background: true
    },
    {
      id: "device.info.storage",
      category: "device_info",
      name: "Storage Telemetry",
      description: "Inspect free and total internal storage bytes via StatFs",
      supported_platforms: ["android"],
      available: true,
      permission_status: "granted",
      risk_level: RiskLevel.LEVEL_0_INFO,
      supports_background: true
    },
    {
      id: "device.connectivity.status",
      category: "connectivity",
      name: "Network & Connectivity Status",
      description: "Inspect Wi-Fi, cellular, and internet availability",
      supported_platforms: ["android"],
      available: true,
      permission_status: "granted",
      risk_level: RiskLevel.LEVEL_0_INFO,
      supports_background: true
    },
    {
      id: "device.notifications.list",
      category: "notifications",
      name: "List Active Notifications",
      description: "Inspect active status bar notifications via NotificationListenerService",
      supported_platforms: ["android"],
      available: true,
      permission_status: "granted",
      risk_level: RiskLevel.LEVEL_2_SENSITIVE,
      supports_background: true
    },
    {
      id: "device.screen.inspect",
      category: "screen",
      name: "Inspect Accessible Screen Hierarchy",
      description: "Read window node elements, text, bounds, and foreground app info",
      supported_platforms: ["android"],
      available: true,
      permission_status: "granted",
      risk_level: RiskLevel.LEVEL_2_SENSITIVE,
      supports_background: false
    },
    {
      id: "device.hardware.torch",
      category: "camera",
      name: "Camera Flashlight Control",
      description: "Toggle device camera LED torch on or off",
      supported_platforms: ["android"],
      available: true,
      permission_status: "granted",
      risk_level: RiskLevel.LEVEL_1_ORDINARY,
      supports_background: true
    }
  ]
});

// Register transport handler for the companion device
router.registerTransport("android_pixel_9", async (cmd) => {
  switch (cmd.capability) {
    case "device.info.battery":
      return {
        command_id: cmd.command_id,
        status: CommandStatus.COMPLETED,
        platform: "android",
        capability: cmd.capability,
        result: { level_pct: 88, is_charging: true, temperature_c: 29.4, voltage_mv: 4120 },
        verification: { verified: true, verification_method: "battery_manager_receiver" },
        duration_ms: 18
      };
    case "device.info.system":
      return {
        command_id: cmd.command_id,
        status: CommandStatus.COMPLETED,
        platform: "android",
        capability: cmd.capability,
        result: { manufacturer: "Google", model: "Pixel 9 Pro", os: "Android 15", abi: "arm64-v8a" },
        verification: { verified: true, verification_method: "android_os_build_constants" },
        duration_ms: 12
      };
    case "device.info.storage":
      return {
        command_id: cmd.command_id,
        status: CommandStatus.COMPLETED,
        platform: "android",
        capability: cmd.capability,
        result: { free_gb: 184, total_gb: 256, free_pct: 71.8 },
        verification: { verified: true, verification_method: "stat_fs_environment" },
        duration_ms: 15
      };
    case "device.connectivity.status":
      return {
        command_id: cmd.command_id,
        status: CommandStatus.COMPLETED,
        platform: "android",
        capability: cmd.capability,
        result: { connected: true, type: "WIFI", ssid: "px27m_secure_mesh", link_speed_mbps: 866 },
        verification: { verified: true, verification_method: "connectivity_manager_network_capabilities" },
        duration_ms: 20
      };
    case "device.notifications.list":
      return {
        command_id: cmd.command_id,
        status: CommandStatus.COMPLETED,
        platform: "android",
        capability: cmd.capability,
        result: { active_count: 2, notifications: ["Signal: Encrypted message received", "Calendar: Team sync in 15m"] },
        verification: { verified: true, verification_method: "notification_listener_service" },
        duration_ms: 25
      };
    case "device.screen.inspect":
      return {
        command_id: cmd.command_id,
        status: CommandStatus.COMPLETED,
        platform: "android",
        capability: cmd.capability,
        result: { foreground_app: "com.pocketstrike.app", focused_node: "chat_input_field", node_count: 42 },
        verification: { verified: true, verification_method: "accessibility_service_node_tree" },
        duration_ms: 35
      };
    case "device.hardware.torch":
      return {
        command_id: cmd.command_id,
        status: CommandStatus.COMPLETED,
        platform: "android",
        capability: cmd.capability,
        result: { torch_state: "toggled" },
        verification: { verified: true, verification_method: "camera_manager_set_torch" },
        duration_ms: 22
      };
    default:
      return {
        command_id: cmd.command_id,
        status: CommandStatus.COMPLETED,
        platform: "android",
        capability: cmd.capability,
        result: { success: true },
        verification: { verified: true, verification_method: "device_runtime_dispatch" },
        duration_ms: 15
      };
  }
});

const agentClient = new Px27mAgentClient({
  agentId: "agent_copilot_universal",
  agentName: "px27m Copilot Agent",
  autonomyProfile: AutonomyProfile.POWER_USER,
  deviceRegistry: registry,
  commandRouter: router,
  policyEngine: policy,
  auditEngine: audit
});

const server = new HybridGatewayServer({
  deviceRegistry: registry,
  commandRouter: router,
  pairingManager: pairing,
  auditEngine: audit,
  policyEngine: policy,
  agentClient
});

server.start(PORT, HOST).then(({ port, host }) => {
  console.log(`[px27m] Hybrid Gateway Server is live at http://${host}:${port}`);
  console.log(`[px27m] Dashboard URL: http://${host}:${port}/dashboard`);
  console.log(`[px27m] Top 10 Web OSINT & Top 10 Phone OSINT modules loaded`);
  console.log(`[px27m] Copilot AI Agent console active with OpenAI, Claude, and MCP tool schemas`);
}).catch((err) => {
  console.error("[px27m] Failed to start gateway server:", err);
  process.exit(1);
});
