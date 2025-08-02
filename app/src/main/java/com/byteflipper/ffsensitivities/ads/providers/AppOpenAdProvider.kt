package com.byteflipper.ffsensitivities.ads.providers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.byteflipper.ffsensitivities.ads.core.*
import com.byteflipper.ffsensitivities.ads.AdManager
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Date
import kotlin.coroutines.resume

class AppOpenAdProvider(
    context: Context,
    config: AdConfig,
    adManager: AdManager
) : BaseFullScreenAdProvider<AppOpenAd>(context, config, adManager) {
    
    override val TAG = "AppOpenAdProvider"
    
    private companion object {
        private const val AD_TIMEOUT_MS = 4 * 60 * 60 * 1000L // 4 часа
        private const val MIN_AD_INTERVAL = 30_000L // 30 секунд между показами
    }

    private var loadTime: Long = 0
    private var lastAdShowTime = 0L

    override suspend fun loadAdInternal() {
        suspendCancellableCoroutine { continuation ->
            val adRequest = createAdRequest()
            
            AppOpenAd.load(
                context,
                config.adUnitId,
                adRequest,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        Log.d(TAG, "App open ad loaded successfully")
                        adInstance = ad
                        loadTime = Date().time
                        isLoading = false
                        updateAdState(AdState.Loaded)

                        // Устанавливаем коллбэки для показа
                        ad.fullScreenContentCallback = createFullScreenCallback()
                        
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.e(TAG, "Failed to load app open ad: ${loadAdError.message}")
                        adInstance = null
                        loadTime = 0
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
        // Проверка дебаунсинга
        if (!canShowAd()) {
            Log.d(TAG, "App open ad blocked by debouncing (${System.currentTimeMillis() - lastAdShowTime}ms since last show)")
            return AdResult(config.adType, false, error = IllegalStateException("Ad shown too recently"))
        }

        val ad = adInstance ?: return AdResult(config.adType, false, error = IllegalStateException("No ad instance"))

        return suspendCancellableCoroutine { continuation ->
            updateAdState(AdState.Showing)
            isShowingAd = true

            val originalCallback = ad.fullScreenContentCallback
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "App open ad dismissed")
                    isShowingAd = false
                    adInstance = null
                    loadTime = 0
                    lastAdShowTime = System.currentTimeMillis() // Обновляем время последнего показа
                    updateAdState(AdState.Dismissed)
                    
                    if (continuation.isActive) {
                        continuation.resume(AdResult(config.adType, true))
                    }
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.e(TAG, "Failed to show app open ad: ${adError.message}")
                    isShowingAd = false
                    adInstance = null
                    loadTime = 0
                    updateAdState(AdState.ShowFailed(adError))
                    
                    if (continuation.isActive) {
                        continuation.resume(AdResult(config.adType, false, error = Exception(adError.message)))
                    }
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "App open ad showed successfully")
                    originalCallback?.onAdShowedFullScreenContent()
                }
            }

            try {
                ad.show(activity)
            } catch (e: Exception) {
                Log.e(TAG, "Exception showing app open ad", e)
                isShowingAd = false
                adInstance = null
                loadTime = 0
                updateAdState(AdState.Initial)
                
                if (continuation.isActive) {
                    continuation.resume(AdResult(config.adType, false, error = e))
                }
            }
        }
    }

    override fun isReady(): Boolean {
        if (adInstance == null || isShowingAd) return false
        
        // Проверяем не истекла ли реклама (4 часа)
        val wasLoadTimeLessThan4HoursAgo = Date().time - loadTime < AD_TIMEOUT_MS
        if (!wasLoadTimeLessThan4HoursAgo) {
            Log.d(TAG, "App open ad expired")
            adInstance = null
            loadTime = 0
            updateAdState(AdState.Initial)
            return false
        }
        
        return true
    }

    override fun canShow(): Boolean = super.canShow() && canShowAd()

    /**
     * Проверяет, можно ли показать рекламу (дебаунсинг)
     */
    private fun canShowAd(): Boolean {
        return System.currentTimeMillis() - lastAdShowTime > MIN_AD_INTERVAL
    }

    override fun destroy() {
        adInstance?.fullScreenContentCallback = null
        adInstance = null
        loadTime = 0
        isLoading = false
        isShowingAd = false
        lastAdShowTime = 0L
        updateAdState(AdState.Initial)
    }

    private fun createFullScreenCallback() = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
            Log.d(TAG, "App open ad dismissed (default callback)")
            isShowingAd = false
            adInstance = null
            loadTime = 0
            updateAdState(AdState.Dismissed)
        }

        override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
            Log.e(TAG, "Failed to show app open ad (default callback): ${adError.message}")
            isShowingAd = false
            adInstance = null
            loadTime = 0
            updateAdState(AdState.ShowFailed(adError))
        }

        override fun onAdShowedFullScreenContent() {
            Log.d(TAG, "App open ad showed successfully (default callback)")
        }
    }
} 