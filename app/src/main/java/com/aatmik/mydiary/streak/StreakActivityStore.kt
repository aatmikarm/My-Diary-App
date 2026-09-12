// streak/StreakActivityStore.kt
package com.aatmik.mydiary.streak

import android.content.Context

/**
 * Records the calendar days on which the user genuinely wrote/edited an entry.
 * Deliberately separate from the diary_entries table: seed data, future imports,
 * or backdated edits never touch this, so nothing can inflate a streak that
 * wasn't actually earned. Only the ViewModel's real save path writes here.
 */
class StreakActivityStore(context: Context) {
    private val prefs = context.getSharedPreferences("streak_activity_prefs", Context.MODE_PRIVATE)

    fun recordToday() {
        val days = prefs.getStringSet(KEY_DAYS, emptySet())!!.toMutableSet()
        days.add(StreakEngine.epochDayOf(System.currentTimeMillis()).toString())
        prefs.edit().putStringSet(KEY_DAYS, days).apply()
    }

    fun allActivityTimestamps(): List<Long> =
        prefs.getStringSet(KEY_DAYS, emptySet())!!
            .mapNotNull { it.toLongOrNull() }
            .map { it * StreakEngine.DAY_MILLIS }

    companion object {
        private const val KEY_DAYS = "key_activity_days"
    }
}