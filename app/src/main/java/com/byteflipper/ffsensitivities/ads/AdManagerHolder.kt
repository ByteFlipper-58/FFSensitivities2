package com.byteflipper.ffsensitivities.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.byteflipper.ffsensitivities.utils.AdConstants // Add import
import com.yandex.mobile.ads.rewarded.Reward

/**
 * Singleton object to hold and manage ad manager instances.
 */
object AdManagerHolder {

    private var interstitialAdManager: InterstitialAdManager? = null
    private var rewardedAdManager: RewardedAdManager? = null

    private const val TAG = "AdManagerHolder"

    // Use constants from AdConstants object
    // private const val INTERSTITIAL_AD_UNIT_ID = "R-M-13549181-3" // Removed hardcoded ID
    // private const val REWARDED_AD_UNIT_ID = "YOUR_REWARDED_AD_UNIT_ID" // Removed hardcoded ID

    /**
     * Initializes the ad managers and starts preloading.
     * Should be called once from Application.onCreate().
     */
    fun initialize(context: Context) {
        if (interstitialAdManager == null) {
            interstitialAdManager = InterstitialAdManager(context.applicationContext)
            Log.d(TAG, "InterstitialAdManager initialized.")
            preloadInterstitialAd() // Start preloading
        }
        if (rewardedAdManager == null) {
             // Initialize Rewarded only if needed/used in the app
             // rewardedAdManager = RewardedAdManager(context.applicationContext)
             // Log.d(TAG, "RewardedAdManager initialized.")
             // preloadRewardedAd() // Start preloading if initialized
        }
    }

    /**
     * Preloads an interstitial ad if one isn't already loaded or loading.
     */
    fun preloadInterstitialAd() {
        Log.d(TAG, "Attempting to preload Interstitial Ad...")
        interstitialAdManager?.loadAd(
            adUnitId = AdConstants.INTERSTITIAL_AD_UNIT_ID, // Use constant
            onLoaded = { Log.i(TAG, "Interstitial Ad preloaded successfully.") },
            onError = { error -> Log.e(TAG, "Failed to preload Interstitial Ad: ${error.description}") }
            // Other callbacks (onShown, onDismissed) are handled by the manager internally
            // and trigger the next preload via preloadNextAd() in BaseAdManager
        )
    }

    /**
     * Shows the preloaded interstitial ad if available.
     * Automatically triggers preloading of the next ad upon dismissal or failure to show.
     * Allows passing specific callbacks for this show instance.
     */
    fun showInterstitialAd(activity: Activity, onShown: () -> Unit = {}, onDismissed: () -> Unit = {}) {
        Log.d(TAG, "Attempting to show Interstitial Ad...")

        // Pass the specific callbacks for this show instance to the manager's show method.
        // BaseAdManager will handle setting the listener with these callbacks before showing.
        interstitialAdManager?.show(
            activity = activity,
            onShown = {
                Log.d(TAG, "Interstitial Ad shown callback triggered.")
                onShown() // Call the provided callback
            },
            onDismissed = {
                Log.d(TAG, "Interstitial Ad dismissed callback triggered.")
                onDismissed() // Call the provided callback
                // Preloading is handled internally by BaseAdManager after dismissal
            }
            // onRewarded is not applicable here
        )

        // If the ad wasn't loaded, BaseAdManager's show method logs a warning
        // and attempts to preload the next ad. We don't need to load on demand here,
        // relying on the preload mechanism.
    }

    // --- Rewarded Ad Methods (similar structure if needed) ---
    fun preloadRewardedAd() {
        // rewardedAdManager?.loadAd(adUnitId = AdConstants.REWARDED_AD_UNIT_ID, ...) // Use constant if implemented
    }

    fun showRewardedAd(activity: Activity, onRewarded: (Reward) -> Unit, onShown: () -> Unit = {}, onDismissed: () -> Unit = {}) {
         Log.d(TAG, "Attempting to show Rewarded Ad...")
         rewardedAdManager?.show(
             activity = activity,
             onShown = {
                 Log.d(TAG, "Rewarded Ad shown callback triggered.")
                 onShown()
             },
             onDismissed = {
                 Log.d(TAG, "Rewarded Ad dismissed callback triggered.")
                 onDismissed()
             },
             onRewarded = { reward ->
                 Log.d(TAG, "Rewarded Ad reward callback triggered.")
                 onRewarded(reward)
             }
         )
    }

    /**
     * Cleans up ad resources. Should be called when the app is destroyed.
     */
    fun destroy() {
        Log.d(TAG, "Destroying Ad Managers.")
        interstitialAdManager?.destroy()
        interstitialAdManager = null
        rewardedAdManager?.destroy()
        rewardedAdManager = null
    }
}
