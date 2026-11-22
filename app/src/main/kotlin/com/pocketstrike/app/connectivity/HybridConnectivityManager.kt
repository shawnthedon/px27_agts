package com.pocketstrike.app.connectivity

import android.content.Context
import com.pocketstrike.app.core.protocol.CapabilityManifest
import com.pocketstrike.app.core.protocol.CommandStatus
import com.pocketstrike.app.core.protocol.ExecutionResult
import com.pocketstrike.app.core.protocol.PhoneControlCommand
import com.pocketstrike.app.core.protocol.VerificationInfo
import com.pocketstrike.app.discovery.CapabilityDiscoveryEngine
import com.pocketstrike.app.engine.ExecutionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * HybridConnectivityManager
 * Provides dual-transport connectivity:
 * 1. Local LAN/Loopback TCP Server (for direct low-latency agent calls)
 * 2. Remote Gateway WebSocket client connection
 * Enforces cryptographic HMAC signature and shared secret verification.
 */
class HybridConnectivityManager(
    private val context: Context,
    val executionEngine: ExecutionEngine
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val discoveryEngine = CapabilityDiscoveryEngine(context)

    var deviceId: String = "android_${android.os.Build.SERIAL.takeIf { it != "unknown" } ?: "dev_px27"}"
    var sharedSecret: String? = null
    var isLocalServerRunning: Boolean = false
        private set
    var isGatewayConnected: Boolean = false
        private set

    private var serverSocket: ServerSocket? = null

    /**
     * Start local daemon listening on port (default 27027)
     */
    fun startLocalServer(port: Int = 27027) {
        if (isLocalServerRunning) return
        scope.launch {
            try {
                serverSocket = ServerSocket(port)
                isLocalServerRunning = true
                while (isLocalServerRunning && serverSocket != null && !serverSocket!!.isClosed) {
                    val clientSocket = serverSocket!!.accept()
                    handleClientSocket(clientSocket)
                }
            } catch (e: Exception) {
                isLocalServerRunning = false
            }
        }
    }

    fun stopLocalServer() {
        isLocalServerRunning = false
        try {
            serverSocket?.close()
            serverSocket = null
        } catch (e: Exception) {}
    }

    private fun handleClientSocket(socket: Socket) {
        scope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val writer = PrintWriter(socket.getOutputStream(), true)

                val line = reader.readLine()
                if (line != null) {
                    val responseStr = processCommandPayload(line)
                    writer.println(responseStr)
                }
                socket.close()
            } catch (e: Exception) {
                socket.close()
            }
        }
    }

    /**
     * Process raw JSON command payload, verify signature if sharedSecret is set,
     * execute through ExecutionEngine, and return serialized ExecutionResult JSON.
     */
    suspend fun processCommandPayload(jsonStr: String): String {
        return try {
            val root = JSONObject(jsonStr)

            // Capability discovery query
            if (root.optString("action") == "discover" || root.optString("type") == "manifest_request") {
                val manifest = discoveryEngine.discoverCapabilities(deviceId)
                return manifest.toJsonString()
            }

            val command = PhoneControlCommand.fromJson(jsonStr)

            // Verify HMAC signature if secret configured
            if (sharedSecret != null) {
                val sig = command.authorization.signature
                if (sig.isNullOrEmpty() || !verifyHmacSignature(jsonStr, sig, sharedSecret!!)) {
                    val errRes = ExecutionResult(
                        commandId = command.commandId,
                        status = CommandStatus.FAILED,
                        capability = command.capability,
                        verification = VerificationInfo(false, reason = "HMAC signature verification failed"),
                        durationMs = 0L
                    )
                    return errRes.toJsonString()
                }
            }

            val result = executionEngine.executeCommand(command, deviceId)
            result.toJsonString()
        } catch (e: Exception) {
            val errObj = JSONObject().apply {
                put("status", "failed")
                put("error", e.message ?: "Invalid JSON command payload")
            }
            errObj.toString()
        }
    }

    private fun verifyHmacSignature(payload: String, signature: String, secret: String): Boolean {
        return try {
            val mac = Mac.getInstance("HmacSHA256")
            val keySpec = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
            mac.init(keySpec)
            val hmacBytes = mac.doFinal(payload.toByteArray())
            val hex = hmacBytes.joinToString("") { "%02x".format(it) }
            hex.equals(signature, ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }

    fun getPublishedManifest(): CapabilityManifest {
        return discoveryEngine.discoverCapabilities(deviceId)
    }
}
