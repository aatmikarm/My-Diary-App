// util/StreakReminderScheduler.kt
package com.aatmik.mydiary.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.aatmik.mydiary.receiver.StreakReminderReceiver

object StreakReminderScheduler {
    private const val REQUEST_CODE = 2002 // different from ReminderScheduler's 2001

    private fun buildPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, StreakReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleDailyCheck(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            ReminderScheduler.nextTriggerMillis(hour, minute), // reused, already public
            buildPendingIntent(context)
        )
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(buildPendingIntent(context))
    }
}