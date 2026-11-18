package com.pocketstrike.app.engine

import android.content.Context
import com.pocketstrike.app.capabilities.CapabilityHandlers
import com.pocketstrike.app.core.protocol.AutonomyProfile
import com.pocketstrike.app.core.protocol.CapabilityDefinition
import com.pocketstrike.app.core.protocol.CommandStatus
import com.pocketstrike.app.core.protocol.ExecutionError
import com.pocketstrike.app.core.protocol.ExecutionResult
import com.pocketstrike.app.core.protocol.PhoneControlCommand
import com.pocketstrike.app.core.protocol.RiskLevel
import com.pocketstrike.app.core.protocol.VerificationInfo
import com.pocketstrike.app.discovery.CapabilityDiscoveryEngine
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

/**
 * 10-Component Power-User Execution Engine for px27m Android Module
 * In accordance with Section 4 of px27m universal phone control specification.
 */
class ExecutionEngine(private val context: Context) {

    private val discoveryEngine = CapabilityDiscoveryEngine(context)
    private val handlers = CapabilityHandlers(context)
    val auditLog = mutableListOf<ExecutionResult>()
    var maxDeviceAutonomy: AutonomyProfile = AutonomyProfile.POWER_USER
    var isEmergencyDisconnected: Boolean = false

    // 1. Command Planner: parses structured steps from agent instructions
    fun planSteps(instruction: String): List<String> {
        val lower = instruction.lowercase()
        val steps = mutableListOf<String>()
        if (lower.contains("battery") || lower.contains("power") || lower.contains("charge")) steps.add("device.info.battery")
        if (lower.contains("wifi") || lower.contains("network") || lower.contains("connection")) steps.add("device.connectivity.status")
        if (lower.contains("notification") || lower.contains("alert")) steps.add("device.notifications.list")
        if (lower.contains("storage") || lower.contains("space") || lower.contains("disk")) steps.add("device.info.storage")
        if (lower.contains("inspect") || lower.contains("screen")) steps.add("device.screen.inspect")
        if (lower.contains("torch") || lower.contains("flash")) steps.add("device.hardware.torch")
        if (lower.contains("vibrate") || lower.contains("haptic")) steps.add("device.hardware.vibrate")
        if (lower.contains("location") || lower.contains("gps")) steps.add("device.sensors.location")
        if (lower.contains("brightness")) steps.add("device.settings.brightness")
        if (lower.contains("clipboard") || lower.contains("paste")) steps.add("device.clipboard.read")
        return steps
    }

    // 2. Capability Resolver: matches capability ID to advertised manifest definition
    fun resolveCapability(capabilityId: String, deviceId: String): CapabilityDefinition? {
        val manifest = discoveryEngine.discoverCapabilities(deviceId)
        return manifest.capabilities.firstOrNull { it.id == capabilityId }
    }

    // 3. Permission Manager: checks required access
    fun checkPermission(capDef: CapabilityDefinition): Boolean {
        return capDef.permissionStatus == "granted" || capDef.permissionStatus == "not_required"
    }

    // 4. Policy Engine: enforces user-defined restrictions
    fun evaluatePolicy(command: PhoneControlCommand, capDef: CapabilityDefinition): Pair<Boolean, String?> {
        if (isEmergencyDisconnected) {
            return false to "Device is in emergency disconnect state"
        }

        val risk = capDef.riskLevel
        val agentProfile = command.authorization.autonomyProfile

        if (agentProfile == AutonomyProfile.OBSERVER && risk.value > RiskLevel.LEVEL_0_INFO.value) {
            return false to "Observer profile is restricted to non-sensitive device info (Risk 0)"
        }
        if (agentProfile == AutonomyProfile.ASSISTANT && risk.value > RiskLevel.LEVEL_1_ORDINARY.value) {
            return false to "Assistant profile is restricted from sensitive operations (Risk >= 2)"
        }
        return true to null
    }

    // 5. Approval Gate: checks user approval for Risk Tier 3 & 4
    fun checkApproval(command: PhoneControlCommand, capDef: CapabilityDefinition): Pair<Boolean, String?> {
        val risk = capDef.riskLevel
        if (risk == RiskLevel.LEVEL_3_HIGH && command.authorization.approvalToken.isNullOrEmpty()) {
            return false to "Risk Level 3 operation requires explicit user approval token"
        }
        if (risk == RiskLevel.LEVEL_4_CRITICAL && command.authorization.approvalToken.isNullOrEmpty()) {
            return false to "Risk Level 4 operation strictly requires explicit user confirmation"
        }
        return true to null
    }

    // 6. Workflow Executor: runs execution with timeout & cancellation
    suspend fun executeCommand(command: PhoneControlCommand, deviceId: String = "local_android"): ExecutionResult {
        val start = System.currentTimeMillis()

        // Step 2: Resolve
        val capDef = resolveCapability(command.capability, deviceId)
        if (capDef == null) {
            return logAudit(
                ExecutionResult(
                    commandId = command.commandId,
                    status = CommandStatus.UNSUPPORTED,
                    capability = command.capability,
                    verification = VerificationInfo(false, reason = "Capability not recognized by device"),
                    error = ExecutionError("UNKNOWN_CAPABILITY", "Capability ${command.capability} is not advertised"),
                    durationMs = System.currentTimeMillis() - start
                )
            )
        }

        // Step 3: Check Permissions
        if (!checkPermission(capDef)) {
            return logAudit(
                ExecutionResult(
                    commandId = command.commandId,
                    status = CommandStatus.PERMISSION_REQUIRED,
                    capability = command.capability,
                    verification = VerificationInfo(false, reason = "Missing required Android permission"),
                    error = ExecutionError("PERMISSION_MISSING", "Grant permissions: ${capDef.requiredPermissions.joinToString()}"),
                    durationMs = System.currentTimeMillis() - start
                )
            )
        }

        // Step 4: Policy Engine
        val (policyAllowed, policyReason) = evaluatePolicy(command, capDef)
        if (!policyAllowed) {
            return logAudit(
                ExecutionResult(
                    commandId = command.commandId,
                    status = CommandStatus.FAILED,
                    capability = command.capability,
                    verification = VerificationInfo(false, reason = policyReason),
                    error = ExecutionError("POLICY_VIOLATION", policyReason ?: "Policy check failed"),
                    durationMs = System.currentTimeMillis() - start
                )
            )
        }

        // Step 5: Approval Gate
        val (approvalGranted, approvalReason) = checkApproval(command, capDef)
        if (!approvalGranted) {
            return logAudit(
                ExecutionResult(
                    commandId = command.commandId,
                    status = CommandStatus.AWAITING_APPROVAL,
                    capability = command.capability,
                    verification = VerificationInfo(false, reason = approvalReason),
                    error = ExecutionError("APPROVAL_REQUIRED", approvalReason ?: "User approval required"),
                    durationMs = System.currentTimeMillis() - start
                )
            )
        }

        // Step 6 & 8: Execute with Timeout & Recovery
        return try {
            val timeout = command.execution.timeoutMs.coerceIn(500L, 60000L)
            val result = withTimeout(timeout) {
                handlers.execute(command)
            }
            logAudit(result)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            logAudit(
                ExecutionResult(
                    commandId = command.commandId,
                    status = CommandStatus.TIMED_OUT,
                    capability = command.capability,
                    verification = VerificationInfo(false, reason = "Execution exceeded timeout of ${command.execution.timeoutMs}ms"),
                    error = ExecutionError("TIMEOUT", "Execution timed out"),
                    durationMs = System.currentTimeMillis() - start
                )
            )
        } catch (e: Exception) {
            logAudit(
                ExecutionResult(
                    commandId = command.commandId,
                    status = CommandStatus.FAILED,
                    capability = command.capability,
                    verification = VerificationInfo(false, reason = "Exception during native execution"),
                    error = ExecutionError("EXECUTION_ERROR", e.message ?: "Unknown error"),
                    durationMs = System.currentTimeMillis() - start
                )
            )
        }
    }

    // 10. Audit Engine: records local execution history
    private fun logAudit(res: ExecutionResult): ExecutionResult {
        synchronized(auditLog) {
            auditLog.add(0, res)
            if (auditLog.size > 200) auditLog.removeAt(auditLog.lastIndex)
        }
        return res
    }
}
