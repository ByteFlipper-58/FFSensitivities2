package com.byteflipper.ffsensitivities.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.byteflipper.ffsensitivities.utils.AdConstants
import com.byteflipper.ffsensitivities.utils.NetworkUtils
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardedAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val consentManager: ConsentManager
) {
    private companion object {
        private const val TAG = "RewardedAdManager"
    }

    private val _rewardedAd = MutableStateFlow<RewardedAd?>(null)
    val rewardedAdState: StateFlow<RewardedAd?> = _rewardedAd.asStateFlow()

    private val canShowAds: Boolean
        get() = consentManager.canRequestPersonalizedAds()

    init {
        preloadAd()
    }

    fun preloadAd(adUnitId: String? = null) {
        val actualAdUnitId = if (!adUnitId.isNullOrEmpty()) adUnitId else AdConstants.REWARDED_AD_UNIT_ID
        Log.d(TAG, "Attempting to preload Rewarded Ad with ID: $actualAdUnitId")

        if (!canShowAds || !NetworkUtils.isInternetConnected(context)) {
            Log.w(TAG, "Cannot preload rewarded ad (Can show: $canShowAds, Internet: ${NetworkUtils.isInternetConnected(context)}, Ad ID: $actualAdUnitId)")
            return
        }
        if (_rewardedAd.value != null) {
            Log.d(TAG, "Rewarded ad already loaded or loading.")
            return
        }

        Log.d(TAG, "Preloading Rewarded Ad with ID: $actualAdUnitId...")
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            actualAdUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully")
                    _rewardedAd.value = ad
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Rewarded Ad Load Error - Code: ${loadAdError.code}, Message: ${loadAdError.message}, Domain: ${loadAdError.domain}")
                    _rewardedAd.value = null // Clear potential stale ad
                }
            }
        )
    }

    fun showAd(activity: Activity, onDismissed: () -> Unit = {}, onRewardEarned: (RewardItem) -> Unit) {
        val ad = _rewardedAd.value

        if (ad == null) {
            Log.w(TAG, "Cannot show rewarded ad (Ad not loaded yet). Triggering preload with default ID.")
            preloadAd()
            onDismissed()
            return
        }

        if (!canShowAds) {
             Log.w(TAG, "Cannot show rewarded ad (Cannot request ads - consent/init issue).")
             onDismissed()
             return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded ad dismissed")
                _rewardedAd.value = null // Ad consumed
                preloadAd()
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Rewarded Ad Show Error - Code: ${adError.code}, Message: ${adError.message}, Domain: ${adError.domain}")
                _rewardedAd.value = null
                preloadAd()
                onDismissed()
            }

             override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded ad showed successfully.")
            }
        }

        Log.d(TAG, "Showing Rewarded Ad...")
        ad.show(activity) { rewardItem ->
            Log.d(TAG, "User earned reward: Amount=${rewardItem.amount}, Type=${rewardItem.type}")
            onRewardEarned(rewardItem)
        }
    }
}
