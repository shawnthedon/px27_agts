package com.pocketstrike.app.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import org.json.JSONArray
import org.json.JSONObject

/**
 * Px27AccessibilityService
 * Deep Android screen and interface automation service for authorized px27m agents.
 */
class Px27AccessibilityService : AccessibilityService() {

    companion object {
        var instance: Px27AccessibilityService? = null
            private set

        val isServiceActive: Boolean
            get() = instance != null

        var currentForegroundPackage: String = ""
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString()
            if (!pkg.isNullOrEmpty()) {
                currentForegroundPackage = pkg
            }
        }
    }

    override fun onInterrupt() {
        // Handle interruption
    }

    /**
     * Inspects active window hierarchy and returns structured JSON of accessible nodes
     */
    fun inspectScreenHierarchy(): JSONObject {
        val root = rootInActiveWindow ?: return JSONObject().apply {
            put("error", "No active window accessible")
            put("nodes", JSONArray())
        }

        val result = JSONObject()
        result.put("foreground_package", currentForegroundPackage)
        val nodesArray = JSONArray()
        traverseNode(root, nodesArray, 0)
        result.put("nodes", nodesArray)
        return result
    }

    private fun traverseNode(node: AccessibilityNodeInfo?, array: JSONArray, depth: Int) {
        if (node == null || depth > 20) return

        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        val nodeObj = JSONObject()
        nodeObj.put("class", node.className?.toString() ?: "")
        nodeObj.put("text", node.text?.toString() ?: "")
        nodeObj.put("content_description", node.contentDescription?.toString() ?: "")
        nodeObj.put("view_id", node.viewIdResourceName ?: "")
        nodeObj.put("clickable", node.isClickable)
        nodeObj.put("editable", node.isEditable)
        nodeObj.put("focused", node.isFocused)
        nodeObj.put("bounds", JSONObject().apply {
            put("left", bounds.left)
            put("top", bounds.top)
            put("right", bounds.right)
            put("bottom", bounds.bottom)
        })

        if (!node.text.isNullOrEmpty() || !node.contentDescription.isNullOrEmpty() || node.isClickable || node.isEditable) {
            array.put(nodeObj)
        }

        for (i in 0 until node.childCount) {
            traverseNode(node.getChild(i), array, depth + 1)
        }
    }

    /**
     * Clicks an accessible element matching resourceId, text, or coordinates
     */
    fun tapElement(text: String?, viewId: String?, x: Float?, y: Float?): Boolean {
        val root = rootInActiveWindow
        if (root != null) {
            if (!viewId.isNullOrEmpty()) {
                val matches = root.findAccessibilityNodeInfosByViewId(viewId)
                for (match in matches) {
                    if (match.isClickable && match.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        return true
                    }
                }
            }

            if (!text.isNullOrEmpty()) {
                val matches = root.findAccessibilityNodeInfosByText(text)
                for (match in matches) {
                    if (match.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        return true
                    }
                }
            }
        }

        // Coordinate tap fallback via gesture dispatch
        if (x != null && y != null) {
            return dispatchClickGesture(x, y)
        }

        return false
    }

    /**
     * Injects text into currently focused editable field
     */
    fun inputText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return false

        val arguments = Bundle()
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        return focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    /**
     * Performs swipe gesture across screen coordinates
     */
    fun swipeGesture(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long = 300L): Boolean {
        val path = Path()
        path.moveTo(startX, startY)
        path.lineTo(endX, endY)
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null)
    }

    private fun dispatchClickGesture(x: Float, y: Float): Boolean {
        val path = Path()
        path.moveTo(x, y)
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null)
    }

    /**
     * Triggers global navigation actions
     */
    fun performGlobal(actionName: String): Boolean {
        val actionId = when (actionName.lowercase()) {
            "back" -> GLOBAL_ACTION_BACK
            "home" -> GLOBAL_ACTION_HOME
            "recents" -> GLOBAL_ACTION_RECENTS
            "notifications" -> GLOBAL_ACTION_NOTIFICATIONS
            "quick_settings" -> GLOBAL_ACTION_QUICK_SETTINGS
            else -> return false
        }
        return performGlobalAction(actionId)
    }
}
