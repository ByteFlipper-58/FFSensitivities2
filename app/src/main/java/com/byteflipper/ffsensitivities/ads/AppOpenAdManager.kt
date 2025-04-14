package com.byteflipper.ffsensitivities.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import com.byteflipper.ffsensitivities.di.ApplicationScope
import com.byteflipper.ffsensitivities.presentation.ui.MainActivity // To check if current activity is the main one
import com.byteflipper.ffsensitivities.utils.AdConstants
import com.yandex.mobile.ads.appopenad.AppOpenAd
import com.yandex.mobile.ads.appopenad.AppOpenAdEventListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoader
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppOpenAdManager @Inject constructor(
    private val context: Context,
    private val dataStoreManager: DataStoreManager,
    @ApplicationScope private val coroutineScope: CoroutineScope
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private var appOpenAd: AppOpenAd? = null
    private var isLoading: Boolean = false
    private var isShowing: Boolean = false
    private var loadTime: Long = 0
    private var currentActivity: Activity? = null
    private var appOpenAdLoader: AppOpenAdLoader = AppOpenAdLoader(context)
    private var isFirstLoadAttempted = false
    private var shouldAttemptShowOnLoad = false

    companion object {
        private const val TAG = "AppOpenAdManager"
        private const val AD_EXPIRATION_MS = 4 * 60 * 60 * 1000
        private const val SHOW_FREQUENCY = 3
    }

    // Declare listener before init block
    private val adLoadListener = object : AppOpenAdLoadListener {
        override fun onAdLoaded(ad: AppOpenAd) {
            Log.d(TAG, "AppOpenAd loaded successfully.")
            appOpenAd = ad
            isLoading = false
            loadTime = System.currentTimeMillis()
            ad.setAdEventListener(appOpenAdEventListener)
            // If we intended to show this ad immediately after load, try now
            if (shouldAttemptShowOnLoad) {
                Log.d(TAG, "Ad loaded, attempting to show immediately due to shouldAttemptShowOnLoad flag.")
                showAdIfAvailable()
            }
        }

        override fun onAdFailedToLoad(error: AdRequestError) {
            isLoading = false
            val errorMessage = "AppOpenAd failed to load: ${error.description} (Code: ${error.code})"
            Log.e(TAG, errorMessage)
            appOpenAd = null
            // Do not retry immediately per Yandex recommendations. Preload will happen after next dismissal/failure.
        }
    }

    init {
        appOpenAdLoader.setAdLoadListener(adLoadListener) // Now listener is declared before use
    }

    private val appOpenAdEventListener = object : AppOpenAdEventListener {
        override fun onAdShown() {
            Log.d(TAG, "AppOpenAd shown.")
            isShowing = true
        }

        override fun onAdDismissed() {
            Log.d(TAG, "AppOpenAd dismissed.")
            isShowing = false
            clearAppOpenAd() // Clear resources
            loadAd() // Preload the next ad
        }

        override fun onAdFailedToShow(adError: AdError) {
            Log.e(TAG, "AppOpenAd failed to show: ${adError.description}")
            isShowing = false
            clearAppOpenAd() // Clear resources
            loadAd() // Preload the next ad
        }

        override fun onAdClicked() {
            Log.d(TAG, "AppOpenAd clicked.")
        }

        override fun onAdImpression(impressionData: ImpressionData?) {
            Log.d(TAG, "AppOpenAd impression recorded: ${impressionData?.rawData}")
        }
    }

    // --- Public Methods ---

    /** Initiates the first ad load attempt. Should be called after SDK init. */
    fun loadInitialAd() {
        if (!isFirstLoadAttempted) {
             Log.d(TAG, "Initiating first AppOpenAd load.")
             isFirstLoadAttempted = true
             loadAd()
        } else {
             Log.d(TAG, "Initial load already attempted.")
        }
    }

    // --- Private Methods ---

    private fun loadAd() {
        if (isLoading || isAdAvailable()) {
            Log.d(TAG, "Load request ignored: Ad is already loading or available.")
            return
        }
        isLoading = true
        Log.d(TAG, "Loading AppOpenAd with Unit ID: ${AdConstants.APP_OPEN_AD_UNIT_ID}")
        val adRequestConfiguration = AdRequestConfiguration.Builder(AdConstants.APP_OPEN_AD_UNIT_ID).build()
        appOpenAdLoader.loadAd(adRequestConfiguration)
    }

    private fun isAdAvailable(): Boolean {
        val isExpired = (System.currentTimeMillis() - loadTime) > AD_EXPIRATION_MS
        if (appOpenAd != null && isExpired) {
            Log.d(TAG, "AppOpenAd expired.")
            clearAppOpenAd() // Clear expired ad
        }
        return appOpenAd != null && !isShowing
    }

    private fun showAdIfAvailable() {
        val activity = currentActivity
        if (activity == null || activity !is MainActivity) { // Only show on MainActivity for now
             Log.d(TAG, "Show condition not met: Not MainActivity or activity is null.")
             return
        }
        if (isShowing) {
            Log.d(TAG, "Show request ignored: An ad is already showing.")
            return
        }
        if (!isAdAvailable()) {
            Log.d(TAG, "Show request ignored: Ad not available or expired.")
            // Set the flag to true, so we show it when it loads
            shouldAttemptShowOnLoad = true
            Log.d(TAG, "Setting shouldAttemptShowOnLoad = true and initiating load.")
            loadAd() // Try to load it
            return
        }

        Log.d(TAG, "Attempting to show AppOpenAd.")
        // Reset the flag if we are showing the ad now
        shouldAttemptShowOnLoad = false
        appOpenAd?.show(activity)
    }

    private fun clearAppOpenAd() {
        appOpenAd?.setAdEventListener(null)
        appOpenAd = null
    }

    // --- DefaultLifecycleObserver Implementation ---

    override fun onStart(owner: LifecycleOwner) {
        // App came to foreground
        Log.d(TAG, "App came to foreground (onStart).")
        coroutineScope.launch {
            // Increment launch count first
            dataStoreManager.incrementAppLaunchCount()
            val launchCount = dataStoreManager.getAppLaunchCount().first()
            Log.d(TAG, "App launch count is now: $launchCount")

            // Reset the show attempt flag at the beginning of each onStart check
            // to ensure we only try to show immediately if the check within this onStart fails due to ad not being ready.
            shouldAttemptShowOnLoad = false

            if (launchCount > 0 && launchCount % SHOW_FREQUENCY == 0) {
                Log.d(TAG, "Launch count ($launchCount) is multiple of $SHOW_FREQUENCY. Attempting to show ad.")
                showAdIfAvailable() // This might set shouldAttemptShowOnLoad = true if ad not ready
            } else {
                Log.d(TAG, "Launch count ($launchCount) not a multiple of $SHOW_FREQUENCY. Not showing ad.")
                // Consider preloading if ad is not available, even if not showing this time
                if (!isAdAvailable() && !isLoading) {
                    loadAd()
                }
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        // No specific action needed here
    }

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}
