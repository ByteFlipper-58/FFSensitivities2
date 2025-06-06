package com.byteflipper.ffsensitivities.ads.providers

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import com.byteflipper.ffsensitivities.ads.ConsentManager
import com.byteflipper.ffsensitivities.ads.core.*
import com.google.android.gms.ads.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BannerAdProviderImpl(
    private val context: Context,
    override val config: AdConfig,
    private val consentManager: ConsentManager
) : BannerAdProvider {
    
    private companion object {
        private const val TAG = "BannerAdProvider"
    }

    private val _adState = MutableStateFlow<AdState>(AdState.Initial)
    override val adState: StateFlow<AdState> = _adState.asStateFlow()

    override suspend fun load() {
        // Баннерная реклама загружается при создании AdView
        Log.d(TAG, "Banner ad loading handled by AdView creation")
    }

    override suspend fun show(activity: Activity): AdResult {
        // Баннерная реклама показывается через createAdView
        Log.d(TAG, "Banner ad showing handled by AdView")
        return AdResult(config.adType, true)
    }

    override fun isReady(): Boolean {
        return consentManager.canRequestPersonalizedAds()
    }

    override fun destroy() {
        _adState.value = AdState.Initial
        Log.d(TAG, "Banner ad provider destroyed")
    }

    override fun createAdView(activity: Activity): View? {
        if (!consentManager.canRequestPersonalizedAds()) {
            Log.w(TAG, "Cannot create banner ad - no consent")
            return null
        }

        _adState.value = AdState.Loading

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
                        _adState.value = AdState.Loaded
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.e(TAG, "Failed to load banner ad: ${loadAdError.message}")
                        _adState.value = AdState.LoadFailed(loadAdError)
                    }

                    override fun onAdOpened() {
                        Log.d(TAG, "Banner ad opened")
                        _adState.value = AdState.Showing
                    }

                    override fun onAdClosed() {
                        Log.d(TAG, "Banner ad closed")
                        _adState.value = AdState.Dismissed
                    }

                    override fun onAdClicked() {
                        Log.d(TAG, "Banner ad clicked")
                    }

                    override fun onAdImpression() {
                        Log.d(TAG, "Banner ad impression recorded")
                    }
                }

                loadAd(AdRequest.Builder().build())
            }

            return adView
        } catch (e: Exception) {
            Log.e(TAG, "Error creating banner ad view", e)
            _adState.value = AdState.Initial
            return null
        }
    }
} 