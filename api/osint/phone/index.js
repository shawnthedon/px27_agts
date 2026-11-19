import { TOP_10_PHONE_OSINT_PROJECTS } from "../../../apps/gateway/src/phone-control/osint-modules.js";

export default function handler(req, res) {
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
  if (req.method === "OPTIONS") return res.status(200).end();

  res.status(200).json({ count: TOP_10_PHONE_OSINT_PROJECTS.length, projects: TOP_10_PHONE_OSINT_PROJECTS });
}
