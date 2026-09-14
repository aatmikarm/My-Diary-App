package com.aatmik.mydiary.util

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object RemoteConfigManager {
    private const val TAG = "RemoteConfigManager"

    // The single key that controls ads app-wide.
    const val KEY_ADS_ENABLED = "ads_enabled"

    // Controls whether the full onboarding profile (name/age/birthday/gender/goals)
    // is pushed to Firestore. Flip this off remotely if the Privacy Policy / Play
    // Console Data Safety form ever falls out of sync with what's actually being
    // collected — no app update needed.
    const val KEY_PROFILE_FIRESTORE_SYNC_ENABLED = "profile_firestore_sync_enabled"

    // Separate switch for the anonymous profile_setup_completed funnel event sent to
    // Firebase Analytics (no personal fields in it — just a completion signal).
    const val KEY_PROFILE_EVENT_ENABLED = "profile_event_enabled"

    private lateinit var remoteConfig: FirebaseRemoteConfig

    // In-app fallback if Remote Config has never been fetched (e.g. first ever launch,
    // no internet). This is what decides behavior on launch day: keep both false until
    // you've confirmed the policy/Data Safety form are updated, then flip server-side.
    private val defaults = mapOf(
        KEY_ADS_ENABLED to false,
        KEY_PROFILE_FIRESTORE_SYNC_ENABLED to false,
        KEY_PROFILE_EVENT_ENABLED to false
    )

    private val _adsEnabled = MutableStateFlow(false)
    val adsEnabled: StateFlow<Boolean> = _adsEnabled

    private val _profileFirestoreSyncEnabled = MutableStateFlow(false)
    val profileFirestoreSyncEnabled: StateFlow<Boolean> = _profileFirestoreSyncEnabled

    private val _profileEventEnabled = MutableStateFlow(false)
    val profileEventEnabled: StateFlow<Boolean> = _profileEventEnabled

    fun initialize() {
        remoteConfig = FirebaseRemoteConfig.getInstance()

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // re-check for updates once an hour
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(defaults)

        // Reflect whatever was already activated in a previous session immediately,
        // so we don't wait on the network to know the last-known toggle state.
        _adsEnabled.value = remoteConfig.getBoolean(KEY_ADS_ENABLED)
        _profileFirestoreSyncEnabled.value = remoteConfig.getBoolean(KEY_PROFILE_FIRESTORE_SYNC_ENABLED)
        _profileEventEnabled.value = remoteConfig.getBoolean(KEY_PROFILE_EVENT_ENABLED)

        fetchAndApply()
    }

    fun fetchAndApply() {
        Log.d(TAG, "Fetching remote config...")
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                val adsEnabled = remoteConfig.getBoolean(KEY_ADS_ENABLED)
                val profileFirestoreSyncEnabled = remoteConfig.getBoolean(KEY_PROFILE_FIRESTORE_SYNC_ENABLED)
                val profileEventEnabled = remoteConfig.getBoolean(KEY_PROFILE_EVENT_ENABLED)
                _adsEnabled.value = adsEnabled
                _profileFirestoreSyncEnabled.value = profileFirestoreSyncEnabled
                _profileEventEnabled.value = profileEventEnabled
                Log.d(
                    TAG,
                    "fetchAndActivate complete. success=${task.isSuccessful} " +
                            "activated=${task.result} ads_enabled=$adsEnabled " +
                            "profile_firestore_sync_enabled=$profileFirestoreSyncEnabled " +
                            "profile_event_enabled=$profileEventEnabled"
                )
                task.exception?.let { Log.e(TAG, "Fetch failed", it) }
            }
    }
}