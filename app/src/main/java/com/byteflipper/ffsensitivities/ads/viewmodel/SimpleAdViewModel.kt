package com.byteflipper.ffsensitivities.ads.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.ads.AdManager
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.core.AdResult
import com.byteflipper.ffsensitivities.ads.core.AdType
import com.byteflipper.ffsensitivities.ads.core.AdProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Упрощенный ViewModel для работы с рекламой
 * Заменяет UnifiedAdViewModel и устраняет сложность
 */
@HiltViewModel
class SimpleAdViewModel @Inject constructor(
    private val adManager: AdManager
) : ViewModel() {

    // Состояние последнего результата показа рекламы
    private val _lastAdResult = MutableStateFlow<AdResult?>(null)
    val lastAdResult: StateFlow<AdResult?> = _lastAdResult.asStateFlow()

    // Состояние готовности рекламы
    val adReadyState = adManager.adReadyState

    // Состояние согласия
    val consentState = adManager.consentState

    /**
     * Показывает интерстициальную рекламу в указанной локации
     */
    fun showInterstitialAd(
        location: AdLocation,
        activity: Activity,
        onResult: (AdResult) -> Unit = {}
    ) {
        viewModelScope.launch {
            adManager.showAd(AdType.INTERSTITIAL, location, activity) { adResult ->
                _lastAdResult.value = adResult
                onResult(adResult)
            }
        }
    }

    /**
     * Показывает наградную рекламу в указанной локации
     */
    fun showRewardedAd(
        location: AdLocation,
        activity: Activity,
        onResult: (AdResult) -> Unit = {}
    ) {
        viewModelScope.launch {
            adManager.showAd(AdType.REWARDED, location, activity) { adResult ->
                _lastAdResult.value = adResult
                onResult(adResult)
            }
        }
    }

    /**
     * Показывает баннерную рекламу в указанной локации
     */
    fun showBannerAd(
        location: AdLocation,
        activity: Activity,
        onResult: (AdResult) -> Unit = {}
    ) {
        viewModelScope.launch {
            adManager.showAd(AdType.BANNER, location, activity) { adResult ->
                _lastAdResult.value = adResult
                onResult(adResult)
            }
        }
    }

    /**
     * Показывает рекламу при открытии приложения
     */
    fun showAppOpenAd(activity: Activity, onResult: (AdResult) -> Unit = {}) {
        viewModelScope.launch {
            adManager.showAd(AdType.APP_OPEN, AdLocation.APP_STARTUP, activity) { adResult ->
                _lastAdResult.value = adResult
                onResult(adResult)
            }
        }
    }

    /**
     * Отслеживает действие пользователя и показывает интерстициальную рекламу по частоте
     */
    fun trackActionAndShowInterstitial(
        location: AdLocation,
        activity: Activity,
        onResult: (AdResult) -> Unit = {}
    ) {
        viewModelScope.launch {
            adManager.trackActionAndShowAd(
                location = location,
                adType = AdType.INTERSTITIAL,
                activity = activity
            ) { adResult ->
                _lastAdResult.value = adResult
                onResult(adResult)
            }
        }
    }

    /**
     * Проверяет готовность рекламы определенного типа в указанной локации
     */
    fun isAdReady(adType: AdType, location: AdLocation): Boolean {
        return adManager.isAdReady(adType, location)
    }

    /**
     * Обновляет состояние готовности рекламы
     */
    fun updateAdReadyState() {
        adManager.updateReadyState()
    }

    /**
     * Очищает последний результат
     */
    fun clearLastAdResult() {
        _lastAdResult.value = null
    }

    /**
     * Проверяет согласие пользователя
     */
    fun checkAndRequestConsent(activity: Activity, onConsentResolved: (Boolean) -> Unit) {
        adManager.checkAndRequestConsent(activity, onConsentResolved)
    }

    /**
     * Инициализирует Mobile Ads SDK
     */
    fun initializeMobileAdsSdk(onInitialized: (() -> Unit)? = null) {
        adManager.initializeMobileAdsSdk(onInitialized)
    }

    /**
     * Показывает форму опций приватности
     */
    fun showPrivacyOptionsForm(
        activity: Activity, 
        onDismissed: (formError: com.google.android.ump.FormError?) -> Unit
    ) {
        adManager.showPrivacyOptionsForm(activity, onDismissed)
    }

    /**
     * Получает провайдер рекламы для указанного типа и локации
     */
    fun getAdProvider(adType: AdType, location: AdLocation): AdProvider<*>? {
        return adManager.getAdProvider(adType, location)
    }

    /**
     * Очистка только локальных ресурсов ViewModel
     */
    override fun onCleared() {
        super.onCleared()
        _lastAdResult.value = null
    }
} 