package com.byteflipper.ffsensitivities.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.yandex.mobile.ads.appopenad.AppOpenAd
import com.yandex.mobile.ads.appopenad.AppOpenAdEventListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoader
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData

class AppOpenAdManager(private val application: Application) : Application.ActivityLifecycleCallbacks {
    private var appOpenAdLoader: AppOpenAdLoader? = null
    private var appOpenAd: AppOpenAd? = null
    private var isShowingAd = false
    private var currentActivity: Activity? = null

    // Use dependency injection or config for ad unit ID
    private val adUnitId = "R-M-13549181-1" // Replace with actual Yandex ad unit ID

    fun loadAd() {
        if (isShowingAd) return

        // Create or get existing loader
        appOpenAdLoader = appOpenAdLoader ?: AppOpenAdLoader(application)

        // Ad request configuration
        val adRequestConfiguration = AdRequestConfiguration.Builder(adUnitId)
            .build()

        appOpenAdLoader?.setAdLoadListener(object : AppOpenAdLoadListener {
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad.apply {
                    setAdEventListener(adEventListener)
                }
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                // Log error or implement retry logic
                Log.e("AppOpenAd", "Ad failed to load: ${error.description}")
            }
        })

        // Load ad
        appOpenAdLoader?.loadAd(adRequestConfiguration)
    }

    private val adEventListener = object : AppOpenAdEventListener {
        override fun onAdShown() {
            isShowingAd = true
        }

        override fun onAdFailedToShow(error: AdError) {
            isShowingAd = false
            loadAd() // Attempt to load new ad
        }

        override fun onAdDismissed() {
            isShowingAd = false
            appOpenAd = null
            loadAd() // Preload next ad
        }

        override fun onAdClicked() {
            // Optional: Track ad click
        }

        override fun onAdImpression(data: ImpressionData?) {
            // Optional: Track ad impression
        }
    }

    fun showAdIfAvailable(activity: Activity) {
        if (!isShowingAd && appOpenAd != null) {
            appOpenAd?.show(activity)
        } else {
            loadAd()
        }
    }

    // ActivityLifecycleCallbacks implementation
    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        showAdIfAvailable(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        currentActivity = null
    }

    // Other lifecycle methods - left minimal for brevity
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}