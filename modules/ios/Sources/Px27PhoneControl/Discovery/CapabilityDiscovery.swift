import Foundation

/// CapabilityDiscovery for iOS
/// Dynamically interrogates iOS platform capabilities, enforcing strict truthfulness about sandbox boundaries.
public struct CapabilityDiscovery: Sendable {

    public init() {}

    public func discoverCapabilities(deviceId: String) -> CapabilityManifest {
        var caps: [CapabilityDefinition] = []

        // 3.1 Screen and Interface (Truthful iOS Sandboxing)
        caps.append(
            CapabilityDefinition(
                id: "device.screen.inspect",
                category: "screen",
                name: "Inspect System Screen Hierarchy",
                description: "Arbitrary system-wide UI inspection is prohibited by Apple iOS application sandbox",
                available: false,
                unavailabilityReason: UnavailabilityReason.unsupportedPlatformSandbox.rawValue,
                permissionStatus: PermissionStatus.restrictedByOS.rawValue,
                riskLevel: .level2Sensitive
            )
        )
        caps.append(
            CapabilityDefinition(
                id: "device.screen.tap",
                category: "screen",
                name: "System-wide Tap Injection",
                description: "Arbitrary gesture and tap injection into external apps is prohibited on iOS",
                available: false,
                unavailabilityReason: UnavailabilityReason.unsupportedPlatformSandbox.rawValue,
                permissionStatus: PermissionStatus.restrictedByOS.rawValue,
                riskLevel: .level3High
            )
        )
        caps.append(
            CapabilityDefinition(
                id: "device.screen.capture",
                category: "screen",
                name: "Capture Screen",
                description: "Capture application-owned screenshot or trigger Shortcuts screenshot action",
                available: true,
                permissionStatus: PermissionStatus.notRequired.rawValue,
                riskLevel: .level1Ordinary
            )
        )

        // 3.2 Application Management
        caps.append(
            CapabilityDefinition(
                id: "device.application.deep_link",
                category: "application",
                name: "Open Universal Link / URL Scheme",
                description: "Dispatch URL schemes and Universal Links to open external applications",
                available: true,
                permissionStatus: PermissionStatus.notRequired.rawValue,
                riskLevel: .level1Ordinary
            )
        )

        // 3.3 Device Info
        caps.append(
            CapabilityDefinition(
                id: "device.info.system",
                category: "device_info",
                name: "Device Hardware & iOS Version",
                description: "Query device model, iOS system version, and system uptime",
                available: true,
                permissionStatus: PermissionStatus.notRequired.rawValue,
                riskLevel: .level0Info,
                supportsBackground: true
            )
        )
        caps.append(
            CapabilityDefinition(
                id: "device.info.battery",
                category: "device_info",
                name: "Battery Telemetry",
                description: "Inspect battery charge level and charging status",
                available: true,
                permissionStatus: PermissionStatus.notRequired.rawValue,
                riskLevel: .level0Info,
                supportsBackground: true
            )
        )
        caps.append(
            CapabilityDefinition(
                id: "device.info.storage",
                category: "device_info",
                name: "Storage Telemetry",
                description: "Inspect available and total disk capacity bytes",
                available: true,
                permissionStatus: PermissionStatus.notRequired.rawValue,
                riskLevel: .level0Info,
                supportsBackground: true
            )
        )

        // 3.4 Connectivity
        caps.append(
            CapabilityDefinition(
                id: "device.connectivity.status",
                category: "connectivity",
                name: "Network Connectivity Status",
                description: "Monitor cellular, Wi-Fi, and gateway connectivity via Network framework",
                available: true,
                permissionStatus: PermissionStatus.notRequired.rawValue,
                riskLevel: .level0Info,
                supportsBackground: true
            )
        )

        // 3.5 Telephony
        caps.append(
            CapabilityDefinition(
                id: "device.telephony.dial",
                category: "telephony",
                name: "Prepare Phone Call",
                description: "Open system dialler via tel: scheme (requires user confirmation)",
                available: true,
                permissionStatus: PermissionStatus.notRequired.rawValue,
                riskLevel: .level1Ordinary
            )
        )

        // 3.6 Messaging
        caps.append(
            CapabilityDefinition(
                id: "device.messaging.share",
                category: "messaging",
                name: "Share Sheet Dispatch",
                description: "Present iOS system share sheet for text, links, or media",
                available: true,
                permissionStatus: PermissionStatus.notRequired.rawValue,
                riskLevel: .level1Ordinary
            )
        )

        // 3.7 Audio
        caps.append(
            CapabilityDefinition(
                id: "device.audio.tts",
                category: "audio",
                name: "AVSpeechSynthesizer TTS",
                description: "Synthesize natural spoken audio from text via iOS Speech framework",
                available: true,
                permissionStatus: PermissionStatus.notRequired.rawValue,
                riskLevel: .level1Ordinary,
                supportsBackground: true
            )
        )

        // 3.8 Camera & Vision
        caps.append(
            CapabilityDefinition(
                id: "device.camera.torch",
                category: "camera",
                name: "Flashlight / Torch Toggle",
                description: "Toggle rear camera LED torch via AVCaptureDevice",
                available: true,
                permissionStatus: PermissionStatus.notRequired.rawValue,
                riskLevel: .level1Ordinary
            )
        )
        caps.append(
            CapabilityDefinition(
                id: "device.camera.ocr",
                category: "camera",
                name: "Vision Framework Text OCR",
                description: "Recognize printed and handwritten text in images using Apple Vision",
                available: true,
                permissionStatus: PermissionStatus.notRequired.rawValue,
                riskLevel: .level1Ordinary,
                supportsBackground: true
            )
        )

        // 3.9 Storage
        caps.append(
            CapabilityDefinition(
                id: "device.storage.list_files",
                category: "storage",
                name: "List App Sandbox Documents",
                description: "List files inside the companion app sandbox Documents directory",
                available: true,
                permissionStatus: PermissionStatus.notRequired.rawValue,
                riskLevel: .level1Ordinary,
                supportsBackground: true
            )
        )

        // 3.10 Notifications
        caps.append(
            CapabilityDefinition(
                id: "device.notifications.post",
                category: "notifications",
                name: "Post UserNotification",
                description: "Schedule local notification banner via UNUserNotificationCenter",
                available: true,
                permissionStatus: PermissionStatus.granted.rawValue,
                riskLevel: .level1Ordinary,
                supportsBackground: true
            )
        )

        // 3.12 Sensors & Haptics
        caps.append(
            CapabilityDefinition(
                id: "device.sensors.vibrate",
                category: "sensors",
                name: "Trigger Haptic Feedback",
                description: "Dispatch physical haptic tap via UIImpactFeedbackGenerator",
                available: true,
                permissionStatus: PermissionStatus.notRequired.rawValue,
                riskLevel: .level1Ordinary
            )
        )

        // 3.13 Settings
        caps.append(
            CapabilityDefinition(
                id: "device.settings.open",
                category: "settings",
                name: "Open iOS Application Settings",
                description: "Open the system Settings page for px27m Phone Control",
                available: true,
                permissionStatus: PermissionStatus.notRequired.rawValue,
                riskLevel: .level1Ordinary
            )
        )

        // 3.14 Automation (Shortcuts & App Intents)
        caps.append(
            CapabilityDefinition(
                id: "device.automation.run_workflow",
                category: "automation",
                name: "Execute Apple Shortcut",
                description: "Trigger registered Apple Shortcut or App Intent via shortcuts:// scheme",
                available: true,
                permissionStatus: PermissionStatus.notRequired.rawValue,
                riskLevel: .level3High
            )
        )

        // 3.15 Security
        caps.append(
            CapabilityDefinition(
                id: "device.security.disconnect",
                category: "security",
                name: "Emergency Disconnect",
                description: "Instantly revoke session token and terminate remote gateway link",
                available: true,
                permissionStatus: PermissionStatus.notRequired.rawValue,
                riskLevel: .level4Critical,
                supportsBackground: true
            )
        )

        return CapabilityManifest(
            deviceId: deviceId,
            deviceName: "Apple iPhone",
            osVersion: "iOS 18+",
            capabilities: caps
        )
    }
}
