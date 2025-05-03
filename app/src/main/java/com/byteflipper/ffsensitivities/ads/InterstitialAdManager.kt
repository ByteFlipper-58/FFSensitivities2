package com.byteflipper.ffsensitivities.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.Date

/**
 * Interstitial Ad Manager
 * Handles loading and showing interstitial ads
 */
class InterstitialAdManager(
    private val context: Context,
    private val adUnitId: String
) {
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingAd = false
    // lastLoadTime is not used in the provided show/load logic, but kept for potential future use
    private var lastLoadTime: Long = 0
    private val TAG = "InterstitialAdManager"

    // Listener for ad events
    var interstitialAdListener: InterstitialAdListener? = null

    // Interface for ad event callbacks
    interface InterstitialAdListener {
        fun onAdDismissed()
        fun onAdShowedFullScreenContent()
        fun onAdFailedToShowFullScreenContent(adError: AdError)
        fun onAdLoaded()
        fun onAdFailedToLoad(error: LoadAdError)
    }

    /**
     * Load an interstitial ad
     */
    fun loadAd() {
        // Don't load ad if there's already an ad loaded or loading
        if (isLoadingAd || isAdAvailable()) {
            Log.d(TAG, "Load request ignored: Ad is already loading or available.")
            return
        }

        isLoadingAd = true
        Log.d(TAG, "Requesting InterstitialAd load. Unit ID: $adUnitId")

        val request = AdRequest.Builder().build()

        // Load the ad
        InterstitialAd.load(
            context,
            adUnitId,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoadingAd = false
                    lastLoadTime = Date().time
                    Log.d(TAG, "Interstitial ad loaded successfully")

                    // Set the full screen content callback
                    interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitialAd = null
                            isShowingAd = false // Reset flag
                            interstitialAdListener?.onAdDismissed()
                            Log.d(TAG, "Interstitial ad dismissed")
                            // Preload the next ad (as per example logic)
                            // ViewModel might handle this differently, but manager follows example
                            loadAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            interstitialAd = null
                            // isLoadingAd should remain false here unless a new load is triggered
                            isShowingAd = false // Reset flag
                            interstitialAdListener?.onAdFailedToShowFullScreenContent(adError)
                            Log.e(TAG, "Interstitial ad failed to show: ${adError.message}")
                            // Consider preloading next ad here too? Example doesn't, ViewModel handles retry.
                        }

                        override fun onAdShowedFullScreenContent() {
                            isShowingAd = true // Set flag
                            interstitialAdListener?.onAdShowedFullScreenContent()
                            Log.d(TAG, "Interstitial ad showed successfully")
                        }
                    }

                    interstitialAdListener?.onAdLoaded()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null // Ensure ad is null on failure
                    isLoadingAd = false
                    interstitialAdListener?.onAdFailedToLoad(loadAdError)
                    Log.e(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Check if ad is available
     */
    fun isAdAvailable(): Boolean {
        // Check companion object flag as well? Example doesn't, relies on instance null check.
        return interstitialAd != null /* && !isShowingAd */ // isShowingAd check added in showAdIfAvailable
    }

    /**
     * Show the ad if it's available
     * @return true if ad was shown, false otherwise
     */
    fun showAdIfAvailable(activity: Activity): Boolean {
        // Check companion object flag first
        if (isShowingAd) {
            Log.d(TAG, "The interstitial ad is already showing (checked via companion flag).")
            return false
        }

        if (!isAdAvailable()) {
            Log.d(TAG, "The interstitial ad is not ready yet (checked via instance).")
            // Only load if not already loading
            if (!isLoadingAd) {
                loadAd()
            }
            return false
        }

        Log.d(TAG, "Attempting to show InterstitialAd.")
        // The fullScreenContentCallback is set during onAdLoaded
        interstitialAd?.show(activity)
        // isShowingAd will be set to true in onAdShowedFullScreenContent callback
        // However, the example sets it here immediately. Let's follow the example for consistency.
        isShowingAd = true
        return true
    }

    companion object {
        // Flag to avoid showing multiple ads at the same time
        @Volatile private var isShowingAd = false
    }
}
