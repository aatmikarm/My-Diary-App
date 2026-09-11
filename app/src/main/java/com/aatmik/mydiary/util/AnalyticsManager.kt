package com.aatmik.mydiary.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsManager {
    private lateinit var analytics: FirebaseAnalytics

    fun initialize(context: Context) {
        analytics = FirebaseAnalytics.getInstance(context.applicationContext)
    }

    fun logScreenView(screenName: String) {
        log(FirebaseAnalytics.Event.SCREEN_VIEW) {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
    }

    fun log(eventName: String, block: (Bundle.() -> Unit)? = null) {
        if (!::analytics.isInitialized) return
        val bundle = block?.let { Bundle().apply(it) }
        analytics.logEvent(eventName, bundle)
    }

    object Events {
        const val ENTRY_CREATED = "entry_created"
        const val ENTRY_UPDATED = "entry_updated"
        const val ENTRY_DELETED = "entry_deleted"
        const val ENTRY_FAVORITED = "entry_favorited"
        const val ENTRY_UNFAVORITED = "entry_unfavorited"
        const val MOOD_SELECTED = "mood_selected"
        const val SEARCH_PERFORMED = "search_performed"
        const val TAG_FILTER_APPLIED = "tag_filter_applied"
        const val CALENDAR_DATE_SELECTED = "calendar_date_selected"
        const val THEME_CHANGED = "theme_changed"
        const val APP_LOCK_TOGGLED = "app_lock_toggled"
        const val BIOMETRIC_TOGGLED = "biometric_toggled"
        const val ONBOARDING_COMPLETED = "onboarding_completed"
        const val UNLOCK_SUCCESS = "unlock_success"
        const val UNLOCK_FAILED = "unlock_failed"
        const val CLEAR_ALL_DATA = "clear_all_data"
        const val DRAWING_SAVED = "drawing_saved"
        const val DRAWING_DISCARDED = "drawing_discarded"
        const val PHOTO_ADDED = "photo_added"
        const val STICKER_ADDED = "sticker_added"
        const val TAG_ADDED = "tag_added"
        const val ENTRY_EXPORTED = "entry_exported"
        const val ALL_ENTRIES_EXPORTED = "all_entries_exported"

        const val REMINDER_PREFERENCE_SET = "reminder_preference_set"
    }
}