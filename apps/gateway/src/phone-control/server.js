/**
 * HybridGatewayServer
 * Hybrid HTTP/WebSocket server implementing the server-side of hybrid connectivity
 * for companion applications and px27m agents.
 */

import http from "node:http";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { URL } from "node:url";
import {
  TOP_10_WEB_OSINT_PROJECTS,
  TOP_10_PHONE_OSINT_PROJECTS,
  analyzePhoneNumber
} from "./osint-modules.js";
import { Px27mAgentClient } from "./agent-interface.js";
import { AutonomyProfile } from "@px27m/phone-control-protocol";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const PUBLIC_DIR = path.resolve(__dirname, "../../public");

export class HybridGatewayServer {
  constructor({ deviceRegistry, capabilityRegistry, commandRouter, pairingManager, auditEngine, policyEngine, agentClient }) {
    this.deviceRegistry = deviceRegistry;
    this.capabilityRegistry = capabilityRegistry || deviceRegistry;
    this.commandRouter = commandRouter;
    this.pairingManager = pairingManager;
    this.auditEngine = auditEngine;
    this.policyEngine = policyEngine;
    this.agentClient = agentClient || new Px27mAgentClient({
      agentId: "agent_copilot_universal",
      agentName: "px27m Copilot Agent",
      autonomyProfile: AutonomyProfile.POWER_USER,
      deviceRegistry,
      commandRouter,
      policyEngine,
      auditEngine
    });
    this.server = null;
    this.activeSockets = new Map(); // deviceId -> socket/handler
  }

  start(port = 27000, host = "0.0.0.0") {
    return new Promise((resolve, reject) => {
      this.server = http.createServer((req, res) => {
        this.handleHttpRequest(req, res);
      });

      this.server.listen(port, host, () => {
        const addr = this.server.address();
        const actualPort = typeof addr === "object" && addr ? addr.port : port;
        resolve({ port: actualPort, host });
      });

      this.server.on("error", (err) => {
        reject(err);
      });
    });
  }

  stop() {
    return new Promise((resolve) => {
      if (this.server) {
        this.server.close(() => {
          resolve();
        });
      } else {
        resolve();
      }
    });
  }

  async handleHttpRequest(req, res) {
    const parsedUrl = new URL(req.url, `http://${req.headers.host || "localhost"}`);
    const pathname = parsedUrl.pathname;
    const method = req.method;

    // CORS & JSON headers
    res.setHeader("Access-Control-Allow-Origin", "*");
    res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

    if (method === "OPTIONS") {
      res.writeHead(204);
      res.end();
      return;
    }

    try {
      if (pathname === "/" || pathname === "/dashboard") {
        const htmlPath = path.join(PUBLIC_DIR, "index.html");
        if (fs.existsSync(htmlPath)) {
          const html = fs.readFileSync(htmlPath, "utf-8");
          res.writeHead(200, { "Content-Type": "text/html; charset=utf-8" });
          res.end(html);
          return;
        }
      }

      if (pathname === "/api/osint/web" && method === "GET") {
        this.sendJson(res, 200, { count: TOP_10_WEB_OSINT_PROJECTS.length, projects: TOP_10_WEB_OSINT_PROJECTS });
        return;
      }

      if (pathname === "/api/osint/phone" && method === "GET") {
        this.sendJson(res, 200, { count: TOP_10_PHONE_OSINT_PROJECTS.length, projects: TOP_10_PHONE_OSINT_PROJECTS });
        return;
      }

      if (pathname === "/api/osint/phone/analyze" && method === "POST") {
        const body = await this.readJsonBody(req);
        const analysis = analyzePhoneNumber(body.phone_number || body.number || "");
        this.sendJson(res, 200, analysis);
        return;
      }

      if (pathname === "/health" && method === "GET") {
        this.sendJson(res, 200, { status: "healthy", service: "px27m-phone-control-gateway", v: "1.0" });
        return;
      }

      if (pathname === "/api/devices" && method === "GET") {
        const devices = this.deviceRegistry.listConnectedDevices();
        this.sendJson(res, 200, { devices });
        return;
      }

      if (pathname === "/api/pair/init" && method === "POST") {
        const body = await this.readJsonBody(req);
        const result = this.pairingManager.initiatePairing(body.device_name || "Companion Device");
        this.sendJson(res, 200, result);
        return;
      }

      if (pathname === "/api/pair/complete" && method === "POST") {
        const body = await this.readJsonBody(req);
        const result = this.pairingManager.completePairing(body.pairing_code, body.device_id, body.platform);
        this.sendJson(res, 200, result);
        return;
      }

      if (pathname === "/api/manifest" && method === "POST") {
        const body = await this.readJsonBody(req);
        const manifest = this.deviceRegistry.updateManifest(body);
        this.sendJson(res, 200, { success: true, registered_capabilities: manifest.capabilities.length });
        return;
      }

      if (pathname === "/api/command" && method === "POST") {
        const body = await this.readJsonBody(req);
        const deviceId = body.device_id;
        const command = body.command;

        if (!deviceId || !command) {
          this.sendJson(res, 400, { error: "Both 'device_id' and 'command' are required in request body" });
          return;
        }

        const result = await this.commandRouter.dispatch(command, deviceId);
        this.sendJson(res, 200, result);
        return;
      }

      if (pathname === "/api/agent/tools" && method === "GET") {
        const deviceId = parsedUrl.searchParams.get("device_id") || null;
        const [openai, anthropic, mcp, capabilities] = await Promise.all([
          this.agentClient.getOpenAITools(deviceId),
          this.agentClient.getAnthropicTools(deviceId),
          this.agentClient.getMcpTools(deviceId),
          this.agentClient.discoverAvailableCapabilities(deviceId)
        ]);
        this.sendJson(res, 200, {
          total_capabilities: capabilities.length,
          capabilities,
          tools: { openai, anthropic, mcp }
        });
        return;
      }

      if (pathname === "/api/agent/execute" && method === "POST") {
        const body = await this.readJsonBody(req);
        if (body.tool_name) {
          const result = await this.agentClient.executeToolCall(body.tool_name, body.parameters || {}, body.approval_token || null);
          this.sendJson(res, 200, result);
          return;
        }
        if (!body.device_id || !body.capability_id) {
          this.sendJson(res, 400, { error: "device_id and capability_id (or tool_name) are required" });
          return;
        }
        const result = await this.agentClient.executeCapability({
          deviceId: body.device_id,
          capabilityId: body.capability_id,
          parameters: body.parameters || {},
          approvalToken: body.approval_token || null,
          timeoutMs: body.timeout_ms || 15000,
          verifyResult: body.verify_result ?? true
        });
        this.sendJson(res, 200, result);
        return;
      }

      if (pathname === "/api/agent/workflow" && method === "POST") {
        const body = await this.readJsonBody(req);
        if (!body.device_id || !Array.isArray(body.steps)) {
          this.sendJson(res, 400, { error: "device_id and steps array are required" });
          return;
        }
        const result = await this.agentClient.executeWorkflow(body.steps, body.device_id, {
          approvalToken: body.approval_token,
          stopOnError: body.stop_on_error
        });
        this.sendJson(res, 200, result);
        return;
      }

      if (pathname === "/api/agent/copilot" && method === "POST") {
        const body = await this.readJsonBody(req);
        const prompt = (body.prompt || "").trim();
        if (!prompt) {
          this.sendJson(res, 400, { error: "prompt is required" });
          return;
        }

        const lower = prompt.toLowerCase();
        let targetDeviceId = body.device_id;
        if (!targetDeviceId) {
          const connected = this.deviceRegistry.listConnectedDevices().filter(d => d.status === "connected");
          targetDeviceId = connected.length > 0 ? connected[0].device_id : null;
        }

        const executedSteps = [];
        let osintPhoneAnalysis = null;

        // Check if phone number / phone tracking query
        const phoneRegex = /(?:\+?\d{1,3}[-.\s]?)?\(?\d{2,4}\)?[-.\s]?\d{3,4}[-.\s]?\d{3,4}/;
        const phoneMatch = prompt.match(phoneRegex);
        if (phoneMatch && phoneMatch[0].replace(/\D/g, "").length >= 7) {
          osintPhoneAnalysis = analyzePhoneNumber(phoneMatch[0]);
          executedSteps.push({
            tool: "osint.phone.analyze",
            target: phoneMatch[0],
            result: osintPhoneAnalysis
          });
        }

        // Plan device capability steps
        if (targetDeviceId) {
          const availableCaps = await this.agentClient.discoverAvailableCapabilities(targetDeviceId);
          const capsToRun = [];

          if ((lower.includes("battery") || lower.includes("charge") || lower.includes("power")) &&
              availableCaps.some(c => c.capability_id === "device.info.battery")) {
            capsToRun.push("device.info.battery");
          }
          if ((lower.includes("system") || lower.includes("spec") || lower.includes("hardware")) &&
              availableCaps.some(c => c.capability_id === "device.info.system")) {
            capsToRun.push("device.info.system");
          }
          if ((lower.includes("storage") || lower.includes("space") || lower.includes("disk")) &&
              availableCaps.some(c => c.capability_id === "device.info.storage")) {
            capsToRun.push("device.info.storage");
          }
          if ((lower.includes("network") || lower.includes("wifi") || lower.includes("connectivity")) &&
              availableCaps.some(c => c.capability_id === "device.connectivity.status")) {
            capsToRun.push("device.connectivity.status");
          }
          if ((lower.includes("notification") || lower.includes("alert")) &&
              availableCaps.some(c => c.capability_id === "device.notifications.list")) {
            capsToRun.push("device.notifications.list");
          }
          if ((lower.includes("screen") || lower.includes("inspect")) &&
              availableCaps.some(c => c.capability_id === "device.screen.inspect")) {
            capsToRun.push("device.screen.inspect");
          }

          for (const capId of capsToRun) {
            const resExec = await this.agentClient.executeCapability({
              deviceId: targetDeviceId,
              capabilityId: capId,
              parameters: {},
              approvalToken: body.approval_token || null
            });
            executedSteps.push({
              tool: capId,
              target: targetDeviceId,
              result: resExec
            });
          }
        }

        // Formulate synthesis
        let synthesis = "";
        if (executedSteps.length === 0) {
          synthesis = `Copilot processed your prompt: "${prompt}". No active connected device capabilities matched this request, or device is currently offline.`;
        } else {
          const summaryParts = executedSteps.map(s => {
            if (s.tool === "osint.phone.analyze") {
              return `• **Phone Intelligence**: Normalized E.164: \`${s.result.e164_normalized}\`, Country: ${s.result.country} ${s.result.flag}. Generated ${s.result.osint_dorks.length} OSINT search dorks.`;
            }
            const status = s.result.success ? "Verified ✅" : "Failed ❌";
            return `• **${s.tool}** [${s.target}]: ${status} (${s.result.verification?.verification_method || "direct"}).`;
          });
          synthesis = `Copilot successfully executed ${executedSteps.length} action(s):\n\n${summaryParts.join("\n")}\n\nAll actions have been logged to the immutable audit trail.`;
        }

        this.sendJson(res, 200, {
          prompt,
          target_device_id: targetDeviceId,
          steps_executed: executedSteps.length,
          steps: executedSteps,
          synthesis
        });
        return;
      }

      if (pathname === "/api/audit" && method === "GET") {
        const filter = {
          agent_id: parsedUrl.searchParams.get("agent_id") || undefined,
          capability: parsedUrl.searchParams.get("capability") || undefined,
          status: parsedUrl.searchParams.get("status") || undefined
        };
        const trail = this.auditEngine.getAuditTrail(filter);
        this.sendJson(res, 200, { count: trail.length, audit_entries: trail });
        return;
      }

      this.sendJson(res, 404, { error: `Not found: ${pathname}` });
    } catch (err) {
      this.sendJson(res, 500, { error: err.message });
    }
  }

  readJsonBody(req) {
    return new Promise((resolve, reject) => {
      let data = "";
      req.on("data", chunk => { data += chunk; });
      req.on("end", () => {
        try {
          resolve(data ? JSON.parse(data) : {});
        } catch (e) {
          reject(new Error("Malformed JSON request body"));
        }
      });
      req.on("error", reject);
    });
  }

  sendJson(res, statusCode, obj) {
    res.writeHead(statusCode, { "Content-Type": "application/json" });
    res.end(JSON.stringify(obj));
  }
}
