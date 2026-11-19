package com.pocketstrike.app.core.protocol

import org.json.JSONArray
import org.json.JSONObject

/**
 * Universal Phone Control Protocol Models for Android (Kotlin)
 * In accordance with px27m universal protocol specification v1.0
 */

enum class RiskLevel(val value: Int) {
    LEVEL_0_INFO(0),
    LEVEL_1_ORDINARY(1),
    LEVEL_2_SENSITIVE(2),
    LEVEL_3_HIGH(3),
    LEVEL_4_CRITICAL(4);

    companion object {
        fun fromInt(v: Int) = entries.firstOrNull { it.value == v } ?: LEVEL_1_ORDINARY
    }
}

enum class AutonomyProfile(val key: String) {
    OBSERVER("observer"),
    ASSISTANT("assistant"),
    OPERATOR("operator"),
    POWER_USER("power_user"),
    RESTRICTED("restricted");

    companion object {
        fun fromKey(k: String) = entries.firstOrNull { it.key.equals(k, ignoreCase = true) } ?: RESTRICTED
    }
}

enum class CommandStatus(val key: String) {
    ACCEPTED("accepted"),
    QUEUED("queued"),
    AWAITING_APPROVAL("awaiting_approval"),
    RUNNING("running"),
    COMPLETED("completed"),
    PARTIALLY_COMPLETED("partially_completed"),
    FAILED("failed"),
    CANCELLED("cancelled"),
    TIMED_OUT("timed_out"),
    UNSUPPORTED("unsupported"),
    PERMISSION_REQUIRED("permission_required")
}

data class CapabilityDefinition(
    val id: String,
    val category: String,
    val name: String,
    val description: String,
    val supportedPlatforms: List<String> = listOf("android"),
    val available: Boolean,
    val unavailabilityReason: String? = null,
    val permissionStatus: String, // "granted", "not_granted", "not_required", "restricted_by_os"
    val requiredPermissions: List<String> = emptyList(),
    val riskLevel: RiskLevel = RiskLevel.LEVEL_1_ORDINARY,
    val supportsBackground: Boolean = false,
    val parametersSchema: String? = null
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("category", category)
        obj.put("name", name)
        obj.put("description", description)
        val platArr = JSONArray()
        supportedPlatforms.forEach { platArr.put(it) }
        obj.put("supported_platforms", platArr)
        obj.put("available", available)
        if (unavailabilityReason != null) obj.put("unavailability_reason", unavailabilityReason)
        obj.put("permission_status", permissionStatus)
        val permArr = JSONArray()
        requiredPermissions.forEach { permArr.put(it) }
        obj.put("required_permissions", permArr)
        obj.put("risk_level", riskLevel.value)
        obj.put("supports_background", supportsBackground)
        if (parametersSchema != null) {
            try {
                obj.put("parameters_schema", JSONObject(parametersSchema))
            } catch (e: Exception) {
                obj.put("parameters_schema", JSONObject())
            }
        }
        return obj
    }
}

data class CapabilityManifest(
    val module: String = "px27m.phone-control",
    val platform: String = "android",
    val moduleVersion: String = "1.0.0",
    val protocolVersion: String = "1.0",
    val deviceId: String,
    val deviceName: String,
    val osVersion: String,
    val connectionMode: String = "hybrid",
    val capabilities: List<CapabilityDefinition>
) {
    fun toJsonString(): String {
        val obj = JSONObject()
        obj.put("module", module)
        obj.put("platform", platform)
        obj.put("module_version", moduleVersion)
        obj.put("protocol_version", protocolVersion)
        obj.put("device_id", deviceId)
        obj.put("device_name", deviceName)
        obj.put("os_version", osVersion)
        obj.put("connection_mode", connectionMode)
        val capArr = JSONArray()
        capabilities.forEach { capArr.put(it.toJsonObject()) }
        obj.put("capabilities", capArr)
        return obj.toString(2)
    }
}

data class CommandExecution(
    val mode: String = "foreground",
    val timeoutMs: Long = 15000L,
    val verifyResult: Boolean = true,
    val retryCount: Int = 0
)

data class CommandAuthorization(
    val agentId: String,
    val agentName: String? = null,
    val autonomyProfile: AutonomyProfile = AutonomyProfile.OPERATOR,
    val approvalToken: String? = null,
    val signature: String? = null,
    val timestampMs: Long = System.currentTimeMillis()
)

data class PhoneControlCommand(
    val commandId: String,
    val capability: String,
    val parameters: JSONObject = JSONObject(),
    val execution: CommandExecution = CommandExecution(),
    val authorization: CommandAuthorization
) {
    companion object {
        fun fromJson(jsonStr: String): PhoneControlCommand {
            val root = JSONObject(jsonStr)
            val cmdId = root.getString("command_id")
            val cap = root.getString("capability")
            val params = root.optJSONObject("parameters") ?: JSONObject()

            val execObj = root.optJSONObject("execution")
            val exec = if (execObj != null) {
                CommandExecution(
                    mode = execObj.optString("mode", "foreground"),
                    timeoutMs = execObj.optLong("timeout_ms", 15000L),
                    verifyResult = execObj.optBoolean("verify_result", true),
                    retryCount = execObj.optInt("retry_count", 0)
                )
            } else CommandExecution()

            val authObj = root.optJSONObject("authorization") ?: JSONObject()
            val auth = CommandAuthorization(
                agentId = authObj.optString("agent_id", "unknown_agent"),
                agentName = authObj.optString("agent_name", null),
                autonomyProfile = AutonomyProfile.fromKey(authObj.optString("autonomy_profile", "operator")),
                approvalToken = authObj.optString("approval_token", null),
                signature = authObj.optString("signature", null),
                timestampMs = authObj.optLong("timestamp_ms", System.currentTimeMillis())
            )

            return PhoneControlCommand(cmdId, cap, params, exec, auth)
        }
    }
}

data class VerificationInfo(
    val verified: Boolean,
    val verificationMethod: String? = null,
    val reason: String? = null
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("verified", verified)
        if (verificationMethod != null) obj.put("verification_method", verificationMethod)
        if (reason != null) obj.put("reason", reason)
        return obj
    }
}

data class ExecutionError(
    val code: String,
    val message: String,
    val recoverable: Boolean = false,
    val details: JSONObject? = null
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("code", code)
        obj.put("message", message)
        obj.put("recoverable", recoverable)
        if (details != null) obj.put("details", details)
        return obj
    }
}

data class ExecutionResult(
    val commandId: String,
    val status: CommandStatus,
    val platform: String = "android",
    val capability: String,
    val result: JSONObject? = null,
    val verification: VerificationInfo,
    val error: ExecutionError? = null,
    val durationMs: Long,
    val timestampMs: Long = System.currentTimeMillis()
) {
    fun toJsonString(): String {
        val obj = JSONObject()
        obj.put("command_id", commandId)
        obj.put("status", status.key)
        obj.put("platform", platform)
        obj.put("capability", capability)
        obj.put("result", result ?: JSONObject.NULL)
        obj.put("verification", verification.toJsonObject())
        obj.put("error", error?.toJsonObject() ?: JSONObject.NULL)
        obj.put("duration_ms", durationMs)
        obj.put("timestamp_ms", timestampMs)
        return obj.toString(2)
    }
}
