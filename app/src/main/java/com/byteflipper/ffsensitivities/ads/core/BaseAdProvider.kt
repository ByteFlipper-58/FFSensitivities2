package com.byteflipper.ffsensitivities.ads.core

import android.app.Activity
import android.content.Context
import android.util.Log
import com.byteflipper.ffsensitivities.ads.AdManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Базовый класс для всех провайдеров рекламы
 * Устраняет дублирование кода между различными типами рекламы
 */
abstract class BaseAdProvider<T : Any>(
    protected val context: Context,
    override val config: AdConfig,
    protected val adManager: AdManager
) : FullScreenAdProvider<AdState> {

    protected abstract val TAG: String
    
    protected val _adState = MutableStateFlow<AdState>(AdState.Initial)
    override val adState: StateFlow<AdState> = _adState.asStateFlow()

    protected var isLoading = false
    protected var adInstance: T? = null

    override suspend fun load() {
        if (isLoading || isReady()) {
            Log.d(TAG, "Ad already loading or ready")
            return
        }

        if (!adManager.canRequestPersonalizedAds()) {
            Log.w(TAG, "Cannot load ad - no consent")
            return
        }

        _adState.value = AdState.Loading
        isLoading = true

        withContext(Dispatchers.Main) {
            try {
                loadAdInternal()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load ad", e)
                isLoading = false
                _adState.value = AdState.LoadFailed(LoadAdError(0, e.message ?: "Unknown error", e.message ?: "Unknown error", null, null))
            }
        }
    }

    override suspend fun show(activity: Activity): AdResult {
        if (!isReady()) {
            Log.w(TAG, "Ad not ready")
            if (!isLoading) load()
            return AdResult(config.adType, false, error = IllegalStateException("Ad not ready"))
        }

        return try {
            showAdInternal(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show ad", e)
            AdResult(config.adType, false, error = e)
        }
    }

    override fun isReady(): Boolean = adInstance != null && !isLoading

    override fun canShow(): Boolean = isReady() && adManager.canRequestPersonalizedAds()

    override fun destroy() {
        adInstance = null
        isLoading = false
        _adState.value = AdState.Initial
    }

    /**
     * Внутренняя реализация загрузки рекламы
     */
    protected abstract suspend fun loadAdInternal()

    /**
     * Внутренняя реализация показа рекламы
     */
    protected abstract suspend fun showAdInternal(activity: Activity): AdResult

    /**
     * Обновляет состояние рекламы
     */
    protected fun updateAdState(state: AdState) {
        _adState.value = state
    }

    /**
     * Создает базовый AdRequest
     */
    protected fun createAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }
} 