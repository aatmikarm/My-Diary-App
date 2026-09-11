package com.aatmik.mydiary.data

import android.content.Context
import android.content.SharedPreferences

enum class ReminderPreset(val hour: Int, val minute: Int, val label: String) {
    MORNING(8, 0, "Morning"),
    AFTERNOON(13, 0, "Afternoon"),
    EVENING(21, 0, "Evening"),
    CUSTOM(-1, -1, "Custom"),
    OFF(-1, -1, "Don't remind me");

    companion object {
        fun fromName(name: String?): ReminderPreset =
            entries.firstOrNull { it.name == name } ?: EVENING
    }
}

class ReminderManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("diary_reminder_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PRESET = "key_reminder_preset"
        private const val KEY_HOUR = "key_reminder_hour"
        private const val KEY_MINUTE = "key_reminder_minute"
        private const val KEY_ENABLED = "key_reminder_enabled"

        const val DEFAULT_HOUR = 21 // 9:00 PM — best default for a reflective diary reminder
        const val DEFAULT_MINUTE = 0
        val DEFAULT_PRESET = ReminderPreset.EVENING
    }

    var preset: ReminderPreset
        get() = ReminderPreset.fromName(prefs.getString(KEY_PRESET, DEFAULT_PRESET.name))
        set(value) = prefs.edit().putString(KEY_PRESET, value.name).apply()

    var hour: Int
        get() = prefs.getInt(KEY_HOUR, DEFAULT_HOUR)
        set(value) = prefs.edit().putInt(KEY_HOUR, value).apply()

    var minute: Int
        get() = prefs.getInt(KEY_MINUTE, DEFAULT_MINUTE)
        set(value) = prefs.edit().putInt(KEY_MINUTE, value).apply()

    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true) // ON at 9 PM by default unless user opts out
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    /** Applies a preset chosen in onboarding or Settings; persists preset + concrete time + enabled flag. */
    fun applyPreset(selected: ReminderPreset, customHour: Int = DEFAULT_HOUR, customMinute: Int = DEFAULT_MINUTE) {
        preset = selected
        when (selected) {
            ReminderPreset.OFF -> isEnabled = false
            ReminderPreset.CUSTOM -> {
                isEnabled = true
                hour = customHour
                minute = customMinute
            }
            else -> {
                isEnabled = true
                hour = selected.hour
                minute = selected.minute
            }
        }
    }
}