package com.byteflipper.ffsensitivities.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.byteflipper.ffsensitivities.ads.core.*
import com.byteflipper.ffsensitivities.ads.providers.*
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import com.byteflipper.ffsensitivities.utils.NetworkUtils
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import com.byteflipper.ffsensitivities.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.EnumMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Упрощенный менеджер рекламы, объединяющий ConsentManager и AdRepository
 * Устраняет сложность и дублирование кода
 */
@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val coroutineScope: CoroutineScope,
    private val dataStoreManager: DataStoreManager
) {
    private companion object {
        private const val TAG = "AdManager"
    }

    // Consent Management
    private lateinit var consentInformation: ConsentInformation
    private var isMobileAdsInitialized = false
    private var currentConsentStatus: Int = ConsentInformation.ConsentStatus.UNKNOWN
    private val firebaseAnalytics: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(context) }

    // Ad Providers
    private val adProviders = mutableMapOf<Pair<AdType, AdLocation>, AdProvider<*>>()
    
    // State Management
    private val _consentState = MutableStateFlow(ConsentState())
    val consentState: StateFlow<ConsentState> = _consentState.asStateFlow()
    
    private val _adReadyState = MutableStateFlow<Map<AdLocation, Boolean>>(emptyMap())
    val adReadyState: StateFlow<Map<AdLocation, Boolean>> = _adReadyState.asStateFlow()

    init {
        initializeProviders()
        updateConsentState()
    }

    /**
     * Инициализирует провайдеры рекламы
     */
    private fun initializeProviders() {
        // Интерстициальная реклама
        createProvider(AdType.INTERSTITIAL, AdLocation.DEVICES_SCREEN)
        createProvider(AdType.INTERSTITIAL, AdLocation.SENSITIVITIES_SCREEN)
        createProvider(AdType.INTERSTITIAL, AdLocation.HOME_SCREEN)
        createProvider(AdType.INTERSTITIAL, AdLocation.SETTINGS_SCREEN)
        
        // Баннерная реклама
        createProvider(AdType.BANNER, AdLocation.MAIN_BANNER)
        createProvider(AdType.BANNER, AdLocation.DEVICES_SCREEN)
        createProvider(AdType.BANNER, AdLocation.SENSITIVITIES_SCREEN)
        createProvider(AdType.BANNER, AdLocation.HOME_SCREEN)
        createProvider(AdType.BANNER, AdLocation.SETTINGS_SCREEN)
        
        // Наградная реклама
        createProvider(AdType.REWARDED, AdLocation.PREMIUM_FEATURES)
        createProvider(AdType.REWARDED, AdLocation.EXTRA_SENSITIVITIES)
        
        // Реклама при открытии приложения
        createProvider(AdType.APP_OPEN, AdLocation.APP_STARTUP)
    }

    private fun createProvider(adType: AdType, location: AdLocation) {
        val config = when (adType) {
            AdType.INTERSTITIAL -> AdConfigFactory.createInterstitialConfig(location)
            AdType.BANNER -> AdConfigFactory.createBannerConfig(location)
            AdType.REWARDED -> AdConfigFactory.createRewardedConfig(location)
            AdType.APP_OPEN -> AdConfigFactory.createAppOpenConfig()
        }
        
        val provider = when (adType) {
            AdType.INTERSTITIAL -> InterstitialAdProvider(context, config, this)
            AdType.BANNER -> BannerAdProviderImpl(context, config, this)
            AdType.REWARDED -> RewardedAdProvider(context, config, this)
            AdType.APP_OPEN -> AppOpenAdProvider(context, config, this)
        }
        
        adProviders[adType to location] = provider
    }

    /**
     * Проверяет и запрашивает согласие пользователя
     */
    fun checkAndRequestConsent(activity: Activity, onConsentResolved: (Boolean) -> Unit) {
        consentInformation = UserMessagingPlatform.getConsentInformation(context)

        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        Log.d(TAG, "Requesting consent info update...")

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            { // Success listener
                Log.d(TAG, "Consent info updated successfully. Status: ${consentInformation.consentStatus}")
                currentConsentStatus = consentInformation.consentStatus
                updateConsentState()

                if (consentInformation.isConsentFormAvailable &&
                    currentConsentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                    loadAndShowForm(activity, onConsentResolved)
                } else {
                    val canRequestAds = canRequestPersonalizedAds()
                    Log.d(TAG, "Consent form not required or not available. Can request ads: $canRequestAds")
                    updateConsentStatus(canRequestAds)
                    onConsentResolved(canRequestAds)
                }
            },
            { formError -> // Error listener
                Log.e(TAG, "Consent info update failed: ${formError.message}")
                currentConsentStatus = ConsentInformation.ConsentStatus.UNKNOWN
                updateConsentState(error = formError.message)
                val canRequestAds = canRequestPersonalizedAds()
                updateConsentStatus(canRequestAds)
                onConsentResolved(canRequestAds)
            }
        )
    }

    private fun loadAndShowForm(activity: Activity, onConsentResolved: (Boolean) -> Unit) {
        Log.d(TAG, "Loading consent form...")
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { loadAndShowError ->
            if (loadAndShowError != null) {
                Log.e(TAG, "Consent form load/show failed: ${loadAndShowError.message}")
                currentConsentStatus = ConsentInformation.ConsentStatus.UNKNOWN
                updateConsentState(error = loadAndShowError.message)
            } else {
                currentConsentStatus = consentInformation.consentStatus
                Log.d(TAG, "Consent form shown and dismissed. New status: $currentConsentStatus")
                updateConsentState()
            }
            val canRequestAds = canRequestPersonalizedAds()
            Log.d(TAG, "After form attempt, can request ads: $canRequestAds")
            updateConsentStatus(canRequestAds)
            onConsentResolved(canRequestAds)
        }
    }

    /**
     * Определяет возможность запроса персонализированной рекламы
     */
    fun canRequestPersonalizedAds(): Boolean {
        return when (currentConsentStatus) {
            ConsentInformation.ConsentStatus.OBTAINED,
            ConsentInformation.ConsentStatus.NOT_REQUIRED -> true
            else -> false
        }
    }

    /**
     * Инициализирует Google Mobile Ads SDK
     */
    fun initializeMobileAdsSdk(onInitialized: (() -> Unit)? = null) {
        if (!isMobileAdsInitialized) {
            Log.d(TAG, "Initializing Google Mobile Ads SDK...")
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "Google Mobile Ads initialized successfully.")
                isMobileAdsInitialized = true
                updateConsentState()
                onInitialized?.invoke()
            }
        } else {
            Log.d(TAG, "Google Mobile Ads SDK already initialized.")
            onInitialized?.invoke()
        }
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

        val provider = adProviders[adType to location]
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
                        provider.load()
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
     */
    suspend fun trackActionAndShowAd(
        location: AdLocation,
        adType: AdType,
        activity: Activity,
        onResult: (AdResult) -> Unit = {}
    ): Boolean {
        val provider = adProviders[adType to location] ?: return false
        val frequency = provider.config.frequency
        
        val locationKey = location.name
        val currentCount = dataStoreManager.incrementAdCounter(locationKey)
        
        Log.d(TAG, "Action tracked for $location: $currentCount/$frequency")
        
        if (currentCount >= frequency) {
            Log.d(TAG, "Frequency reached for $location, attempting to show $adType ad")
            val shown = showAd(adType, location, activity, onResult)
            if (shown) {
                dataStoreManager.resetAdCounter(locationKey)
                Log.d(TAG, "Ad shown successfully, counter reset for $location")
            } else {
                Log.w(TAG, "Failed to show ad for $location, counter NOT reset")
            }
            return shown
        }
        
        // Предзагружаем рекламу когда приближаемся к показу
        if (currentCount == frequency - 1 && !provider.isReady()) {
            Log.d(TAG, "Preloading ad for upcoming show: $adType at $location")
            coroutineScope.launch {
                try {
                    provider.load()
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
        return adProviders[adType to location]?.isReady() ?: false
    }

    /**
     * Получает провайдер рекламы для указанного типа и локации
     */
    fun getAdProvider(adType: AdType, location: AdLocation): AdProvider<*>? {
        return adProviders[adType to location]
    }

    /**
     * Загружает рекламу определенного типа в указанной локации
     */
    suspend fun loadAd(adType: AdType, location: AdLocation) {
        val provider = adProviders[adType to location]
        if (provider != null) {
            try {
                provider.load()
                updateReadyState()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load ad: $adType at $location", e)
            }
        }
    }

    /**
     * Обновляет состояние готовности всей рекламы
     */
    fun updateReadyState() {
        val readyMap = adProviders.entries
            .groupBy { it.key.second } // Group by location
            .mapValues { (_, providers) -> 
                providers.any { it.value.isReady() }
            }
        _adReadyState.value = readyMap
        Log.d(TAG, "Ad ready state updated: $readyMap")
    }

    /**
     * Проверяет возможность показа рекламы
     */
    private fun canShowAds(): Boolean {
        return canRequestPersonalizedAds() && NetworkUtils.isInternetConnected(context)
    }

    /**
     * Обновляет статус согласия для релевантных SDK
     */
    private fun updateConsentStatus(consentGranted: Boolean) {
        Log.d(TAG, "Updating consent status: $consentGranted")

        val analyticsStatus = if (consentGranted) {
            FirebaseAnalytics.ConsentStatus.GRANTED
        } else {
            FirebaseAnalytics.ConsentStatus.DENIED
        }
        
        val consentMap = EnumMap<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>(
            FirebaseAnalytics.ConsentType::class.java
        ).apply {
            put(FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE, analyticsStatus)
            put(FirebaseAnalytics.ConsentType.AD_STORAGE, analyticsStatus)
            put(FirebaseAnalytics.ConsentType.AD_USER_DATA, analyticsStatus)
            put(FirebaseAnalytics.ConsentType.AD_PERSONALIZATION, analyticsStatus)
        }
        
        firebaseAnalytics.setConsent(consentMap)
        Log.d(TAG, "Set FirebaseAnalytics consent: $consentMap")
    }

    /**
     * Обновляет состояние согласия
     */
    private fun updateConsentState(error: String? = null) {
        val newState = ConsentState(
            status = currentConsentStatus,
            canRequestPersonalizedAds = canRequestPersonalizedAds(),
            isPrivacyOptionsRequired = isPrivacyOptionsRequired,
            isInitialized = ::consentInformation.isInitialized && isMobileAdsInitialized,
            isFormAvailable = if (::consentInformation.isInitialized) {
                consentInformation.isConsentFormAvailable
            } else false,
            error = error
        )
        
        _consentState.value = newState
        Log.d(TAG, "Consent state updated: $newState")
    }

    /**
     * Проверяет необходимость показа опций приватности
     */
    val isPrivacyOptionsRequired: Boolean
        get() = if (::consentInformation.isInitialized) {
            consentInformation.privacyOptionsRequirementStatus == 
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        } else {
            Log.w(TAG, "isPrivacyOptionsRequired checked before consentInformation initialized.")
            false
        }

    /**
     * Показывает форму опций приватности
     */
    fun showPrivacyOptionsForm(activity: Activity, onDismissed: (formError: com.google.android.ump.FormError?) -> Unit) {
        Log.d(TAG, "Showing privacy options form...")
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.e(TAG, "Privacy options form error: ${formError.message}")
                updateConsentState(error = formError.message)
            } else {
                Log.d(TAG, "Privacy options form dismissed.")
                if (::consentInformation.isInitialized) {
                    currentConsentStatus = consentInformation.consentStatus
                    updateConsentState()
                    val canRequestAds = canRequestPersonalizedAds()
                    updateConsentStatus(canRequestAds)
                }
            }
            onDismissed(formError)
        }
    }

    /**
     * Состояние согласия пользователя
     */
    data class ConsentState(
        val status: Int = ConsentInformation.ConsentStatus.UNKNOWN,
        val canRequestPersonalizedAds: Boolean = false,
        val isPrivacyOptionsRequired: Boolean = false,
        val isInitialized: Boolean = false,
        val isFormAvailable: Boolean = false,
        val error: String? = null
    )
} 