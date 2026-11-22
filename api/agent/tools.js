export default function handler(req, res) {
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
  if (req.method === "OPTIONS") return res.status(200).end();

  res.status(200).json({
    total_capabilities: 7,
    tools: {
      openai: [
        { type: "function", function: { name: "android_pixel_9__device_info_battery", description: "[ANDROID] Battery Telemetry" } }
      ],
      anthropic: [
        { name: "android_pixel_9__device_info_battery", description: "[ANDROID] Battery Telemetry", input_schema: { type: "object" } }
      ],
      mcp: [
        { name: "android_pixel_9__device_info_battery", description: "[ANDROID] Battery Telemetry", inputSchema: { type: "object" } }
      ]
    }
  });
}
