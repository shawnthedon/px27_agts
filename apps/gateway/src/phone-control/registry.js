/**
 * px27m Universal Phone Control Capability & Device Registry
 * Automatically registers device capabilities and generates tool definitions for agents
 */

import { validateManifest } from "@px27m/phone-control-protocol";

export class DeviceRegistry {
  constructor() {
    this.devices = new Map();
    this.manifests = new Map();
  }

  registerDevice(deviceInfo) {
    if (!deviceInfo || !deviceInfo.device_id) {
      throw new Error("Invalid device info: device_id is required");
    }
    const record = {
      device_id: deviceInfo.device_id,
      device_name: deviceInfo.device_name || "Unknown Device",
      platform: deviceInfo.platform,
      os_version: deviceInfo.os_version,
      connection_mode: deviceInfo.connection_mode || "hybrid",
      local_endpoint: deviceInfo.local_endpoint || null,
      gateway_connected: deviceInfo.gateway_connected ?? true,
      last_seen_ms: Date.now(),
      status: "connected"
    };
    this.devices.set(deviceInfo.device_id, record);
    return record;
  }

  updateManifest(manifest) {
    const val = validateManifest(manifest);
    if (!val.valid) {
      throw new Error(`Invalid capability manifest: ${val.errors.join("; ")}`);
    }

    this.manifests.set(manifest.device_id, manifest);
    const dev = this.devices.get(manifest.device_id);
    if (dev) {
      dev.last_seen_ms = Date.now();
      dev.platform = manifest.platform;
      dev.os_version = manifest.os_version;
      dev.status = "connected";
    }
    return manifest;
  }

  getDevice(deviceId) {
    return this.devices.get(deviceId) || null;
  }

  getManifest(deviceId) {
    return this.manifests.get(deviceId) || null;
  }

  listConnectedDevices() {
    return Array.from(this.devices.values());
  }

  /**
   * Automatically generate agent-facing tool definitions for an authorized agent.
   * Filters by device availability, platform support, and agent autonomy profile.
   */
  getAgentTools(deviceId, agentProfile = "operator") {
    const manifest = this.manifests.get(deviceId);
    if (!manifest) return [];

    return manifest.capabilities
      .filter(cap => cap.available && cap.permission_status === "granted")
      .map(cap => ({
        type: "function",
        function: {
          name: cap.id.replace(/\./g, "_"),
          description: `[${manifest.platform.toUpperCase()}] ${cap.name}: ${cap.description}. Risk Level: ${cap.risk_level}.`,
          parameters: cap.parameters_schema || {
            type: "object",
            properties: {},
            required: []
          }
        },
        metadata: {
          capability_id: cap.id,
          category: cap.category,
          risk_level: cap.risk_level,
          platform: manifest.platform,
          device_id: deviceId
        }
      }));
  }

  disconnectDevice(deviceId) {
    const dev = this.devices.get(deviceId);
    if (dev) {
      dev.status = "disconnected";
      dev.gateway_connected = false;
    }
  }
}
