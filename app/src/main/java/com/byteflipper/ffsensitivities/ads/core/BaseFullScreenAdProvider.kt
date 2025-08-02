package com.byteflipper.ffsensitivities.ads.core

import android.app.Activity
import android.content.Context
import android.util.Log
import com.byteflipper.ffsensitivities.ads.AdManager

/**
 * Базовый класс для полноэкранной рекламы (Interstitial, Rewarded, App Open)
 */
abstract class BaseFullScreenAdProvider<T : Any>(
    context: Context,
    config: AdConfig,
    adManager: AdManager
) : BaseAdProvider<T>(context, config, adManager) {
    
    protected var isShowingAd = false
    
    override suspend fun show(activity: Activity): AdResult {
        if (isShowingAd) {
            Log.w(TAG, "Another ad is already showing")
            return AdResult(config.adType, false, error = IllegalStateException("Ad already showing"))
        }

        return super.show(activity)
    }

    override fun isReady(): Boolean = super.isReady() && !isShowingAd

    override fun destroy() {
        isShowingAd = false
        super.destroy()
    }
    
    override fun canShow(): Boolean = isReady() && !isShowingAd && adManager.canRequestPersonalizedAds()
} 