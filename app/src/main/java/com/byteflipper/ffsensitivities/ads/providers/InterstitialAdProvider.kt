package com.byteflipper.ffsensitivities.ads.providers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.byteflipper.ffsensitivities.ads.ConsentManager
import com.byteflipper.ffsensitivities.ads.core.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class InterstitialAdProvider(
    private val context: Context,
    override val config: AdConfig,
    private val consentManager: ConsentManager
) : FullScreenAdProvider<AdState> {
    
    private companion object {
        private const val TAG = "InterstitialAdProvider"
        @Volatile
        private var isShowingAd = false
    }

    private val _adState = MutableStateFlow<AdState>(AdState.Initial)
    override val adState: StateFlow<AdState> = _adState.asStateFlow()

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    override suspend fun load() {
        if (isLoading || isReady()) {
            Log.d(TAG, "Ad already loading or ready")
            return
        }

        if (!consentManager.canRequestPersonalizedAds()) {
            Log.w(TAG, "Cannot load ad - no consent")
            return
        }

        _adState.value = AdState.Loading
        isLoading = true

        // Убеждаемся что загрузка происходит в главном потоке
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                val adRequest = AdRequest.Builder().build()
                
                InterstitialAd.load(
                    context,
                    config.adUnitId,
                    adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            Log.d(TAG, "Interstitial ad loaded successfully")
                            interstitialAd = ad
                            isLoading = false
                            _adState.value = AdState.Loaded

                            // Устанавливаем коллбэки для показа
                            ad.fullScreenContentCallback = createFullScreenCallback()
                            
                            if (continuation.isActive) {
                                continuation.resume(Unit)
                            }
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            Log.e(TAG, "Failed to load interstitial ad: ${loadAdError.message}")
                            interstitialAd = null
                            isLoading = false
                            _adState.value = AdState.LoadFailed(loadAdError)
                            
                            if (continuation.isActive) {
                                continuation.resume(Unit)
                            }
                        }
                    }
                )
            }
        }
    }

    override suspend fun show(activity: Activity): AdResult {
        if (isShowingAd) {
            Log.w(TAG, "Another interstitial ad is already showing")
            return AdResult(config.adType, false, error = IllegalStateException("Ad already showing"))
        }

        val ad = interstitialAd
        if (ad == null || !isReady()) {
            Log.w(TAG, "Interstitial ad not ready")
            // Попытаемся загрузить если не загружена
            if (!isLoading) load()
            return AdResult(config.adType, false, error = IllegalStateException("Ad not ready"))
        }

        return suspendCancellableCoroutine { continuation ->
            _adState.value = AdState.Showing
            isShowingAd = true

            val originalCallback = ad.fullScreenContentCallback
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed")
                    isShowingAd = false
                    interstitialAd = null
                    _adState.value = AdState.Dismissed
                    
                    if (continuation.isActive) {
                        continuation.resume(AdResult(config.adType, true))
                    }
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.e(TAG, "Failed to show interstitial ad: ${adError.message}")
                    isShowingAd = false
                    interstitialAd = null
                    _adState.value = AdState.ShowFailed(adError)
                    
                    if (continuation.isActive) {
                        continuation.resume(AdResult(config.adType, false, error = Exception(adError.message)))
                    }
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad showed successfully")
                    originalCallback?.onAdShowedFullScreenContent()
                }
            }

            try {
                ad.show(activity)
            } catch (e: Exception) {
                Log.e(TAG, "Exception showing interstitial ad", e)
                isShowingAd = false
                interstitialAd = null
                _adState.value = AdState.Initial
                
                if (continuation.isActive) {
                    continuation.resume(AdResult(config.adType, false, error = e))
                }
            }
        }
    }

    override fun isReady(): Boolean = interstitialAd != null && !isShowingAd

    override fun canShow(): Boolean = isReady() && consentManager.canRequestPersonalizedAds()

    override fun destroy() {
        interstitialAd?.fullScreenContentCallback = null
        interstitialAd = null
        isLoading = false
        isShowingAd = false
        _adState.value = AdState.Initial
    }

    private fun createFullScreenCallback() = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
            Log.d(TAG, "Interstitial ad dismissed (default callback)")
            isShowingAd = false
            interstitialAd = null
            _adState.value = AdState.Dismissed
        }

        override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
            Log.e(TAG, "Failed to show interstitial ad (default callback): ${adError.message}")
            isShowingAd = false
            interstitialAd = null
            _adState.value = AdState.ShowFailed(adError)
        }

        override fun onAdShowedFullScreenContent() {
            Log.d(TAG, "Interstitial ad showed successfully (default callback)")
        }
    }
} 