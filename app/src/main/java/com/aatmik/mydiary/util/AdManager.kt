package com.aatmik.mydiary.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.aatmik.mydiary.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AdManager {
    private const val TAG = "AdManager"

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false
    private var isInitialized = false

    // Minimum interval between interstitial ads to protect user experience (3 minutes)
    private const val MIN_INTERSTITIAL_INTERVAL_MS = 180_000L
    private var lastInterstitialShownTime: Long = 0L

    /**
     * Initialize Google Mobile Ads SDK
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            MobileAds.setRequestConfiguration(configuration)

            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "AdMob MobileAds initialized: $initializationStatus")
                isInitialized = true

                // React to the flag whenever it changes, instead of checking once.
                CoroutineScope(Dispatchers.Main).launch {
                    RemoteConfigManager.adsEnabled.collect { enabled ->
                        Log.d(TAG, "adsEnabled changed to $enabled")
                        if (enabled) {
                            loadInterstitialAd(context.applicationContext)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AdMob SDK", e)
        }
    }

    /**
     * Pre-load an interstitial ad in the background
     */
    fun loadInterstitialAd(context: Context) {
        if (!RemoteConfigManager.adsEnabled.value) return
        if (interstitialAd != null || isAdLoading) return

        isAdLoading = true
        val adUnitId = AdConfig.interstitialAdUnitId
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad successfully loaded")
                    interstitialAd = ad
                    isAdLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d("AdProof", "INTERSTITIAL FAILED — code=${loadAdError.code} domain=${loadAdError.domain} message=${loadAdError.message}")
                    interstitialAd = null
                    interstitialAd = null
                    isAdLoading = false
                }
            }
        )
    }

    /**
     * Show an interstitial ad if available and the user-friendly cooldown interval has passed.
     * Always calls [onAdDismissed] when complete or if no ad is ready, ensuring uninterrupted user flow.
     */
    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (!RemoteConfigManager.adsEnabled.value) {
            onAdDismissed()
            return
        }
        val now = System.currentTimeMillis()
        val timeSinceLastAd = now - lastInterstitialShownTime
        val currentAd = interstitialAd

        if (currentAd == null || timeSinceLastAd < MIN_INTERSTITIAL_INTERVAL_MS) {
            // Either no ad loaded or cooldown period active — continue seamlessly
            if (currentAd == null) {
                loadInterstitialAd(activity.applicationContext)
            }
            onAdDismissed()
            return
        }

        currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial ad was dismissed")
                interstitialAd = null
                lastInterstitialShownTime = System.currentTimeMillis()
                loadInterstitialAd(activity.applicationContext)
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                Log.w(TAG, "Interstitial ad failed to show: ${adError.message}")
                interstitialAd = null
                loadInterstitialAd(activity.applicationContext)
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial ad displayed full screen")
            }
        }

        currentAd.show(activity)
    }
}
