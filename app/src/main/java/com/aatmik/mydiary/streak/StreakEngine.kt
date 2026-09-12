// streak/StreakEngine.kt
package com.aatmik.mydiary.streak

import java.util.Calendar

object StreakEngine {

    data class StreakResult(
        val currentStreak: Int,
        val longestStreak: Int,
        val isTodayLogged: Boolean,
        val isAtRisk: Boolean
    )

    const val DAY_MILLIS = 24L * 60 * 60 * 1000

    fun calculate(activityTimestampsMillis: List<Long>): StreakResult {
        if (activityTimestampsMillis.isEmpty()) return StreakResult(0, 0, false, false)

        val days = activityTimestampsMillis.map { epochDayOf(it) }.toSortedSet()
        val todayDay = epochDayOf(System.currentTimeMillis())
        val isTodayLogged = todayDay in days

        var current = 0
        var cursor = if (isTodayLogged) todayDay else todayDay - 1
        while (cursor in days) {
            current++
            cursor--
        }

        var longest = 0
        var run = 0
        var prev: Long? = null
        for (day in days) {
            run = if (prev != null && day == prev + 1) run + 1 else 1
            longest = maxOf(longest, run)
            prev = day
        }

        return StreakResult(current, longest, isTodayLogged, isAtRisk = !isTodayLogged && current > 0)
    }

    /** Public so other classes (e.g. an activity log) can key off the same day boundary. */
    fun epochDayOf(millis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis / DAY_MILLIS
    }
}