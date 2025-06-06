package com.byteflipper.ffsensitivities.ads.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.byteflipper.ffsensitivities.ads.ConsentManager
import com.byteflipper.ffsensitivities.ads.core.*
import com.byteflipper.ffsensitivities.ads.providers.*
import com.byteflipper.ffsensitivities.utils.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val consentManager: ConsentManager,
    private val coroutineScope: CoroutineScope,
    private val dataStoreManager: com.byteflipper.ffsensitivities.data.local.DataStoreManager
) {
    private companion object {
        private const val TAG = "AdRepository"
    }

    // Карта провайдеров по локациям
    private val providersByLocation = mutableMapOf<AdLocation, AdProvider<*>>()
    
    // Карта провайдеров по типу и локации для быстрого доступа
    private val providersByTypeAndLocation = mutableMapOf<Pair<AdType, AdLocation>, AdProvider<*>>()

    // Глобальное состояние готовности рекламы
    private val _adReadyState = MutableStateFlow<Map<AdLocation, Boolean>>(emptyMap())
    val adReadyState: StateFlow<Map<AdLocation, Boolean>> = _adReadyState.asStateFlow()

    // Счетчики действий теперь хранятся в DataStore
    // private val actionCounters убран - теперь используем DataStoreManager

    init {
        // Инициализируем провайдеры для всех локаций
        initializeProviders()
        
        // Инициализируем состояние готовности
        updateReadyState()
        
        // Запускаем предзагрузку асинхронно
        coroutineScope.launch {
            preloadAllAds()
        }
    }

    /**
     * Инициализирует провайдеры для всех локаций
     */
    private fun initializeProviders() {
        // Интерстициальная реклама для разных экранов
        createInterstitialProvider(AdLocation.DEVICES_SCREEN)
        createInterstitialProvider(AdLocation.SENSITIVITIES_SCREEN)
        createInterstitialProvider(AdLocation.HOME_SCREEN)
        createInterstitialProvider(AdLocation.SETTINGS_SCREEN)
        
        // Баннерная реклама для разных локаций
        createBannerProvider(AdLocation.MAIN_BANNER)
        createBannerProvider(AdLocation.DEVICES_SCREEN)
        createBannerProvider(AdLocation.SENSITIVITIES_SCREEN)
        createBannerProvider(AdLocation.HOME_SCREEN)
        createBannerProvider(AdLocation.SETTINGS_SCREEN)
        
        // Наградная реклама
        createRewardedProvider(AdLocation.PREMIUM_FEATURES)
        createRewardedProvider(AdLocation.EXTRA_SENSITIVITIES)
        
        // Реклама при открытии приложения
        createAppOpenProvider()
    }

    private fun createInterstitialProvider(location: AdLocation) {
        val config = AdConfigFactory.createInterstitialConfig(location)
        val provider = InterstitialAdProvider(context, config, consentManager)
        providersByLocation[location] = provider
        providersByTypeAndLocation[AdType.INTERSTITIAL to location] = provider
    }

    private fun createBannerProvider(location: AdLocation) {
        val config = AdConfigFactory.createBannerConfig(location)
        val provider = BannerAdProviderImpl(context, config, consentManager)
        providersByLocation[location] = provider
        providersByTypeAndLocation[AdType.BANNER to location] = provider
    }

    private fun createRewardedProvider(location: AdLocation) {
        val config = AdConfigFactory.createRewardedConfig(location)
        val provider = RewardedAdProvider(context, config, consentManager)
        providersByLocation[location] = provider
        providersByTypeAndLocation[AdType.REWARDED to location] = provider
    }

    private fun createAppOpenProvider() {
        val config = AdConfigFactory.createAppOpenConfig()
        val provider = AppOpenAdProvider(context, config, consentManager)
        providersByLocation[AdLocation.APP_STARTUP] = provider
        providersByTypeAndLocation[AdType.APP_OPEN to AdLocation.APP_STARTUP] = provider
    }

    /**
     * Проверяет готовность рекламы
     */
    private fun canShowAds(): Boolean {
        return consentManager.canRequestPersonalizedAds() && 
               NetworkUtils.isInternetConnected(context)
    }

    /**
     * Предзагружает всю рекламу
     */
    private suspend fun preloadAllAds() {
        if (!canShowAds()) {
            Log.w(TAG, "Cannot preload ads - consent or network issue")
            return
        }

        Log.d(TAG, "Starting ads preload...")
        providersByLocation.values.forEach { provider ->
            try {
                withContext(Dispatchers.Main) {
                    provider.load()
                }
                Log.d(TAG, "Successfully preloaded ${provider.config.adType} at ${provider.config.location}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to preload ${provider.config.adType} at ${provider.config.location}", e)
            }
        }
        
        // Обновляем состояние после загрузки
        updateReadyState()
        Log.d(TAG, "Ads preload completed")
    }

    /**
     * Получает провайдер по типу и локации
     */
    fun getProvider(adType: AdType, location: AdLocation): AdProvider<*>? {
        return providersByTypeAndLocation[adType to location]
    }

    /**
     * Получает провайдер по локации (для обратной совместимости)
     */
    fun getProvider(location: AdLocation): AdProvider<*>? {
        return providersByLocation[location]
    }

    /**
     * Показывает рекламу с проверкой готовности
     */
    suspend fun showAd(
        adType: AdType,
        location: AdLocation,
        activity: Activity,
        onResult: (AdResult) -> Unit = {}
    ): Boolean {
        if (!canShowAds()) {
            Log.w(TAG, "Cannot show ads - consent or network issue")
            onResult(AdResult(adType, false, error = IllegalStateException("Ads not allowed")))
            return false
        }

        val provider = getProvider(adType, location)
        if (provider == null) {
            Log.e(TAG, "No provider found for ad type: $adType at location: $location")
            onResult(AdResult(adType, false, error = IllegalStateException("No provider")))
            return false
        }

        return try {
            val result = provider.show(activity)
            onResult(result)
            
            // Перезагружаем рекламу после показа
            if (result.success) {
                coroutineScope.launch {
                    try {
                        withContext(Dispatchers.Main) {
                            provider.load()
                        }
                        updateReadyState()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to reload ad after show: $adType at $location", e)
                    }
                }
            }
            
            result.success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show ad: $adType at $location", e)
            onResult(AdResult(adType, false, error = e))
            false
        }
    }

    /**
     * Отслеживает действия пользователя и показывает рекламу по частоте
     * Использует персистентное хранение счетчиков
     */
    suspend fun trackActionAndShowAd(
        location: AdLocation,
        adType: AdType,
        activity: Activity,
        onResult: (AdResult) -> Unit = {}
    ): Boolean {
        val provider = getProvider(adType, location) ?: return false
        val frequency = provider.config.frequency
        
        // Получаем текущий счетчик из DataStore вместо памяти
        val locationKey = location.name
        val currentCount = dataStoreManager.incrementAdCounter(locationKey)
        
        Log.d(TAG, "Action tracked for $location: $currentCount/$frequency")
        
        if (currentCount >= frequency) {
            val shown = showAd(adType, location, activity, onResult)
            if (shown) {
                dataStoreManager.resetAdCounter(locationKey)
                Log.d(TAG, "Action counter reset for $location")
            }
            return shown
        }
        
        // Предзагружаем рекламу когда приближаемся к показу
        if (currentCount == frequency - 1 && !provider.isReady()) {
            Log.d(TAG, "Preloading ad for upcoming show: $adType at $location")
            coroutineScope.launch {
                try {
                    withContext(Dispatchers.Main) {
                        provider.load()
                    }
                    updateReadyState()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to preload ad: $adType at $location", e)
                }
            }
        }
        
        return false
    }

    /**
     * Проверяет готовность рекламы
     */
    fun isAdReady(adType: AdType, location: AdLocation): Boolean {
        return getProvider(adType, location)?.isReady() ?: false
    }

    /**
     * Обновляет состояние готовности всей рекламы
     */
    fun updateReadyState() {
        val readyMap = providersByLocation.mapValues { (_, provider) -> provider.isReady() }
        _adReadyState.value = readyMap
        Log.d(TAG, "Ad ready state updated: $readyMap")
    }

    /**
     * Освобождает ресурсы и предотвращает утечки памяти
     */
    fun destroy() {
        Log.d(TAG, "Destroying AdRepository")
        
        try {
            // Останавливаем все активные корутины
            coroutineScope.coroutineContext.cancelChildren()
            
            // Очищаем провайдеры
            providersByLocation.values.forEach { provider ->
                try {
                    provider.destroy()
                } catch (e: Exception) {
                    Log.w(TAG, "Error destroying provider: ${provider.config.adType}", e)
                }
            }
            
            // Очищаем коллекции
            providersByLocation.clear()
            providersByTypeAndLocation.clear()
            // actionCounters.clear() - убрано, счетчики в DataStore
            
            // Сбрасываем состояние
            _adReadyState.value = emptyMap()
            
            Log.d(TAG, "AdRepository destroyed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during AdRepository destruction", e)
        }
    }

    /**
     * Легкая очистка только активных операций без сброса счетчиков
     * Счетчики теперь в DataStore, поэтому не сбрасываются
     */
    fun cleanup() {
        Log.d(TAG, "Light cleanup of AdRepository - no action needed")
        
        try {
            // Больше ничего не делаем - счетчики в DataStore, провайдеры - Singleton
            Log.d(TAG, "AdRepository light cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during AdRepository light cleanup", e)
        }
    }

    /**
     * Полная очистка AdRepository при уничтожении приложения
     * Вызывается только из MyApplication.onTerminate() или аналогичных мест
     */
    fun fullCleanup() {
        Log.d(TAG, "Full cleanup of AdRepository resources")
        
        try {
            // Отменяем все pending операции
            coroutineScope.coroutineContext.cancelChildren()
            
            // Счетчики теперь в DataStore, не очищаем их
            // actionCounters.clear() - убрано
            
            // Сбрасываем состояние готовности
            _adReadyState.value = emptyMap()
            
            Log.d(TAG, "AdRepository full cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during AdRepository full cleanup", e)
        }
    }

    // Методы для обратной совместимости
    @Deprecated("Используйте showAd с AdLocation")
    suspend fun showAd(
        adType: AdType,
        activity: Activity,
        onResult: (AdResult) -> Unit = {}
    ): Boolean {
        val defaultLocation = when (adType) {
            AdType.INTERSTITIAL -> AdLocation.HOME_SCREEN
            AdType.BANNER -> AdLocation.MAIN_BANNER
            AdType.REWARDED -> AdLocation.PREMIUM_FEATURES
            AdType.APP_OPEN -> AdLocation.APP_STARTUP
        }
        return showAd(adType, defaultLocation, activity, onResult)
    }

    @Deprecated("Используйте trackActionAndShowAd с AdLocation")
    suspend fun trackActionAndShowAd(
        screenKey: String,
        adType: AdType,
        activity: Activity,
        onResult: (AdResult) -> Unit = {}
    ): Boolean {
        val location = when (screenKey) {
            "devices_screen" -> AdLocation.DEVICES_SCREEN
            "sensitivities_screen" -> AdLocation.SENSITIVITIES_SCREEN
            "home_screen" -> AdLocation.HOME_SCREEN
            "settings_screen" -> AdLocation.SETTINGS_SCREEN
            else -> AdLocation.HOME_SCREEN
        }
        return trackActionAndShowAd(location, adType, activity, onResult)
    }

    /**
     * Загружает рекламу определенного типа в указанной локации
     */
    suspend fun loadAd(adType: AdType, location: AdLocation) {
        val provider = getProvider(adType, location)
        if (provider == null) {
            Log.e(TAG, "No provider found for ad type: $adType at location: $location")
            return
        }

        try {
            withContext(Dispatchers.Main) {
                provider.load()
            }
            updateReadyState()
            Log.d(TAG, "Successfully loaded $adType at $location")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load $adType at $location", e)
        }
    }
} 