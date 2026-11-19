package com.pocketstrike.app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pocketstrike.app.MainActivity

/**
 * Px27ForegroundService
 * Maintains continuous hybrid connectivity daemon and foreground state.
 */
class Px27ForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "px27m_phone_control_channel"
        const val NOTIFICATION_ID = 2701
        const val ACTION_START = "com.pocketstrike.app.action.START_SERVICE"
        const val ACTION_STOP = "com.pocketstrike.app.action.STOP_SERVICE"

        var isRunning: Boolean = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            isRunning = false
            return START_NOT_STICKY
        }

        val notification = buildForegroundNotification("Connected to px27m platform (Hybrid)")
        startForeground(NOTIFICATION_ID, notification)
        isRunning = true

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "px27m Phone Control Daemon",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps hybrid local & gateway command listeners active"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(statusText: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("px27m Phone Control")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
