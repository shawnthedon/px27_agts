package com.pocketstrike.app.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import org.json.JSONArray
import org.json.JSONObject

/**
 * Px27NotificationListener
 * Intercepts, inspects, and manages system notifications for authorized agents.
 */
class Px27NotificationListener : NotificationListenerService() {

    companion object {
        var instance: Px27NotificationListener? = null
            private set

        val isServiceActive: Boolean
            get() = instance != null

        val recentNotifications = mutableListOf<JSONObject>()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val pkg = sbn.packageName ?: ""

        val notifObj = JSONObject().apply {
            put("key", sbn.key)
            put("package", pkg)
            put("title", title)
            put("text", text)
            put("post_time_ms", sbn.postTime)
            put("is_clearable", sbn.isClearable)
        }

        synchronized(recentNotifications) {
            recentNotifications.add(0, notifObj)
            if (recentNotifications.size > 100) {
                recentNotifications.removeAt(recentNotifications.lastIndex)
            }
        }
    }

    fun getActiveNotificationList(): JSONArray {
        val array = JSONArray()
        try {
            val sbns = activeNotifications ?: return array
            for (sbn in sbns) {
                val extras = sbn.notification.extras
                val obj = JSONObject().apply {
                    put("key", sbn.key)
                    put("package", sbn.packageName)
                    put("title", extras.getCharSequence("android.title")?.toString() ?: "")
                    put("text", extras.getCharSequence("android.text")?.toString() ?: "")
                    put("post_time_ms", sbn.postTime)
                    put("is_clearable", sbn.isClearable)
                }
                array.put(obj)
            }
        } catch (e: Exception) {
            // Permission or security exception
        }
        return array
    }

    fun dismissNotificationByKey(key: String): Boolean {
        return try {
            cancelNotification(key)
            true
        } catch (e: Exception) {
            false
        }
    }
}
