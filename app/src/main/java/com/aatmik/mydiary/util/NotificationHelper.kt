package com.aatmik.mydiary.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aatmik.mydiary.MainActivity
import com.aatmik.mydiary.R
import com.aatmik.mydiary.ui.theme.DiaryPink

object NotificationHelper {
    // Bumped from "diary_reminder_channel" — channel settings are locked in on first
    // creation and won't update on old installs, so a new id forces correct defaults.
    const val CHANNEL_ID = "diary_reminder_channel_v2"
    const val NOTIFICATION_ID = 1001

    fun createChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Diary Reminders",
                    NotificationManager.IMPORTANCE_HIGH // HIGH so sound/heads-up isn't silently downgraded on some OEMs
                ).apply {
                    description = "Daily reminder to write in your diary"
                    setSound(soundUri, audioAttributes)
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
            // Clean up the old silent channel if it exists on upgrade
            manager.deleteNotificationChannel("diary_reminder_channel")
        }
    }

    fun showReminderNotification(context: Context) {
        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return

        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.diary_icon)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setColor(DiaryPink.toArgb())
            .setContentTitle("Time to write ❤️")
            .setContentText("How was your day? Capture it before you forget.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE) // fallback for pre-O
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}