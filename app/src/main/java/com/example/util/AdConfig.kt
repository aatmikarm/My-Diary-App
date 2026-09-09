package com.example.util

/**
 * AdMob Configuration File
 *
 * Switch between Test Ads and Production Ads using [USE_PRODUCTION_ADS]:
 * - true  -> The entire app shows LIVE PRODUCTION ADS using your registered AdMob units.
 * - false -> The entire app switches to official Google TEST ADS (safe for development and emulator testing).
 */
object AdConfig {

    /**
     * TOGGLE AD MODE HERE:
     * Set to 'true' for Production ads.
     * Set to 'false' for Test ads.
     */
    const val USE_PRODUCTION_ADS = false

    // ----------------------------------------------------
    // Production AdMob Credentials
    // Publisher ID: pub-5678552217308395
    // AdSense Customer ID: 528-875-7018
    // Google Ads Customer ID: 685-807-4795
    // ----------------------------------------------------
    const val PROD_APP_ID = "ca-app-pub-5678552217308395~3628411865"
    const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-5678552217308395/6614502002"
    const val PROD_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-5678552217308395/1960941121"

    // ----------------------------------------------------
    // Official Google Test Ad Credentials
    // Safe for testing on emulators and debug builds without risking policy penalties
    // ----------------------------------------------------
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    /**
     * Active Banner Ad Unit ID based on [USE_PRODUCTION_ADS]
     */
    val bannerAdUnitId: String
        get() = if (USE_PRODUCTION_ADS) PROD_BANNER_AD_UNIT_ID else TEST_BANNER_AD_UNIT_ID

    /**
     * Active Interstitial Ad Unit ID based on [USE_PRODUCTION_ADS]
     */
    val interstitialAdUnitId: String
        get() = if (USE_PRODUCTION_ADS) PROD_INTERSTITIAL_AD_UNIT_ID else TEST_INTERSTITIAL_AD_UNIT_ID
}
