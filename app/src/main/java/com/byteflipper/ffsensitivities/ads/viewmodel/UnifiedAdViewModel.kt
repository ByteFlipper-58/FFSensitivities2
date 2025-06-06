package com.byteflipper.ffsensitivities.ads.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.core.AdResult
import com.byteflipper.ffsensitivities.ads.core.AdType
import com.byteflipper.ffsensitivities.ads.repository.AdRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnifiedAdViewModel @Inject constructor(
    private val adRepository: AdRepository
) : ViewModel() {

    // Состояние последнего результата показа рекламы
    private val _lastAdResult = MutableStateFlow<AdResult?>(null)
    val lastAdResult: StateFlow<AdResult?> = _lastAdResult.asStateFlow()

    // Состояние готовности рекламы
    val adReadyState = adRepository.adReadyState

    /**
     * Показывает интерстициальную рекламу в указанной локации
     */
    fun showInterstitialAd(
        location: AdLocation,
        activity: Activity,
        onResult: (AdResult) -> Unit = {}
    ) {
        viewModelScope.launch {
            adRepository.showAd(AdType.INTERSTITIAL, location, activity) { adResult ->
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
            adRepository.showAd(AdType.REWARDED, location, activity) { adResult ->
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
            adRepository.showAd(AdType.BANNER, location, activity) { adResult ->
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
            adRepository.showAd(AdType.APP_OPEN, AdLocation.APP_STARTUP, activity) { adResult ->
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
            adRepository.trackActionAndShowAd(
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
        return adRepository.isAdReady(adType, location)
    }

    /**
     * Получает провайдер рекламы для прямого доступа (если нужно)
     */
    fun getAdProvider(adType: AdType, location: AdLocation) = 
        adRepository.getProvider(adType, location)

    /**
     * Получает провайдер рекламы по локации
     */
    fun getAdProvider(location: AdLocation) = adRepository.getProvider(location)

    /**
     * Обновляет состояние готовности рекламы
     */
    fun updateAdReadyState() {
        adRepository.updateReadyState()
    }

    /**
     * Очищает последний результат
     */
    fun clearLastAdResult() {
        _lastAdResult.value = null
    }

    /**
     * Очистка только локальных ресурсов ViewModel
     * AdRepository - Singleton и не должен очищаться при пересоздании ViewModel
     */
    override fun onCleared() {
        super.onCleared()
        _lastAdResult.value = null
        // НЕ вызываем adRepository.cleanup() - это приводит к сбросу счетчиков
    }
}