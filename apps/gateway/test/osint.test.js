import test from "node:test";
import assert from "node:assert/strict";
import {
  TOP_10_WEB_OSINT_PROJECTS,
  TOP_10_PHONE_OSINT_PROJECTS,
  analyzePhoneNumber
} from "../src/phone-control/osint-modules.js";
import {
  DeviceRegistry,
  PolicyEngine,
  PairingManager,
  AuditEngine,
  CommandRouter,
  HybridGatewayServer
} from "../src/index.js";

test("TOP_10_WEB_OSINT_PROJECTS contains exactly 10 curated GitHub projects", () => {
  assert.equal(TOP_10_WEB_OSINT_PROJECTS.length, 10);
  for (const p of TOP_10_WEB_OSINT_PROJECTS) {
    assert.ok(p.id);
    assert.ok(p.name);
    assert.ok(p.github_url.startsWith("https://github.com/"));
    assert.ok(p.description);
    assert.ok(p.category);
    assert.ok(p.stars);
    assert.ok(p.key_features.length >= 3);
  }
});

test("TOP_10_PHONE_OSINT_PROJECTS contains exactly 10 curated GitHub phone intelligence projects", () => {
  assert.equal(TOP_10_PHONE_OSINT_PROJECTS.length, 10);
  for (const p of TOP_10_PHONE_OSINT_PROJECTS) {
    assert.ok(p.id);
    assert.ok(p.name);
    assert.ok(p.github_url.startsWith("https://github.com/"));
    assert.ok(p.description);
    assert.ok(p.category);
    assert.ok(p.key_features.length >= 3);
  }
});

test("analyzePhoneNumber correctly parses E.164, country prefix, and generates OSINT dorks", () => {
  const usAnalysis = analyzePhoneNumber("+1 (555) 234-5678");
  assert.equal(usAnalysis.valid, true);
  assert.equal(usAnalysis.country_code, "US/CA");
  assert.ok(usAnalysis.e164_normalized.startsWith("+1555"));
  assert.equal(usAnalysis.osint_dorks.length, 5);

  const ngAnalysis = analyzePhoneNumber("+234 803 123 4567");
  assert.equal(ngAnalysis.valid, true);
  assert.equal(ngAnalysis.country_code, "NG");
  assert.ok(ngAnalysis.flag.includes("🇳🇬"));

  const invalidAnalysis = analyzePhoneNumber("123");
  assert.equal(invalidAnalysis.valid, false);
});

test("HybridGatewayServer serves OSINT Web and Phone endpoints and Dashboard HTML", async () => {
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
    // 1. Test Dashboard HTML
    const dashRes = await fetch(`http://127.0.0.1:${port}/dashboard`);
    assert.equal(dashRes.status, 200);
    const html = await dashRes.text();
    assert.ok(html.includes("Unified Phone Control & OSINT Intelligence Platform"));
    assert.ok(html.includes("Top 10 OSINT Web-Based Projects"));
    assert.ok(html.includes("Top 10 OSINT Phone Tracking"));

    // 2. Test OSINT Web API
    const webRes = await fetch(`http://127.0.0.1:${port}/api/osint/web`);
    assert.equal(webRes.status, 200);
    const webData = await webRes.json();
    assert.equal(webData.count, 10);
    assert.equal(webData.projects[0].name, "SpiderFoot");

    // 3. Test OSINT Phone API
    const phoneRes = await fetch(`http://127.0.0.1:${port}/api/osint/phone`);
    assert.equal(phoneRes.status, 200);
    const phoneData = await phoneRes.json();
    assert.equal(phoneData.count, 10);
    assert.equal(phoneData.projects[0].name, "PhoneInfoga");

    // 4. Test Phone Analysis API
    const analyzeRes = await fetch(`http://127.0.0.1:${port}/api/osint/phone/analyze`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ phone_number: "+44 7911 123456" })
    });
    assert.equal(analyzeRes.status, 200);
    const analyzeData = await analyzeRes.json();
    assert.equal(analyzeData.country_code, "GB");
    assert.ok(analyzeData.osint_dorks.length > 0);
  } finally {
    await server.stop();
  }
});
