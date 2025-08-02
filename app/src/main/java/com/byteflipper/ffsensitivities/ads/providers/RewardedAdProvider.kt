package com.byteflipper.ffsensitivities.ads.providers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.byteflipper.ffsensitivities.ads.core.*
import com.byteflipper.ffsensitivities.ads.AdManager
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class RewardedAdProvider(
    context: Context,
    config: AdConfig,
    adManager: AdManager
) : BaseFullScreenAdProvider<RewardedAd>(context, config, adManager) {
    
    override val TAG = "RewardedAdProvider"

    override suspend fun loadAdInternal() {
        suspendCancellableCoroutine { continuation ->
            val adRequest = createAdRequest()
            
            RewardedAd.load(
                context,
                config.adUnitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        Log.d(TAG, "Rewarded ad loaded successfully")
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
                        Log.e(TAG, "Failed to load rewarded ad: ${loadAdError.message}")
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
            var rewardEarned: com.google.android.gms.ads.rewarded.RewardItem? = null

            val originalCallback = ad.fullScreenContentCallback
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed")
                    isShowingAd = false
                    adInstance = null
                    updateAdState(AdState.Dismissed)
                    
                    if (continuation.isActive) {
                        continuation.resume(AdResult(config.adType, true, rewardEarned))
                    }
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.e(TAG, "Failed to show rewarded ad: ${adError.message}")
                    isShowingAd = false
                    adInstance = null
                    updateAdState(AdState.ShowFailed(adError))
                    
                    if (continuation.isActive) {
                        continuation.resume(AdResult(config.adType, false, error = Exception(adError.message)))
                    }
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad showed successfully")
                    originalCallback?.onAdShowedFullScreenContent()
                }
            }

            try {
                ad.show(activity) { reward ->
                    Log.d(TAG, "User earned reward: ${reward.amount} ${reward.type}")
                                            rewardEarned = reward
                        updateAdState(AdState.Loaded)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception showing rewarded ad", e)
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
            Log.d(TAG, "Rewarded ad dismissed (default callback)")
            isShowingAd = false
            adInstance = null
            updateAdState(AdState.Dismissed)
        }

        override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
            Log.e(TAG, "Failed to show rewarded ad (default callback): ${adError.message}")
            isShowingAd = false
            adInstance = null
            updateAdState(AdState.ShowFailed(adError))
        }

        override fun onAdShowedFullScreenContent() {
            Log.d(TAG, "Rewarded ad showed successfully (default callback)")
        }
    }
} 