package com.pocketstrike.app.discovery

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.pocketstrike.app.core.protocol.CapabilityDefinition
import com.pocketstrike.app.core.protocol.CapabilityManifest
import com.pocketstrike.app.core.protocol.RiskLevel
import com.pocketstrike.app.services.Px27AccessibilityService
import com.pocketstrike.app.services.Px27NotificationListener

/**
 * CapabilityDiscoveryEngine
 * Interrogates Android hardware, OS capabilities, and granted permissions to produce
 * a truthful, dynamically discovered CapabilityManifest for px27m agents.
 */
class CapabilityDiscoveryEngine(private val context: Context) {

    fun discoverCapabilities(deviceId: String): CapabilityManifest {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val hasAccel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
        val hasGyro = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null
        val hasLight = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT) != null

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        val hasCamera = try {
            (cameraManager?.cameraIdList?.isNotEmpty() == true)
        } catch (e: Exception) {
            false
        }

        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val hasTelephony = context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)

        val accessibilityActive = Px27AccessibilityService.isServiceActive
        val notificationActive = Px27NotificationListener.isServiceActive

        val list = mutableListOf<CapabilityDefinition>()

        // 3.1 Screen and Interface Control
        list.add(
            CapabilityDefinition(
                id = "device.screen.inspect",
                category = "screen",
                name = "Inspect Accessible Screen Hierarchy",
                description = "Read window node elements, text, bounds, and foreground app info",
                available = accessibilityActive,
                unavailabilityReason = if (!accessibilityActive) "service_disabled" else null,
                permissionStatus = if (accessibilityActive) "granted" else "not_granted",
                requiredPermissions = listOf("android.permission.BIND_ACCESSIBILITY_SERVICE"),
                riskLevel = RiskLevel.LEVEL_2_SENSITIVE,
                supportsBackground = false
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.screen.tap",
                category = "screen",
                name = "Tap Screen Element",
                description = "Click an accessible UI element by text, view_id, or coordinate",
                available = accessibilityActive,
                unavailabilityReason = if (!accessibilityActive) "service_disabled" else null,
                permissionStatus = if (accessibilityActive) "granted" else "not_granted",
                requiredPermissions = listOf("android.permission.BIND_ACCESSIBILITY_SERVICE"),
                riskLevel = RiskLevel.LEVEL_3_HIGH,
                supportsBackground = false
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.screen.swipe",
                category = "screen",
                name = "Swipe and Scroll Screen",
                description = "Perform directional swipes or scrolling gestures",
                available = accessibilityActive,
                unavailabilityReason = if (!accessibilityActive) "service_disabled" else null,
                permissionStatus = if (accessibilityActive) "granted" else "not_granted",
                requiredPermissions = listOf("android.permission.BIND_ACCESSIBILITY_SERVICE"),
                riskLevel = RiskLevel.LEVEL_3_HIGH,
                supportsBackground = false
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.screen.input_text",
                category = "screen",
                name = "Input Text",
                description = "Enter text into the currently focused editable field",
                available = accessibilityActive,
                unavailabilityReason = if (!accessibilityActive) "service_disabled" else null,
                permissionStatus = if (accessibilityActive) "granted" else "not_granted",
                requiredPermissions = listOf("android.permission.BIND_ACCESSIBILITY_SERVICE"),
                riskLevel = RiskLevel.LEVEL_2_SENSITIVE,
                supportsBackground = false
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.screen.global_action",
                category = "screen",
                name = "Perform Global Action",
                description = "Execute system actions: back, home, recents, notifications, quick_settings",
                available = accessibilityActive,
                unavailabilityReason = if (!accessibilityActive) "service_disabled" else null,
                permissionStatus = if (accessibilityActive) "granted" else "not_granted",
                requiredPermissions = listOf("android.permission.BIND_ACCESSIBILITY_SERVICE"),
                riskLevel = RiskLevel.LEVEL_2_SENSITIVE,
                supportsBackground = false
            )
        )

        // 3.2 Application Management
        list.add(
            CapabilityDefinition(
                id = "device.application.list",
                category = "application",
                name = "List Installed Applications",
                description = "Discover installed applications and package identifiers",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_0_INFO,
                supportsBackground = true
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.application.launch",
                category = "application",
                name = "Launch Application",
                description = "Open an application by package name or launcher intent",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_1_ORDINARY,
                supportsBackground = false
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.application.deep_link",
                category = "application",
                name = "Open Deep Link",
                description = "Open a URI scheme or universal link in the target app",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_1_ORDINARY,
                supportsBackground = false
            )
        )

        // 3.3 Device Information
        list.add(
            CapabilityDefinition(
                id = "device.info.system",
                category = "device_info",
                name = "System Hardware and OS Info",
                description = "Query manufacturer, model, Android OS version, and architecture",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_0_INFO,
                supportsBackground = true
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.info.battery",
                category = "device_info",
                name = "Battery Telemetry",
                description = "Query battery charge level, status, and power connection",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_0_INFO,
                supportsBackground = true
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.info.storage",
                category = "device_info",
                name = "Storage Telemetry",
                description = "Inspect free and total internal storage bytes",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_0_INFO,
                supportsBackground = true
            )
        )

        // 3.4 Connectivity
        list.add(
            CapabilityDefinition(
                id = "device.connectivity.status",
                category = "connectivity",
                name = "Network & Connectivity Status",
                description = "Inspect Wi-Fi, cellular, and internet availability",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_0_INFO,
                supportsBackground = true
            )
        )

        // 3.5 Telephony & SIM
        val phoneCallGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
        list.add(
            CapabilityDefinition(
                id = "device.telephony.dial",
                category = "telephony",
                name = "Open Dialler",
                description = "Open phone dialler with prepared phone number",
                available = hasTelephony,
                unavailabilityReason = if (!hasTelephony) "missing_hardware" else null,
                permissionStatus = "not_required",
                riskLevel = RiskLevel.LEVEL_1_ORDINARY,
                supportsBackground = false
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.telephony.call",
                category = "telephony",
                name = "Initiate Direct Phone Call",
                description = "Initiate phone call directly (requires CALL_PHONE permission and user confirmation)",
                available = hasTelephony && phoneCallGranted,
                unavailabilityReason = if (!hasTelephony) "missing_hardware" else if (!phoneCallGranted) "missing_permission" else null,
                permissionStatus = if (phoneCallGranted) "granted" else "not_granted",
                requiredPermissions = listOf(Manifest.permission.CALL_PHONE),
                riskLevel = RiskLevel.LEVEL_3_HIGH,
                supportsBackground = false
            )
        )

        // 3.6 SMS & Messaging
        val sendSmsGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        list.add(
            CapabilityDefinition(
                id = "device.messaging.send_sms",
                category = "messaging",
                name = "Send SMS Message",
                description = "Dispatch SMS message via native SMS manager",
                available = hasTelephony && sendSmsGranted,
                unavailabilityReason = if (!hasTelephony) "missing_hardware" else if (!sendSmsGranted) "missing_permission" else null,
                permissionStatus = if (sendSmsGranted) "granted" else "not_granted",
                requiredPermissions = listOf(Manifest.permission.SEND_SMS),
                riskLevel = RiskLevel.LEVEL_2_SENSITIVE,
                supportsBackground = true
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.messaging.share",
                category = "messaging",
                name = "Share Text or Media",
                description = "Share text or files through Android system share sheet",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_1_ORDINARY,
                supportsBackground = false
            )
        )

        // 3.7 Audio & Microphone
        val recordAudioGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        list.add(
            CapabilityDefinition(
                id = "device.audio.volume",
                category = "audio",
                name = "Audio Volume Control",
                description = "Inspect and adjust media and ring volume levels",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_1_ORDINARY,
                supportsBackground = true
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.audio.tts",
                category = "audio",
                name = "Text-to-Speech Synthesis",
                description = "Synthesize spoken audio speech from text",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_1_ORDINARY,
                supportsBackground = true
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.audio.record",
                category = "audio",
                name = "Record Audio",
                description = "Capture microphone audio recording with visible indication",
                available = recordAudioGranted,
                unavailabilityReason = if (!recordAudioGranted) "missing_permission" else null,
                permissionStatus = if (recordAudioGranted) "granted" else "not_granted",
                requiredPermissions = listOf(Manifest.permission.RECORD_AUDIO),
                riskLevel = RiskLevel.LEVEL_2_SENSITIVE,
                supportsBackground = false
            )
        )

        // 3.8 Camera & Vision
        val cameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        list.add(
            CapabilityDefinition(
                id = "device.camera.torch",
                category = "camera",
                name = "Flashlight / Torch Control",
                description = "Turn device camera flash torch on or off",
                available = hasCamera,
                unavailabilityReason = if (!hasCamera) "missing_hardware" else null,
                permissionStatus = "not_required",
                riskLevel = RiskLevel.LEVEL_1_ORDINARY,
                supportsBackground = true
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.camera.scan_qr",
                category = "camera",
                name = "Scan QR / Barcodes",
                description = "Scan and decode barcode/QR content via camera feed",
                available = hasCamera && cameraGranted,
                unavailabilityReason = if (!hasCamera) "missing_hardware" else if (!cameraGranted) "missing_permission" else null,
                permissionStatus = if (cameraGranted) "granted" else "not_granted",
                requiredPermissions = listOf(Manifest.permission.CAMERA),
                riskLevel = RiskLevel.LEVEL_2_SENSITIVE,
                supportsBackground = false
            )
        )

        // 3.9 Files & Storage
        list.add(
            CapabilityDefinition(
                id = "device.storage.list_files",
                category = "storage",
                name = "List Storage Files",
                description = "List files in authorized app or external document directories",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_1_ORDINARY,
                supportsBackground = true
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.storage.read_file",
                category = "storage",
                name = "Read File Content",
                description = "Read text or bytes from authorized storage location",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_2_SENSITIVE,
                supportsBackground = true
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.storage.write_file",
                category = "storage",
                name = "Write File Content",
                description = "Create or overwrite file in authorized storage location",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_2_SENSITIVE,
                supportsBackground = true
            )
        )

        // 3.10 Notifications
        list.add(
            CapabilityDefinition(
                id = "device.notifications.list",
                category = "notifications",
                name = "List Active Notifications",
                description = "Inspect active notifications from other apps via NotificationListener",
                available = notificationActive,
                unavailabilityReason = if (!notificationActive) "service_disabled" else null,
                permissionStatus = if (notificationActive) "granted" else "not_granted",
                requiredPermissions = listOf("android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"),
                riskLevel = RiskLevel.LEVEL_2_SENSITIVE,
                supportsBackground = true
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.notifications.post",
                category = "notifications",
                name = "Post System Notification",
                description = "Display local notification to the user",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_1_ORDINARY,
                supportsBackground = true
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.notifications.dismiss",
                category = "notifications",
                name = "Dismiss Notification",
                description = "Clear an active notification by notification key",
                available = notificationActive,
                unavailabilityReason = if (!notificationActive) "service_disabled" else null,
                permissionStatus = if (notificationActive) "granted" else "not_granted",
                requiredPermissions = listOf("android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"),
                riskLevel = RiskLevel.LEVEL_2_SENSITIVE,
                supportsBackground = true
            )
        )

        // 3.11 Location
        val fineLocationGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        list.add(
            CapabilityDefinition(
                id = "device.location.get",
                category = "location",
                name = "Get Current GPS Location",
                description = "Obtain current GPS coordinates with user permission",
                available = fineLocationGranted,
                unavailabilityReason = if (!fineLocationGranted) "missing_permission" else null,
                permissionStatus = if (fineLocationGranted) "granted" else "not_granted",
                requiredPermissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION),
                riskLevel = RiskLevel.LEVEL_2_SENSITIVE,
                supportsBackground = false
            )
        )

        // 3.12 Sensors
        list.add(
            CapabilityDefinition(
                id = "device.sensors.read",
                category = "sensors",
                name = "Sample Hardware Sensors",
                description = "Read accelerometer, gyroscope, or ambient light sensor",
                available = hasAccel || hasGyro || hasLight,
                unavailabilityReason = if (!hasAccel && !hasGyro && !hasLight) "missing_hardware" else null,
                permissionStatus = "not_required",
                riskLevel = RiskLevel.LEVEL_0_INFO,
                supportsBackground = true
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.sensors.vibrate",
                category = "sensors",
                name = "Trigger Haptic Vibration",
                description = "Trigger device vibration pattern or haptic feedback",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_1_ORDINARY,
                supportsBackground = true
            )
        )

        // 3.13 Device Settings
        list.add(
            CapabilityDefinition(
                id = "device.settings.open",
                category = "settings",
                name = "Open System Settings Screen",
                description = "Navigate to system settings: wifi, bluetooth, accessibility, apps, battery",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_1_ORDINARY,
                supportsBackground = false
            )
        )

        // 3.14 Automation (Tasker / MacroDroid bridge)
        list.add(
            CapabilityDefinition(
                id = "device.automation.trigger_tasker",
                category = "automation",
                name = "Trigger Tasker Automation",
                description = "Dispatch intent trigger to Tasker automation app",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_3_HIGH,
                supportsBackground = true
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.automation.trigger_macrodroid",
                category = "automation",
                name = "Trigger MacroDroid Automation",
                description = "Dispatch intent action trigger to MacroDroid app",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_3_HIGH,
                supportsBackground = true
            )
        )

        // 3.15 Security & Pairing
        list.add(
            CapabilityDefinition(
                id = "device.security.permissions",
                category = "security",
                name = "Permission Inventory",
                description = "Audit granted and missing system runtime permissions",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_0_INFO,
                supportsBackground = true
            )
        )
        list.add(
            CapabilityDefinition(
                id = "device.security.disconnect",
                category = "security",
                name = "Emergency Disconnect",
                description = "Immediately terminate all agent access and active sessions",
                available = true,
                permissionStatus = "granted",
                riskLevel = RiskLevel.LEVEL_4_CRITICAL,
                supportsBackground = true
            )
        )

        return CapabilityManifest(
            deviceId = deviceId,
            deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
            osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            capabilities = list
        )
    }
}
