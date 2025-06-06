package com.byteflipper.ffsensitivities.ads.providers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.byteflipper.ffsensitivities.ads.ConsentManager
import com.byteflipper.ffsensitivities.ads.core.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.coroutines.resume

class AppOpenAdProvider(
    private val context: Context,
    override val config: AdConfig,
    private val consentManager: ConsentManager
) : FullScreenAdProvider<AdState> {
    
    private companion object {
        private const val TAG = "AppOpenAdProvider"
        private const val AD_TIMEOUT_MS = 4 * 60 * 60 * 1000L // 4 часа
        @Volatile
        private var isShowingAd = false
    }

    private val _adState = MutableStateFlow<AdState>(AdState.Initial)
    override val adState: StateFlow<AdState> = _adState.asStateFlow()

    private var appOpenAd: AppOpenAd? = null
    private var loadTime: Long = 0
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
                
                AppOpenAd.load(
                    context,
                    config.adUnitId,
                    adRequest,
                    object : AppOpenAd.AppOpenAdLoadCallback() {
                        override fun onAdLoaded(ad: AppOpenAd) {
                            Log.d(TAG, "App open ad loaded successfully")
                            appOpenAd = ad
                            loadTime = Date().time
                            isLoading = false
                            _adState.value = AdState.Loaded

                            // Устанавливаем коллбэки для показа
                            ad.fullScreenContentCallback = createFullScreenCallback()
                            
                            if (continuation.isActive) {
                                continuation.resume(Unit)
                            }
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            Log.e(TAG, "Failed to load app open ad: ${loadAdError.message}")
                            appOpenAd = null
                            loadTime = 0
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
            Log.w(TAG, "Another app open ad is already showing")
            return AdResult(config.adType, false, error = IllegalStateException("Ad already showing"))
        }

        val ad = appOpenAd
        if (ad == null || !isReady()) {
            Log.w(TAG, "App open ad not ready or expired")
            // Попытаемся загрузить если не загружена
            if (!isLoading) load()
            return AdResult(config.adType, false, error = IllegalStateException("Ad not ready"))
        }

        // ИСПРАВЛЕНИЕ: Обеспечиваем выполнение показа рекламы в главном потоке
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                _adState.value = AdState.Showing
                isShowingAd = true

                val originalCallback = ad.fullScreenContentCallback
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "App open ad dismissed")
                        isShowingAd = false
                        appOpenAd = null
                        loadTime = 0
                        _adState.value = AdState.Dismissed
                        
                        if (continuation.isActive) {
                            continuation.resume(AdResult(config.adType, true))
                        }
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                        Log.e(TAG, "Failed to show app open ad: ${adError.message}")
                        isShowingAd = false
                        appOpenAd = null
                        loadTime = 0
                        _adState.value = AdState.ShowFailed(adError)
                        
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
                    appOpenAd = null
                    loadTime = 0
                    _adState.value = AdState.Initial
                    
                    if (continuation.isActive) {
                        continuation.resume(AdResult(config.adType, false, error = e))
                    }
                }
            }
        }
    }

    override fun isReady(): Boolean {
        if (appOpenAd == null || isShowingAd) return false
        
        // Проверяем не истекла ли реклама (4 часа)
        val wasLoadTimeLessThan4HoursAgo = Date().time - loadTime < AD_TIMEOUT_MS
        if (!wasLoadTimeLessThan4HoursAgo) {
            Log.d(TAG, "App open ad expired")
            appOpenAd = null
            loadTime = 0
            _adState.value = AdState.Initial
            return false
        }
        
        return true
    }

    override fun canShow(): Boolean = isReady() && consentManager.canRequestPersonalizedAds()

    override fun destroy() {
        appOpenAd?.fullScreenContentCallback = null
        appOpenAd = null
        loadTime = 0
        isLoading = false
        isShowingAd = false
        _adState.value = AdState.Initial
    }

    private fun createFullScreenCallback() = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
            Log.d(TAG, "App open ad dismissed (default callback)")
            isShowingAd = false
            appOpenAd = null
            loadTime = 0
            _adState.value = AdState.Dismissed
        }

        override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
            Log.e(TAG, "Failed to show app open ad (default callback): ${adError.message}")
            isShowingAd = false
            appOpenAd = null
            loadTime = 0
            _adState.value = AdState.ShowFailed(adError)
        }

        override fun onAdShowedFullScreenContent() {
            Log.d(TAG, "App open ad showed successfully (default callback)")
        }
    }
} 