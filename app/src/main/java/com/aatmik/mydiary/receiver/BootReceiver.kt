package com.aatmik.mydiary.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aatmik.mydiary.data.ReminderManager
import com.aatmik.mydiary.util.ReminderScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val reminderManager = ReminderManager(context)
            if (reminderManager.isEnabled) {
                ReminderScheduler.scheduleDailyReminder(context, reminderManager.hour, reminderManager.minute)
            }
        }
    }
}