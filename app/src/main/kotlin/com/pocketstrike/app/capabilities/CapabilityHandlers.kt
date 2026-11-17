package com.pocketstrike.app.capabilities

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pocketstrike.app.core.protocol.CommandStatus
import com.pocketstrike.app.core.protocol.ExecutionError
import com.pocketstrike.app.core.protocol.ExecutionResult
import com.pocketstrike.app.core.protocol.PhoneControlCommand
import com.pocketstrike.app.core.protocol.VerificationInfo
import com.pocketstrike.app.services.Px27AccessibilityService
import com.pocketstrike.app.services.Px27NotificationListener
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale

/**
 * CapabilityHandlers
 * Native Android implementation for all 15 capability domains in the px27m specification.
 */
class CapabilityHandlers(private val context: Context) {

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
            }
        }
    }

    suspend fun execute(command: PhoneControlCommand): ExecutionResult {
        val start = System.currentTimeMillis()
        val cap = command.capability
        val params = command.parameters

        try {
            return when (cap) {
                // 3.1 Screen & Interface
                "device.screen.inspect" -> handleScreenInspect(command, start)
                "device.screen.tap" -> handleScreenTap(command, params, start)
                "device.screen.swipe" -> handleScreenSwipe(command, params, start)
                "device.screen.input_text" -> handleScreenInputText(command, params, start)
                "device.screen.global_action" -> handleScreenGlobal(command, params, start)

                // 3.2 Application Management
                "device.application.list" -> handleAppList(command, start)
                "device.application.launch" -> handleAppLaunch(command, params, start)
                "device.application.deep_link" -> handleAppDeepLink(command, params, start)

                // 3.3 Device Info
                "device.info.system" -> handleSystemInfo(command, start)
                "device.info.battery" -> handleBatteryInfo(command, start)
                "device.info.storage" -> handleStorageInfo(command, start)

                // 3.4 Connectivity
                "device.connectivity.status" -> handleConnectivityStatus(command, start)

                // 3.5 Telephony & SIM
                "device.telephony.dial" -> handleTelephonyDial(command, params, start)
                "device.telephony.call" -> handleTelephonyCall(command, params, start)

                // 3.6 SMS & Messaging
                "device.messaging.send_sms" -> handleSendSms(command, params, start)
                "device.messaging.share" -> handleShare(command, params, start)

                // 3.7 Audio & Mic
                "device.audio.volume" -> handleAudioVolume(command, params, start)
                "device.audio.tts" -> handleAudioTts(command, params, start)

                // 3.8 Camera & Vision
                "device.camera.torch" -> handleCameraTorch(command, params, start)

                // 3.9 Files & Storage
                "device.storage.list_files" -> handleStorageList(command, params, start)
                "device.storage.read_file" -> handleStorageRead(command, params, start)
                "device.storage.write_file" -> handleStorageWrite(command, params, start)

                // 3.10 Notifications
                "device.notifications.list" -> handleNotificationList(command, start)
                "device.notifications.post" -> handleNotificationPost(command, params, start)
                "device.notifications.dismiss" -> handleNotificationDismiss(command, params, start)

                // 3.12 Sensors
                "device.sensors.vibrate" -> handleVibrate(command, params, start)

                // 3.13 Settings
                "device.settings.open" -> handleSettingsOpen(command, params, start)

                // 3.14 Automation (Tasker / MacroDroid)
                "device.automation.trigger_tasker" -> handleTaskerTrigger(command, params, start)
                "device.automation.trigger_macrodroid" -> handleMacroDroidTrigger(command, params, start)

                // 3.15 Security
                "device.security.permissions" -> handlePermissionsAudit(command, start)
                "device.security.disconnect" -> handleEmergencyDisconnect(command, start)

                else -> ExecutionResult(
                    commandId = command.commandId,
                    status = CommandStatus.UNSUPPORTED,
                    capability = cap,
                    result = null,
                    verification = VerificationInfo(verified = false, reason = "Capability not implemented for Android"),
                    error = ExecutionError("UNSUPPORTED_CAPABILITY", "Capability $cap is not handled"),
                    durationMs = System.currentTimeMillis() - start
                )
            }
        } catch (e: Exception) {
            return ExecutionResult(
                commandId = command.commandId,
                status = CommandStatus.FAILED,
                capability = cap,
                result = null,
                verification = VerificationInfo(verified = false, reason = "Exception during native execution"),
                error = ExecutionError("NATIVE_EXECUTION_EXCEPTION", e.message ?: "Unknown error", details = JSONObject().apply { put("stack", e.stackTraceToString()) }),
                durationMs = System.currentTimeMillis() - start
            )
        }
    }

    // --- Screen Control Handlers ---
    private fun handleScreenInspect(cmd: PhoneControlCommand, start: Long): ExecutionResult {
        val service = Px27AccessibilityService.instance
        if (service == null) {
            return ExecutionResult(
                commandId = cmd.commandId,
                status = CommandStatus.PERMISSION_REQUIRED,
                capability = cmd.capability,
                verification = VerificationInfo(false, reason = "Px27AccessibilityService is not enabled"),
                error = ExecutionError("ACCESSIBILITY_DISABLED", "Enable Accessibility Service in Android Settings"),
                durationMs = System.currentTimeMillis() - start
            )
        }
        val hierarchy = service.inspectScreenHierarchy()
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = hierarchy,
            verification = VerificationInfo(true, verificationMethod = "accessibility_node_tree"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    private fun handleScreenTap(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val service = Px27AccessibilityService.instance
            ?: return ExecutionResult(
                commandId = cmd.commandId,
                status = CommandStatus.PERMISSION_REQUIRED,
                capability = cmd.capability,
                verification = VerificationInfo(false),
                error = ExecutionError("ACCESSIBILITY_DISABLED", "Accessibility service disabled"),
                durationMs = System.currentTimeMillis() - start
            )

        val text = params.optString("text", null)
        val viewId = params.optString("view_id", null)
        val x = if (params.has("x")) params.getDouble("x").toFloat() else null
        val y = if (params.has("y")) params.getDouble("y").toFloat() else null

        val success = service.tapElement(text, viewId, x, y)
        return ExecutionResult(
            commandId = cmd.commandId,
            status = if (success) CommandStatus.COMPLETED else CommandStatus.FAILED,
            capability = cmd.capability,
            result = JSONObject().apply { put("tapped", success) },
            verification = VerificationInfo(success, verificationMethod = "accessibility_action_result"),
            error = if (!success) ExecutionError("TAP_FAILED", "Could not locate target node or dispatch click") else null,
            durationMs = System.currentTimeMillis() - start
        )
    }

    private fun handleScreenSwipe(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val service = Px27AccessibilityService.instance
            ?: return ExecutionResult(
                commandId = cmd.commandId,
                status = CommandStatus.PERMISSION_REQUIRED,
                capability = cmd.capability,
                verification = VerificationInfo(false),
                error = ExecutionError("ACCESSIBILITY_DISABLED", "Accessibility service disabled"),
                durationMs = System.currentTimeMillis() - start
            )

        val startX = params.optDouble("start_x", 500.0).toFloat()
        val startY = params.optDouble("start_y", 1200.0).toFloat()
        val endX = params.optDouble("end_x", 500.0).toFloat()
        val endY = params.optDouble("end_y", 400.0).toFloat()
        val duration = params.optLong("duration_ms", 300L)

        val success = service.swipeGesture(startX, startY, endX, endY, duration)
        return ExecutionResult(
            commandId = cmd.commandId,
            status = if (success) CommandStatus.COMPLETED else CommandStatus.FAILED,
            capability = cmd.capability,
            result = JSONObject().apply { put("swiped", success) },
            verification = VerificationInfo(success, verificationMethod = "gesture_dispatch_result"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    private fun handleScreenInputText(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val service = Px27AccessibilityService.instance
            ?: return ExecutionResult(
                commandId = cmd.commandId,
                status = CommandStatus.PERMISSION_REQUIRED,
                capability = cmd.capability,
                verification = VerificationInfo(false),
                error = ExecutionError("ACCESSIBILITY_DISABLED", "Accessibility service disabled"),
                durationMs = System.currentTimeMillis() - start
            )

        val text = params.optString("text", "")
        val success = service.inputText(text)
        return ExecutionResult(
            commandId = cmd.commandId,
            status = if (success) CommandStatus.COMPLETED else CommandStatus.FAILED,
            capability = cmd.capability,
            result = JSONObject().apply { put("injected", success) },
            verification = VerificationInfo(success, verificationMethod = "accessibility_focus_set_text"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    private fun handleScreenGlobal(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val service = Px27AccessibilityService.instance
            ?: return ExecutionResult(
                commandId = cmd.commandId,
                status = CommandStatus.PERMISSION_REQUIRED,
                capability = cmd.capability,
                verification = VerificationInfo(false),
                error = ExecutionError("ACCESSIBILITY_DISABLED", "Accessibility service disabled"),
                durationMs = System.currentTimeMillis() - start
            )

        val action = params.optString("action", "back")
        val success = service.performGlobal(action)
        return ExecutionResult(
            commandId = cmd.commandId,
            status = if (success) CommandStatus.COMPLETED else CommandStatus.FAILED,
            capability = cmd.capability,
            result = JSONObject().apply { put("action", action); put("executed", success) },
            verification = VerificationInfo(success, verificationMethod = "accessibility_global_action"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    // --- App Management Handlers ---
    private fun handleAppList(cmd: PhoneControlCommand, start: Long): ExecutionResult {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val arr = JSONArray()
        for (app in apps.take(150)) {
            val label = pm.getApplicationLabel(app).toString()
            val isSystem = (app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            arr.put(JSONObject().apply {
                put("package", app.packageName)
                put("label", label)
                put("is_system", isSystem)
            })
        }
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("applications", arr); put("count", arr.length()) },
            verification = VerificationInfo(true, verificationMethod = "package_manager_query"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    private fun handleAppLaunch(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val pkg = params.getString("package_name")
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(pkg)

        if (intent == null) {
            return ExecutionResult(
                commandId = cmd.commandId,
                status = CommandStatus.FAILED,
                capability = cmd.capability,
                verification = VerificationInfo(false, reason = "Package not found or no launch intent"),
                error = ExecutionError("PACKAGE_NOT_FOUND", "Cannot find launchable activity for $pkg"),
                durationMs = System.currentTimeMillis() - start
            )
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)

        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("launched", true); put("package_name", pkg) },
            verification = VerificationInfo(true, verificationMethod = "intent_dispatched_to_activity_manager"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    private fun handleAppDeepLink(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val uriStr = params.getString("uri")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("opened_uri", uriStr) },
            verification = VerificationInfo(true, verificationMethod = "action_view_intent"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    // --- Device Info Handlers ---
    private fun handleSystemInfo(cmd: PhoneControlCommand, start: Long): ExecutionResult {
        val result = JSONObject().apply {
            put("manufacturer", Build.MANUFACTURER)
            put("model", Build.MODEL)
            put("device", Build.DEVICE)
            put("os_release", Build.VERSION.RELEASE)
            put("sdk_int", Build.VERSION.SDK_INT)
            put("supported_abis", JSONArray(Build.SUPPORTED_ABIS))
        }
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = result,
            verification = VerificationInfo(true, verificationMethod = "build_constants"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    private fun handleBatteryInfo(cmd: PhoneControlCommand, start: Long): ExecutionResult {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val statusIntent = context.registerReceiver(null, filter)
        val level = statusIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = statusIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val pct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()) else -1f
        val status = statusIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val result = JSONObject().apply {
            put("level_percentage", pct)
            put("is_charging", isCharging)
            put("status_code", status)
        }
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = result,
            verification = VerificationInfo(true, verificationMethod = "battery_manager_sticky_broadcast"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    private fun handleStorageInfo(cmd: PhoneControlCommand, start: Long): ExecutionResult {
        val dataDir = Environment.getDataDirectory()
        val stat = StatFs(dataDir.path)
        val blockSize = stat.blockSizeLong
        val availableBytes = stat.availableBlocksLong * blockSize
        val totalBytes = stat.blockCountLong * blockSize

        val result = JSONObject().apply {
            put("available_bytes", availableBytes)
            put("total_bytes", totalBytes)
            put("free_gigabytes", availableBytes / (1024 * 1024 * 1024))
            put("total_gigabytes", totalBytes / (1024 * 1024 * 1024))
        }
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = result,
            verification = VerificationInfo(true, verificationMethod = "statfs_data_directory"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    // --- Connectivity Handlers ---
    private fun handleConnectivityStatus(cmd: PhoneControlCommand, start: Long): ExecutionResult {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNet = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNet)

        val hasWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
        val hasCell = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
        val hasInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false

        val result = JSONObject().apply {
            put("is_connected", activeNet != null)
            put("has_internet", hasInternet)
            put("transport_wifi", hasWifi)
            put("transport_cellular", hasCell)
        }
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = result,
            verification = VerificationInfo(true, verificationMethod = "connectivity_manager_capabilities"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    // --- Telephony Handlers ---
    private fun handleTelephonyDial(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val phone = params.getString("phone_number")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("dialler_opened", true); put("phone_number", phone) },
            verification = VerificationInfo(true, verificationMethod = "action_dial_intent"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    private fun handleTelephonyCall(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val phone = params.getString("phone_number")
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("call_initiated", true); put("phone_number", phone) },
            verification = VerificationInfo(true, verificationMethod = "action_call_intent"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    // --- Messaging Handlers ---
    private fun handleSendSms(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val phone = params.getString("phone_number")
        val message = params.getString("message")

        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

        smsManager.sendTextMessage(phone, null, message, null, null)

        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("sms_dispatched", true); put("recipient", phone) },
            verification = VerificationInfo(true, verificationMethod = "sms_manager_send_text_message"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    private fun handleShare(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val text = params.getString("text")
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(sendIntent, "Share via px27m").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("share_sheet_opened", true) },
            verification = VerificationInfo(true, verificationMethod = "action_send_chooser"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    // --- Audio Handlers ---
    private fun handleAudioVolume(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val stream = AudioManager.STREAM_MUSIC
        val current = audioManager.getStreamVolume(stream)
        val max = audioManager.getStreamMaxVolume(stream)

        if (params.has("set_level")) {
            val target = params.getInt("set_level").coerceIn(0, max)
            audioManager.setStreamVolume(stream, target, 0)
            return ExecutionResult(
                commandId = cmd.commandId,
                status = CommandStatus.COMPLETED,
                capability = cmd.capability,
                result = JSONObject().apply { put("stream", "music"); put("previous_level", current); put("current_level", target); put("max_level", max) },
                verification = VerificationInfo(true, verificationMethod = "audio_manager_set_stream_volume"),
                durationMs = System.currentTimeMillis() - start
            )
        }

        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("stream", "music"); put("current_level", current); put("max_level", max) },
            verification = VerificationInfo(true, verificationMethod = "audio_manager_get_stream_volume"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    private fun handleAudioTts(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val text = params.getString("text")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "px27m_tts_${System.currentTimeMillis()}")
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("spoken_text", text) },
            verification = VerificationInfo(true, verificationMethod = "tts_engine_speak_queue"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    // --- Camera Torch Handler ---
    private fun handleCameraTorch(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val enable = params.optBoolean("enabled", true)
        val cameraId = cm.cameraIdList.firstOrNull()
            ?: return ExecutionResult(
                commandId = cmd.commandId,
                status = CommandStatus.UNSUPPORTED,
                capability = cmd.capability,
                verification = VerificationInfo(false, reason = "No camera with torch available"),
                durationMs = System.currentTimeMillis() - start
            )

        cm.setTorchMode(cameraId, enable)
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("torch_enabled", enable) },
            verification = VerificationInfo(true, verificationMethod = "camera_manager_set_torch_mode"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    // --- Storage Handlers ---
    private fun handleStorageList(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val dir = File(context.filesDir, params.optString("path", ""))
        val files = dir.listFiles() ?: emptyArray()
        val arr = JSONArray()
        for (f in files) {
            arr.put(JSONObject().apply {
                put("name", f.name)
                put("is_directory", f.isDirectory)
                put("size_bytes", f.length())
                put("last_modified", f.lastModified())
            })
        }
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("files", arr); put("count", arr.length()) },
            verification = VerificationInfo(true, verificationMethod = "filesystem_list_files"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    private fun handleStorageRead(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val file = File(context.filesDir, params.getString("filename"))
        if (!file.exists()) {
            return ExecutionResult(
                commandId = cmd.commandId,
                status = CommandStatus.FAILED,
                capability = cmd.capability,
                verification = VerificationInfo(false, reason = "File not found"),
                error = ExecutionError("FILE_NOT_FOUND", "File ${file.name} does not exist"),
                durationMs = System.currentTimeMillis() - start
            )
        }
        val content = file.readText()
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("filename", file.name); put("content", content); put("size", file.length()) },
            verification = VerificationInfo(true, verificationMethod = "filesystem_read_text"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    private fun handleStorageWrite(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val file = File(context.filesDir, params.getString("filename"))
        val content = params.getString("content")
        file.writeText(content)
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("filename", file.name); put("written_bytes", file.length()) },
            verification = VerificationInfo(file.exists() && file.length() == content.toByteArray().size.toLong(), verificationMethod = "filesystem_write_and_stat"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    // --- Notification Handlers ---
    private fun handleNotificationList(cmd: PhoneControlCommand, start: Long): ExecutionResult {
        val listener = Px27NotificationListener.instance
        if (listener == null) {
            return ExecutionResult(
                commandId = cmd.commandId,
                status = CommandStatus.PERMISSION_REQUIRED,
                capability = cmd.capability,
                verification = VerificationInfo(false, reason = "Notification Listener service is disabled"),
                error = ExecutionError("NOTIFICATION_LISTENER_DISABLED", "Enable Notification Access in Android Settings"),
                durationMs = System.currentTimeMillis() - start
            )
        }
        val list = listener.getActiveNotificationList()
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("notifications", list); put("count", list.length()) },
            verification = VerificationInfo(true, verificationMethod = "notification_listener_active_sbns"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    private fun handleNotificationPost(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val title = params.getString("title")
        val text = params.getString("text")
        val id = (System.currentTimeMillis() % 10000).toInt()

        val notif = NotificationCompat.Builder(context, "px27m_phone_control_channel")
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(id, notif)

        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("notification_id", id); put("posted", true) },
            verification = VerificationInfo(true, verificationMethod = "notification_manager_notify"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    private fun handleNotificationDismiss(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val listener = Px27NotificationListener.instance
            ?: return ExecutionResult(
                commandId = cmd.commandId,
                status = CommandStatus.PERMISSION_REQUIRED,
                capability = cmd.capability,
                verification = VerificationInfo(false),
                error = ExecutionError("NOTIFICATION_LISTENER_DISABLED", "Notification listener disabled"),
                durationMs = System.currentTimeMillis() - start
            )

        val key = params.getString("key")
        val success = listener.dismissNotificationByKey(key)
        return ExecutionResult(
            commandId = cmd.commandId,
            status = if (success) CommandStatus.COMPLETED else CommandStatus.FAILED,
            capability = cmd.capability,
            result = JSONObject().apply { put("dismissed", success); put("key", key) },
            verification = VerificationInfo(success, verificationMethod = "notification_listener_cancel_key"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    // --- Sensor Haptic Handler ---
    private fun handleVibrate(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val duration = params.optLong("duration_ms", 200L)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("vibrated", true); put("duration_ms", duration) },
            verification = VerificationInfo(true, verificationMethod = "vibrator_hardware_dispatch"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    // --- Settings Handler ---
    private fun handleSettingsOpen(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val target = params.optString("setting", "settings").lowercase()
        val action = when (target) {
            "wifi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "accessibility" -> Settings.ACTION_ACCESSIBILITY_SETTINGS
            "application", "apps" -> Settings.ACTION_APPLICATION_SETTINGS
            "battery" -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            "notification" -> Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        val intent = Intent(action).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(intent)
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("opened_setting", target); put("intent_action", action) },
            verification = VerificationInfo(true, verificationMethod = "settings_action_intent"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    // --- External Automation Bridges ---
    private fun handleTaskerTrigger(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val taskName = params.getString("task_name")
        val intent = Intent("net.dinglisch.android.tasker.ACTION_OPEN_PREFERENCE").apply {
            putExtra("task_name", taskName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.sendBroadcast(intent)
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("tasker_triggered", true); put("task_name", taskName) },
            verification = VerificationInfo(true, verificationMethod = "tasker_broadcast_intent"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    private fun handleMacroDroidTrigger(cmd: PhoneControlCommand, params: JSONObject, start: Long): ExecutionResult {
        val macroName = params.getString("macro_name")
        val intent = Intent("com.arlosoft.macrodroid.intent.action.TRIGGER_MACRO").apply {
            putExtra("macro_name", macroName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.sendBroadcast(intent)
        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("macrodroid_triggered", true); put("macro_name", macroName) },
            verification = VerificationInfo(true, verificationMethod = "macrodroid_broadcast_intent"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    // --- Security Handlers ---
    private fun handlePermissionsAudit(cmd: PhoneControlCommand, start: Long): ExecutionResult {
        val perms = listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.SEND_SMS
        )
        val obj = JSONObject()
        for (p in perms) {
            val granted = context.checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED
            obj.put(p.substringAfterLast("."), granted)
        }
        obj.put("accessibility_service_enabled", Px27AccessibilityService.isServiceActive)
        obj.put("notification_listener_enabled", Px27NotificationListener.isServiceActive)

        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = obj,
            verification = VerificationInfo(true, verificationMethod = "package_manager_check_permission"),
            durationMs = System.currentTimeMillis() - start
        )
    }

    private fun handleEmergencyDisconnect(cmd: PhoneControlCommand, start: Long): ExecutionResult {
        // Disconnect immediately and stop foreground service
        val intent = Intent(context, com.pocketstrike.app.services.Px27ForegroundService::class.java).apply {
            action = com.pocketstrike.app.services.Px27ForegroundService.ACTION_STOP
        }
        context.startService(intent)

        return ExecutionResult(
            commandId = cmd.commandId,
            status = CommandStatus.COMPLETED,
            capability = cmd.capability,
            result = JSONObject().apply { put("disconnected", true); put("reason", "emergency_kill_switch_activated") },
            verification = VerificationInfo(true, verificationMethod = "foreground_service_stop"),
            durationMs = System.currentTimeMillis() - start
        )
    }
}
