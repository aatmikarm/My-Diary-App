package com.aatmik.mydiary.data

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

/**
 * Stores the user's personalization profile (name, birth date, gender/pronoun,
 * journaling goals) captured during onboarding. Age is never stored directly —
 * it's computed from the birth date via [calculateAge] so it's always correct,
 * not a stale number frozen at onboarding time. Everything here lives in local
 * SharedPreferences on-device, matching how PIN/reminder prefs are already
 * handled (see SecurityManager, ReminderManager).
 *
 * NOTE: this full profile (name included) is pushed to Firestore, not Firebase
 * Analytics (see ProfileRemoteSync / DiaryViewModel.completeProfileSetup), now that
 * the Privacy Policy has been updated to disclose this collection. If the policy
 * ever reverts to "we don't collect your name," this and the Firestore push need
 * to be updated together — otherwise the app is capturing personal data past its
 * Play Store data-safety declaration, independent of anything Anthropic does.
 */
class ProfileManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("diary_profile_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PROFILE_SETUP_DONE = "key_profile_setup_done"
        private const val KEY_NAME = "key_profile_name"
        private const val KEY_BIRTHDAY_YEAR = "key_profile_birthday_year"   // e.g. 1998, -1 if unset
        private const val KEY_BIRTHDAY_MONTH = "key_profile_birthday_month" // 1-12, -1 if unset
        private const val KEY_BIRTHDAY_DAY = "key_profile_birthday_day"     // 1-31, -1 if unset
        private const val KEY_GENDER = "key_profile_gender"
        private const val KEY_GOALS = "key_profile_goals" // comma-separated
        private const val KEY_INSTALL_ID = "key_profile_install_id"

        /**
         * Pure age calculation, usable both here and from ProfileSetupScreen for the
         * live "you're X" preview before the profile is even saved.
         */
        fun calculateAgeFrom(year: Int?, month: Int?, day: Int?): Int? {
            if (year == null || month == null || day == null) return null
            if (year < 1900 || month !in 1..12 || day !in 1..31) return null
            val today = Calendar.getInstance()
            val currentYear = today.get(Calendar.YEAR)
            val currentMonth = today.get(Calendar.MONTH) + 1
            val currentDay = today.get(Calendar.DAY_OF_MONTH)
            var age = currentYear - year
            if (currentMonth < month || (currentMonth == month && currentDay < day)) {
                age--
            }
            return age.takeIf { it in 0..130 }
        }
    }

    var isProfileSetupDone: Boolean
        get() = prefs.getBoolean(KEY_PROFILE_SETUP_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_PROFILE_SETUP_DONE, value).apply()

    var name: String
        get() = prefs.getString(KEY_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_NAME, value).apply()

    var birthdayYear: Int
        get() = prefs.getInt(KEY_BIRTHDAY_YEAR, -1)
        set(value) = prefs.edit().putInt(KEY_BIRTHDAY_YEAR, value).apply()

    var birthdayMonth: Int
        get() = prefs.getInt(KEY_BIRTHDAY_MONTH, -1)
        set(value) = prefs.edit().putInt(KEY_BIRTHDAY_MONTH, value).apply()

    var birthdayDay: Int
        get() = prefs.getInt(KEY_BIRTHDAY_DAY, -1)
        set(value) = prefs.edit().putInt(KEY_BIRTHDAY_DAY, value).apply()

    var gender: String
        get() = prefs.getString(KEY_GENDER, "") ?: ""
        set(value) = prefs.edit().putString(KEY_GENDER, value).apply()

    var goals: Set<String>
        get() = (prefs.getString(KEY_GOALS, "") ?: "")
            .split(",")
            .filter { it.isNotBlank() }
            .toSet()
        set(value) = prefs.edit().putString(KEY_GOALS, value.joinToString(",")).apply()

    /** True if today matches the stored birthday (month + day only, year-agnostic). */
    fun isBirthdayToday(): Boolean {
        if (birthdayMonth < 1 || birthdayDay < 1) return false
        val cal = Calendar.getInstance()
        val todayMonth = cal.get(Calendar.MONTH) + 1
        val todayDay = cal.get(Calendar.DAY_OF_MONTH)
        return todayMonth == birthdayMonth && todayDay == birthdayDay
    }

    /**
     * Age computed from the stored birth date — never stored as its own field, so it's
     * always correct (no "age" value going stale a year after onboarding). Returns null
     * if the birth date was never provided or is incomplete.
     */
    fun calculateAge(): Int? = calculateAgeFrom(birthdayYear, birthdayMonth, birthdayDay)

    /** Name to greet the user with on Home/notifications — null if never provided. */
    fun greetingName(): String? = name.trim().ifBlank { null }

    /** Stable per-install identifier, generated once, used as the Firestore document ID
     *  so re-running setup (or a future edit-profile screen) updates the same document
     *  instead of creating a duplicate. */
    fun getOrCreateInstallId(): String {
        var id = prefs.getString(KEY_INSTALL_ID, null)
        if (id.isNullOrBlank()) {
            id = java.util.UUID.randomUUID().toString()
            prefs.edit().putString(KEY_INSTALL_ID, id).apply()
        }
        return id
    }

    fun saveProfile(
        name: String,
        birthdayYear: Int?,
        birthdayMonth: Int?,
        birthdayDay: Int?,
        gender: String,
        goals: Set<String>
    ) {
        this.name = name.trim()
        this.birthdayYear = birthdayYear ?: -1
        this.birthdayMonth = birthdayMonth ?: -1
        this.birthdayDay = birthdayDay ?: -1
        this.gender = gender
        this.goals = goals
        this.isProfileSetupDone = true
    }
}