package com.byteflipper.ffsensitivities.ads

import android.app.Activity
import android.app.Application
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

/**
 * App Open Ad Manager
 * Handles loading and showing app open ads when the app is brought to foreground
 */
class AppOpenAdManager(
    private val application: Application,
    private val adUnitId: String
) {
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var loadTime: Long = 0
    private val TAG = "AppOpenAdManager"

    // Listener for ad events
    var appOpenAdListener: AppOpenAdListener? = null

    // Interface for ad event callbacks
    interface AppOpenAdListener {
        fun onAdDismissed()
        fun onAdShowedFullScreenContent()
        fun onAdFailedToShowFullScreenContent(adError: AdError)
    }

    /**
     * Load an app open ad
     */
    fun loadAd() {
        // Don't load ad if there's already an ad loaded or loading
        if (isLoadingAd || isAdAvailable()) {
            Log.d(TAG, "Load request ignored: Ad is already loading or available.")
            return
        }

        isLoadingAd = true
        Log.d(TAG, "Requesting AppOpenAd load. Unit ID: $adUnitId")

        val request = AdRequest.Builder().build()

        // Load the ad
        AppOpenAd.load(
            application,
            adUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    Log.d(TAG, "onAdLoaded")

                    // Set the full screen content callback
                    appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            appOpenAd = null
                            isShowingAd = false // Reset flag when ad is dismissed
                            appOpenAdListener?.onAdDismissed()
                            Log.d(TAG, "onAdDismissedFullScreenContent")
                            // Load the next ad
                            loadAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            appOpenAd = null
                            isLoadingAd = false // Should be reset here too
                            isShowingAd = false // Reset flag on failure
                            appOpenAdListener?.onAdFailedToShowFullScreenContent(adError)
                            Log.d(TAG, "onAdFailedToShowFullScreenContent: ${adError.message}")
                            // Load the next ad
                            loadAd()
                        }

                        override fun onAdShowedFullScreenContent() {
                            isShowingAd = true // Set flag when ad is shown
                            appOpenAdListener?.onAdShowedFullScreenContent()
                            Log.d(TAG, "onAdShowedFullScreenContent")
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    Log.e(TAG, "onAdFailedToLoad: ${loadAdError.message}")
                    // No need to set appOpenAd to null here, it wasn't assigned
                }
            }
        )
    }

    /**
     * Check if ad is available and not expired
     */
    private fun isAdAvailable(): Boolean {
        // Ad references are cleared when an ad expires (after 4 hours)
        val wasLoadTimeLessThan4HoursAgo = Date().time - loadTime < (4 * 60 * 60 * 1000)
        return appOpenAd != null && wasLoadTimeLessThan4HoursAgo
    }

    /**
     * Show the ad if it's available, otherwise load a new one
     */
    fun showAdIfAvailable(activity: Activity) {
        // Check if an ad is already showing using the companion object flag
        if (isShowingAd) {
            Log.d(TAG, "The app open ad is already showing.")
            return
        }

        if (!isAdAvailable()) {
            Log.d(TAG, "The app open ad is not ready yet.")
            // Only load if not already loading
            if (!isLoadingAd) {
                loadAd()
            }
            return
        }

        Log.d(TAG, "Attempting to show AppOpenAd.")
        appOpenAd?.show(activity)
        // isShowingAd will be set to true in onAdShowedFullScreenContent callback
    }

    companion object {
        // Flag to avoid showing multiple ads at the same time across instances if needed,
        // though with proper lifecycle management, this might be redundant.
        // Let's keep the logic as provided in the example.
        @Volatile private var isShowingAd = false
    }
}
