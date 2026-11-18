import { analyzePhoneNumber } from "../../../apps/gateway/src/phone-control/osint-modules.js";

export default function handler(req, res) {
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type");
  if (req.method === "OPTIONS") return res.status(200).end();

  let body = req.body;
  if (typeof body === "string") {
    try {
      body = JSON.parse(body);
    } catch {
      body = {};
    }
  }
  body = body || {};
  const number = body.phone_number || body.number || "";
  const analysis = analyzePhoneNumber(number);
  res.status(200).json(analysis);
}
