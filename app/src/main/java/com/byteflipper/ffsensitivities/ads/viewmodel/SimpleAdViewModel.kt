package com.byteflipper.ffsensitivities.ads.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.core.AdProvider
import com.byteflipper.ffsensitivities.ads.core.AdResult
import com.byteflipper.ffsensitivities.ads.core.AdType
import com.byteflipper.ffsensitivities.core.monetization.MonetizationCoordinator
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
    private val monetizationCoordinator: MonetizationCoordinator
) : ViewModel() {

    // Состояние последнего результата показа рекламы
    private val _lastAdResult = MutableStateFlow<AdResult?>(null)
    val lastAdResult: StateFlow<AdResult?> = _lastAdResult.asStateFlow()

    // Состояние готовности рекламы
    val adReadyState = monetizationCoordinator.adReadyState

    // Состояние согласия
    val consentState = monetizationCoordinator.consentState

    /**
     * Показывает интерстициальную рекламу в указанной локации
     */
    fun showInterstitialAd(
        location: AdLocation,
        activity: Activity,
        onResult: (AdResult) -> Unit = {}
    ) {
        viewModelScope.launch {
            monetizationCoordinator.showAd(AdType.INTERSTITIAL, location, activity) { adResult ->
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
            monetizationCoordinator.showAd(AdType.REWARDED, location, activity) { adResult ->
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
            monetizationCoordinator.showAd(AdType.BANNER, location, activity) { adResult ->
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
            monetizationCoordinator.showAd(AdType.APP_OPEN, AdLocation.APP_STARTUP, activity) { adResult ->
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
            monetizationCoordinator.trackActionAndShowAd(
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
        return monetizationCoordinator.isAdReady(adType, location)
    }

    /**
     * Обновляет состояние готовности рекламы
     */
    fun updateAdReadyState() {
        monetizationCoordinator.updateReadyState()
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
        monetizationCoordinator.checkAndRequestConsent(activity, onConsentResolved)
    }

    /**
     * Инициализирует Mobile Ads SDK
     */
    fun initializeMobileAdsSdk(onInitialized: (() -> Unit)? = null) {
        monetizationCoordinator.initializeMobileAdsSdk(onInitialized)
    }

    /**
     * Показывает форму опций приватности
     */
    fun showPrivacyOptionsForm(
        activity: Activity, 
        onDismissed: (formError: com.google.android.ump.FormError?) -> Unit
    ) {
        monetizationCoordinator.showPrivacyOptionsForm(activity, onDismissed)
    }

    /**
     * Получает провайдер рекламы для указанного типа и локации
     */
    fun getAdProvider(adType: AdType, location: AdLocation): AdProvider<*>? {
        return monetizationCoordinator.getAdProvider(adType, location)
    }

    /**
     * Очистка только локальных ресурсов ViewModel
     */
    override fun onCleared() {
        super.onCleared()
        _lastAdResult.value = null
    }
} 
