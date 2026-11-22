export default function handler(req, res) {
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
  if (req.method === "OPTIONS") return res.status(200).end();

  res.status(200).json({
    devices: [
      {
        device_id: "android_pixel_9",
        device_name: "Google Pixel 9 (Android 15 / SDK 36)",
        platform: "android",
        status: "connected",
        hybrid_transport: "direct_lan_active",
        battery_pct: 88,
        capabilities: [
          { id: "device.torch.toggle", name: "Torch / Flashlight Control", risk_level: 0, status: "available" },
          { id: "device.info.battery", name: "Battery & Power Telemetry", risk_level: 0, status: "available" },
          { id: "device.info.wifi", name: "WiFi & Network State", risk_level: 0, status: "available" },
          { id: "device.audio.play_alert", name: "Emergency Audio Alert", risk_level: 1, status: "available" },
          { id: "device.haptics.vibrate", name: "Haptic Feedback Pattern", risk_level: 1, status: "available" },
          { id: "device.location.query", name: "GPS Location Query", risk_level: 3, status: "permission_required" }
        ]
      },
      {
        device_id: "ios_iphone_16",
        device_name: "Apple iPhone 16 Pro (iOS 18.2)",
        platform: "ios",
        status: "connected",
        hybrid_transport: "gateway_relay_active",
        battery_pct: 94,
        capabilities: [
          { id: "device.torch.toggle", name: "AVCaptureDevice Torch", risk_level: 0, status: "available" },
          { id: "device.info.battery", name: "UIDevice Battery Level", risk_level: 0, status: "available" },
          { id: "device.haptics.vibrate", name: "UINotificationFeedbackGenerator", risk_level: 1, status: "available" },
          { id: "device.audio.play_alert", name: "AudioServices System Sound", risk_level: 1, status: "available" },
          { id: "device.sms.send", name: "Background Silent SMS", risk_level: 2, status: "sandboxed_by_os", rejection_reason: "iOS Sandbox policy strictly prohibits background SMS injection without system UI compose controller." },
          { id: "device.screen.tap", name: "Arbitrary Touch Injection", risk_level: 3, status: "sandboxed_by_os", rejection_reason: "iOS Sandbox strictly prohibits cross-application touch event injection." }
        ]
      }
    ]
  });
}
