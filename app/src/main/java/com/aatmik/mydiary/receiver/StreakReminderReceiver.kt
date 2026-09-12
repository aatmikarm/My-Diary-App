// receiver/StreakReminderReceiver.kt
package com.aatmik.mydiary.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aatmik.mydiary.data.DiaryDatabase
import com.aatmik.mydiary.data.StreakReminderManager
import com.aatmik.mydiary.streak.StreakActivityStore
import com.aatmik.mydiary.streak.StreakEngine
import com.aatmik.mydiary.util.NotificationHelper
import com.aatmik.mydiary.util.StreakReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StreakReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val streakReminderManager = StreakReminderManager(context)
        if (!streakReminderManager.isEnabled) return

        val result = StreakEngine.calculate(StreakActivityStore(context).allActivityTimestamps())
        if (result.isAtRisk) {
            NotificationHelper.createStreakChannelIfNeeded(context)
            NotificationHelper.showStreakAtRiskNotification(context, result.currentStreak)
        }

        StreakReminderScheduler.scheduleDailyCheck(context, streakReminderManager.hour, streakReminderManager.minute)
    }
}