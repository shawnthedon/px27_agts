/**
 * px27m Pairing and Security Manager
 * Generates OTP / QR payloads, verifies tokens, and manages sessions
 */

import crypto from "node:crypto";

export class PairingManager {
  constructor() {
    this.pendingPairings = new Map();
    this.pairedDevices = new Map();
  }

  /**
   * Generates a 6-digit OTP and cryptographically secure secret for a new pairing attempt
   */
  initiatePairing(deviceName = "Mobile Device") {
    const pairingCode = Math.floor(100000 + Math.random() * 900000).toString();
    const pairingId = crypto.randomUUID();
    const sharedSecret = crypto.randomBytes(32).toString("hex");

    const record = {
      pairing_id: pairingId,
      pairing_code: pairingCode,
      shared_secret: sharedSecret,
      device_name: deviceName,
      created_at_ms: Date.now(),
      expires_at_ms: Date.now() + 5 * 60 * 1000 // 5 minutes validity
    };

    this.pendingPairings.set(pairingCode, record);

    return {
      pairing_id: pairingId,
      pairing_code: pairingCode,
      qr_payload: JSON.stringify({
        module: "px27m.phone-control",
        v: "1.0",
        pid: pairingId,
        code: pairingCode,
        secret: sharedSecret
      }),
      expires_in_seconds: 300
    };
  }

  /**
   * Completes the pairing handshake from the companion application
   */
  completePairing(pairingCode, deviceId, platform) {
    const record = this.pendingPairings.get(pairingCode);
    if (!record) {
      throw new Error("Invalid or unknown pairing code");
    }
    if (Date.now() > record.expires_at_ms) {
      this.pendingPairings.delete(pairingCode);
      throw new Error("Pairing code has expired");
    }

    const deviceRecord = {
      device_id: deviceId,
      platform: platform,
      shared_secret: record.shared_secret,
      paired_at_ms: Date.now(),
      status: "active"
    };

    this.pairedDevices.set(deviceId, deviceRecord);
    this.pendingPairings.delete(pairingCode);

    return {
      success: true,
      device_id: deviceId,
      auth_token: this.generateDeviceToken(deviceId, record.shared_secret)
    };
  }

  generateDeviceToken(deviceId, secret) {
    const timestamp = Date.now();
    const hmac = crypto.createHmac("sha256", secret);
    hmac.update(`${deviceId}:${timestamp}`);
    return `${timestamp}.${hmac.digest("hex")}`;
  }

  verifySignature(deviceId, payloadString, signature) {
    const device = this.pairedDevices.get(deviceId);
    if (!device) return false;
    const hmac = crypto.createHmac("sha256", device.shared_secret);
    hmac.update(payloadString);
    const expected = hmac.digest("hex");
    return crypto.timingSafeEqual(Buffer.from(signature, "hex"), Buffer.from(expected, "hex"));
  }

  revokeDevice(deviceId) {
    return this.pairedDevices.delete(deviceId);
  }
}
