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

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const PUBLIC_DIR = path.resolve(__dirname, "../../public");

export class HybridGatewayServer {
  constructor({ deviceRegistry, capabilityRegistry, commandRouter, pairingManager, auditEngine, policyEngine }) {
    this.deviceRegistry = deviceRegistry;
    this.capabilityRegistry = capabilityRegistry || deviceRegistry;
    this.commandRouter = commandRouter;
    this.pairingManager = pairingManager;
    this.auditEngine = auditEngine;
    this.policyEngine = policyEngine;
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
