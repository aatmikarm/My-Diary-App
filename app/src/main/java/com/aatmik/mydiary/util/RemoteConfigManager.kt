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

    private lateinit var remoteConfig: FirebaseRemoteConfig

    // In-app fallback if Remote Config has never been fetched (e.g. first ever launch,
    // no internet). This is what decides behavior on launch day: keep it false.
    private val defaults = mapOf(KEY_ADS_ENABLED to false)

    private val _adsEnabled = MutableStateFlow(false)
    val adsEnabled: StateFlow<Boolean> = _adsEnabled

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

        fetchAndApply()
    }

    fun fetchAndApply() {
        Log.d(TAG, "Fetching remote config...")
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                val enabled = remoteConfig.getBoolean(KEY_ADS_ENABLED)
                val source = remoteConfig.getValue(KEY_ADS_ENABLED).source
                _adsEnabled.value = enabled
                Log.d(TAG, "fetchAndActivate complete. success=${task.isSuccessful} " +
                        "activated=${task.result} ads_enabled=$enabled source=$source")
                task.exception?.let { Log.e(TAG, "Fetch failed", it) }
            }
    }
}