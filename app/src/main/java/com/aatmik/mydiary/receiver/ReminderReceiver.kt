package com.aatmik.mydiary.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aatmik.mydiary.data.ReminderManager
import com.aatmik.mydiary.util.NotificationHelper
import com.aatmik.mydiary.util.ReminderScheduler

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderManager = ReminderManager(context)
        if (!reminderManager.isEnabled) return

        NotificationHelper.createChannelIfNeeded(context)
        NotificationHelper.showReminderNotification(context)

        // Re-arm for tomorrow, same time
        ReminderScheduler.scheduleDailyReminder(context, reminderManager.hour, reminderManager.minute)
    }
}