package com.byteflipper.ffsensitivities.ads.migration

import android.app.Activity
import android.util.Log
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.core.AdType
import com.byteflipper.ffsensitivities.ads.repository.AdRepository
import com.byteflipper.ffsensitivities.ads.viewmodel.UnifiedAdViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Вспомогательный класс для плавной миграции с старой архитектуры на новую
 * Обеспечивает совместимость и упрощает переход
 */
@Singleton
class AdMigrationHelper @Inject constructor(
    private val adRepository: AdRepository
) {
    private companion object {
        private const val TAG = "AdMigrationHelper"
    }

    /**
     * Показывает интерстициальную рекламу (совместимость со старым кодом)
     */
    suspend fun showInterstitialAd(
        activity: Activity,
        onAdShown: (() -> Unit)? = null,
        onAdFailed: (() -> Unit)? = null
    ) {
        Log.d(TAG, "Legacy interstitial ad request")
        val success = adRepository.showAd(AdType.INTERSTITIAL, AdLocation.HOME_SCREEN, activity) { result ->
            if (result.success) {
                onAdShown?.invoke()
            } else {
                onAdFailed?.invoke()
            }
        }
        
        if (!success) {
            onAdFailed?.invoke()
        }
    }

    /**
     * Трекинг действий и показ рекламы (совместимость)
     */
    suspend fun trackActionAndShowAdIfNeeded(
        screenKey: String,
        activity: Activity,
        onAdShown: (() -> Unit)? = null
    ) {
        Log.d(TAG, "Legacy action tracking for $screenKey")
        val location = when (screenKey) {
            "devices_screen" -> AdLocation.DEVICES_SCREEN
            "sensitivities_screen" -> AdLocation.SENSITIVITIES_SCREEN
            "home_screen" -> AdLocation.HOME_SCREEN
            "settings_screen" -> AdLocation.SETTINGS_SCREEN
            else -> AdLocation.HOME_SCREEN
        }
        
        val shown = adRepository.trackActionAndShowAd(
            location, 
            AdType.INTERSTITIAL, 
            activity
        ) { result ->
            if (result.success) {
                onAdShown?.invoke()
            }
        }
        
        if (shown) {
            onAdShown?.invoke()
        }
    }

    /**
     * Проверка готовности рекламы
     */
    fun isInterstitialReady(): Boolean {
        return adRepository.isAdReady(AdType.INTERSTITIAL, AdLocation.HOME_SCREEN)
    }

    /**
     * Создает мостик для использования UnifiedAdViewModel в старом коде
     */
    fun createViewModelBridge(
        viewModel: UnifiedAdViewModel,
        coroutineScope: CoroutineScope
    ): LegacyAdBridge {
        return LegacyAdBridge(viewModel, coroutineScope)
    }

    /**
     * Мостик между старым и новым кодом
     */
    class LegacyAdBridge(
        private val viewModel: UnifiedAdViewModel,
        private val coroutineScope: CoroutineScope
    ) {
        fun trackActionAndShowAdIfNeeded(activity: Activity) {
            coroutineScope.launch {
                viewModel.trackActionAndShowInterstitial(AdLocation.HOME_SCREEN, activity)
            }
        }

        fun showInterstitialAd(activity: Activity) {
            viewModel.showInterstitialAd(AdLocation.HOME_SCREEN, activity)
        }

        fun isAdReady(): Boolean {
            return viewModel.isAdReady(AdType.INTERSTITIAL, AdLocation.HOME_SCREEN)
        }
    }
} 