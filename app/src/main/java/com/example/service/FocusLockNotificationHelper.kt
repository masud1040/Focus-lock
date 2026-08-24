package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object FocusLockNotificationHelper {
    const val CHANNEL_PROTECTION = "focuslock_protection_channel"
    const val CHANNEL_ALERTS = "focuslock_alerts_channel"
    const val NOTIFICATION_ID_PROTECTION = 1001
    const val NOTIFICATION_ID_ALERT = 1002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val protectionChannel = NotificationChannel(
                CHANNEL_PROTECTION,
                "FocusLock Protection Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active protection status and remaining schedule time"
                setShowBadge(false)
            }

            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "FocusLock Reminders & Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when daily usage limits are approaching or schedules start"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(protectionChannel)
            notificationManager.createNotificationChannel(alertsChannel)
        }
    }

    fun showProtectionNotification(context: Context, blockedAppsCount: Int, statusText: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PROTECTION)
            .setSmallIcon(R.drawable.focus_lock_icon_1787572031797)
            .setContentTitle("FocusLock is Active")
            .setContentText("$blockedAppsCount apps protected • $statusText")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_PROTECTION, notification)
    }

    fun showUsageAlert(context: Context, appName: String, alertMessage: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.focus_lock_icon_1787572031797)
            .setContentTitle("Focus Alert: $appName")
            .setContentText(alertMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_ALERT, notification)
    }

    fun cancelProtectionNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID_PROTECTION)
    }
}
