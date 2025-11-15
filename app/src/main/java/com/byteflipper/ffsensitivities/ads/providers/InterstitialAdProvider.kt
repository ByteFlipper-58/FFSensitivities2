package com.byteflipper.ffsensitivities.ads.providers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.byteflipper.ffsensitivities.ads.core.*
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class InterstitialAdProvider(
    context: Context,
    config: AdConfig,
    consentProvider: AdConsentProvider
) : BaseFullScreenAdProvider<InterstitialAd>(context, config, consentProvider) {
    
    override val TAG = "InterstitialAdProvider"

    override suspend fun loadAdInternal() {
        suspendCancellableCoroutine { continuation ->
            val adRequest = createAdRequest()
            
            InterstitialAd.load(
                context,
                config.adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        Log.d(TAG, "Interstitial ad loaded successfully")
                        adInstance = ad
                        isLoading = false
                        updateAdState(AdState.Loaded)

                        // Устанавливаем коллбэки для показа
                        ad.fullScreenContentCallback = createFullScreenCallback()
                        
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.e(TAG, "Failed to load interstitial ad: ${loadAdError.message}")
                        adInstance = null
                        isLoading = false
                        updateAdState(AdState.LoadFailed(loadAdError))
                        
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }
                }
            )
        }
    }

    override suspend fun showAdInternal(activity: Activity): AdResult {
        val ad = adInstance ?: return AdResult(config.adType, false, error = IllegalStateException("No ad instance"))

        return suspendCancellableCoroutine { continuation ->
            updateAdState(AdState.Showing)
            isShowingAd = true

            val originalCallback = ad.fullScreenContentCallback
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed")
                    isShowingAd = false
                    adInstance = null
                    updateAdState(AdState.Dismissed)
                    
                    if (continuation.isActive) {
                        continuation.resume(AdResult(config.adType, true))
                    }
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.e(TAG, "Failed to show interstitial ad: ${adError.message}")
                    isShowingAd = false
                    adInstance = null
                    updateAdState(AdState.ShowFailed(adError))
                    
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
                adInstance = null
                updateAdState(AdState.Initial)
                
                if (continuation.isActive) {
                    continuation.resume(AdResult(config.adType, false, error = e))
                }
            }
        }
    }

    private fun createFullScreenCallback() = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
            Log.d(TAG, "Interstitial ad dismissed (default callback)")
            isShowingAd = false
            adInstance = null
            updateAdState(AdState.Dismissed)
        }

        override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
            Log.e(TAG, "Failed to show interstitial ad (default callback): ${adError.message}")
            isShowingAd = false
            adInstance = null
            updateAdState(AdState.ShowFailed(adError))
        }

        override fun onAdShowedFullScreenContent() {
            Log.d(TAG, "Interstitial ad showed successfully (default callback)")
        }
    }
} 
