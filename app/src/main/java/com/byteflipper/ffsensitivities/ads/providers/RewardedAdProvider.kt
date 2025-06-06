package com.byteflipper.ffsensitivities.ads.providers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.byteflipper.ffsensitivities.ads.ConsentManager
import com.byteflipper.ffsensitivities.ads.core.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class RewardedAdProvider(
    private val context: Context,
    override val config: AdConfig,
    private val consentManager: ConsentManager
) : FullScreenAdProvider<RewardedAdState> {
    
    private companion object {
        private const val TAG = "RewardedAdProvider"
        @Volatile
        private var isShowingAd = false
    }

    private val _adState = MutableStateFlow<RewardedAdState>(RewardedAdState.Initial)
    override val adState: StateFlow<RewardedAdState> = _adState.asStateFlow()

    private var rewardedAd: RewardedAd? = null
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

        _adState.value = RewardedAdState.Loading
        isLoading = true

        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                val adRequest = AdRequest.Builder().build()
                
                RewardedAd.load(
                    context,
                    config.adUnitId,
                    adRequest,
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(ad: RewardedAd) {
                            Log.d(TAG, "Rewarded ad loaded successfully")
                            rewardedAd = ad
                            isLoading = false
                            _adState.value = RewardedAdState.Loaded

                            // Устанавливаем коллбэки для показа
                            ad.fullScreenContentCallback = createFullScreenCallback()
                            
                            if (continuation.isActive) {
                                continuation.resume(Unit)
                            }
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            Log.e(TAG, "Failed to load rewarded ad: ${loadAdError.message}")
                            rewardedAd = null
                            isLoading = false
                            _adState.value = RewardedAdState.LoadFailed(loadAdError.message)
                            
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
            Log.w(TAG, "Another rewarded ad is already showing")
            return AdResult(config.adType, false, error = IllegalStateException("Ad already showing"))
        }

        val ad = rewardedAd
        if (ad == null || !isReady()) {
            Log.w(TAG, "Rewarded ad not ready")
            // Попытаемся загрузить если не загружена
            if (!isLoading) load()
            return AdResult(config.adType, false, error = IllegalStateException("Ad not ready"))
        }

        return suspendCancellableCoroutine { continuation ->
            _adState.value = RewardedAdState.Showing
            isShowingAd = true
            var rewardEarned: com.google.android.gms.ads.rewarded.RewardItem? = null

            val originalCallback = ad.fullScreenContentCallback
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed")
                    isShowingAd = false
                    rewardedAd = null
                    _adState.value = RewardedAdState.Dismissed
                    
                    if (continuation.isActive) {
                        continuation.resume(AdResult(config.adType, true, rewardEarned))
                    }
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.e(TAG, "Failed to show rewarded ad: ${adError.message}")
                    isShowingAd = false
                    rewardedAd = null
                    _adState.value = RewardedAdState.ShowFailed(adError.message)
                    
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
                    _adState.value = RewardedAdState.RewardEarned(AdReward(reward.amount, reward.type))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception showing rewarded ad", e)
                isShowingAd = false
                rewardedAd = null
                _adState.value = RewardedAdState.Initial
                
                if (continuation.isActive) {
                    continuation.resume(AdResult(config.adType, false, error = e))
                }
            }
        }
    }

    override fun isReady(): Boolean = rewardedAd != null && !isShowingAd

    override fun canShow(): Boolean = isReady() && consentManager.canRequestPersonalizedAds()

    override fun destroy() {
        rewardedAd?.fullScreenContentCallback = null
        rewardedAd = null
        isLoading = false
        isShowingAd = false
        _adState.value = RewardedAdState.Initial
    }

    private fun createFullScreenCallback() = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
            Log.d(TAG, "Rewarded ad dismissed (default callback)")
            isShowingAd = false
            rewardedAd = null
            _adState.value = RewardedAdState.Dismissed
        }

        override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
            Log.e(TAG, "Failed to show rewarded ad (default callback): ${adError.message}")
            isShowingAd = false
            rewardedAd = null
            _adState.value = RewardedAdState.ShowFailed(adError.message)
        }

        override fun onAdShowedFullScreenContent() {
            Log.d(TAG, "Rewarded ad showed successfully (default callback)")
        }
    }
} 