package com.byteflipper.ffsensitivities.ads.providers

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import com.byteflipper.ffsensitivities.ads.core.*
import com.byteflipper.ffsensitivities.ads.AdManager
import com.google.android.gms.ads.*

class BannerAdProviderImpl(
    context: Context,
    config: AdConfig,
    adManager: AdManager
) : BaseAdProvider<AdView>(context, config, adManager), BannerAdProvider {
    
    override val TAG = "BannerAdProvider"

    override suspend fun loadAdInternal() {
        // Баннерная реклама загружается при создании AdView
        Log.d(TAG, "Banner ad loading handled by AdView creation")
    }

    override suspend fun showAdInternal(activity: Activity): AdResult {
        // Баннерная реклама показывается через createAdView
        Log.d(TAG, "Banner ad showing handled by AdView")
        return AdResult(config.adType, true)
    }

    override fun createAdView(activity: Activity): View? {
        if (!adManager.canRequestPersonalizedAds()) {
            Log.w(TAG, "Cannot create banner ad - no consent")
            return null
        }

        updateAdState(AdState.Loading)

        try {
            val displayMetrics = activity.resources.displayMetrics
            val adWidthPixels = displayMetrics.widthPixels
            val density = displayMetrics.density
            val adWidthDp = (adWidthPixels / density).toInt()

            val adView = AdView(activity).apply {
                setAdSize(
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                        activity,
                        adWidthDp
                    )
                )
                adUnitId = config.adUnitId

                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d(TAG, "Banner ad loaded successfully")
                        updateAdState(AdState.Loaded)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.e(TAG, "Failed to load banner ad: ${loadAdError.message}")
                        updateAdState(AdState.LoadFailed(loadAdError))
                    }

                    override fun onAdOpened() {
                        Log.d(TAG, "Banner ad opened")
                        updateAdState(AdState.Showing)
                    }

                    override fun onAdClosed() {
                        Log.d(TAG, "Banner ad closed")
                        updateAdState(AdState.Dismissed)
                    }

                    override fun onAdClicked() {
                        Log.d(TAG, "Banner ad clicked")
                    }

                    override fun onAdImpression() {
                        Log.d(TAG, "Banner ad impression recorded")
                    }
                }

                loadAd(createAdRequest())
            }

            return adView
        } catch (e: Exception) {
            Log.e(TAG, "Error creating banner ad view", e)
            updateAdState(AdState.Initial)
            return null
        }
    }
} 