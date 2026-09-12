// data/StreakReminderManager.kt
package com.aatmik.mydiary.data

import android.content.Context

class StreakReminderManager(context: Context) {
    private val prefs = context.getSharedPreferences("streak_reminder_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ENABLED = "key_streak_enabled"
        private const val KEY_HOUR = "key_streak_hour"
        private const val KEY_MINUTE = "key_streak_minute"
        const val DEFAULT_HOUR = 20   // 8:00 PM — leaves time to still write before midnight
        const val DEFAULT_MINUTE = 30
    }

    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    var hour: Int
        get() = prefs.getInt(KEY_HOUR, DEFAULT_HOUR)
        set(value) = prefs.edit().putInt(KEY_HOUR, value).apply()

    var minute: Int
        get() = prefs.getInt(KEY_MINUTE, DEFAULT_MINUTE)
        set(value) = prefs.edit().putInt(KEY_MINUTE, value).apply()
}