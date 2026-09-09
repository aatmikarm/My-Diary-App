package com.example.data

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

class SecurityManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("diary_security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FIRST_LAUNCH = "key_first_launch"
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_PIN_LENGTH = "key_pin_length"
        private const val KEY_APP_LOCK_ENABLED = "key_app_lock_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val KEY_TODAY_MOOD = "key_today_mood"
        private const val KEY_THEME_MODE = "key_theme_mode" // "System", "Light", "Dark"
        private const val KEY_SHOW_WORD_COUNT = "key_show_word_count"
    }

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()

    var isAppLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_APP_LOCK_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, value).apply()

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    var pinLength: Int
        get() = prefs.getInt(KEY_PIN_LENGTH, 4)
        set(value) = prefs.edit().putInt(KEY_PIN_LENGTH, value).apply()

    var todayMood: String
        get() = prefs.getString(KEY_TODAY_MOOD, "Happy") ?: "Happy"
        set(value) = prefs.edit().putString(KEY_TODAY_MOOD, value).apply()

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "Light") ?: "Light"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    var showWordCount: Boolean
        get() = prefs.getBoolean(KEY_SHOW_WORD_COUNT, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_WORD_COUNT, value).apply()

    fun setPin(pin: String) {
        val hash = hashPin(pin)
        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putInt(KEY_PIN_LENGTH, pin.length)
            .putBoolean(KEY_APP_LOCK_ENABLED, true)
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return true
        return hashPin(pin) == storedHash
    }

    fun hasPin(): Boolean {
        return prefs.getString(KEY_PIN_HASH, null) != null
    }

    fun clearPin() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_APP_LOCK_ENABLED, false)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .apply()
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
