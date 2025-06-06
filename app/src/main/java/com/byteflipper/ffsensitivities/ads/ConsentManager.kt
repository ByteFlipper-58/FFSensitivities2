package com.byteflipper.ffsensitivities.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.byteflipper.ffsensitivities.di.ApplicationScope
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.EnumMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsentManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val coroutineScope: CoroutineScope
) {
    private lateinit var consentInformation: ConsentInformation
    private var isMobileAdsInitialized = false
    private var currentConsentStatus: Int = ConsentInformation.ConsentStatus.UNKNOWN
    private val firebaseAnalytics: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(context) }

    // Состояние согласия для реактивного программирования
    private val _consentState = MutableStateFlow(ConsentState())
    val consentState: StateFlow<ConsentState> = _consentState.asStateFlow()

    companion object {
        private const val TAG = "ConsentManager"
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

    init {
        updateConsentState()
    }

    /**
     * Инициализирует и проверяет согласие пользователя
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
                Log.e(TAG, "Consent info update failed: ${formError.message} (Code: ${formError.errorCode})")
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
                Log.e(TAG, "Consent form load/show failed: ${loadAndShowError.message} (Code: ${loadAndShowError.errorCode})")
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
                val statusMap = initializationStatus.adapterStatusMap
                Log.d(TAG, "Google Mobile Ads initialized successfully.")
                
                // Логируем статус каждого адаптера
                for ((className, status) in statusMap) {
                    Log.d(TAG, "Adapter $className: ${status.initializationState} - ${status.description}")
                }
                
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
     * Обновляет статус согласия для релевантных SDK
     */
    private fun updateConsentStatus(consentGranted: Boolean) {
        Log.d(TAG, "Updating consent status: $consentGranted")

        // AdMob обрабатывается автоматически через UMP интеграцию
        Log.d(TAG, "AdMob consent is handled automatically by UMP integration.")

        // Обновляем Firebase Analytics
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
                Log.e(TAG, "Privacy options form error: ${formError.message} (Code: ${formError.errorCode})")
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
     * Получает текущий статус согласия UMP
     */
    fun getCurrentUmpConsentStatus(): Int {
        return if (::consentInformation.isInitialized) {
            consentInformation.consentStatus
        } else {
            ConsentInformation.ConsentStatus.UNKNOWN
        }
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
     * Принудительно обновляет информацию о согласии
     */
    fun refreshConsentInfo(activity: Activity, onComplete: (Boolean) -> Unit = {}) {
        if (!::consentInformation.isInitialized) {
            Log.w(TAG, "Cannot refresh - consent information not initialized")
            onComplete(false)
            return
        }

        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                currentConsentStatus = consentInformation.consentStatus
                updateConsentState()
                Log.d(TAG, "Consent info refreshed successfully")
                onComplete(true)
            },
            { formError ->
                Log.e(TAG, "Failed to refresh consent info: ${formError.message}")
                updateConsentState(error = formError.message)
                onComplete(false)
            }
        )
    }

    /**
     * Сбрасывает состояние согласия (для тестирования)
     */
    fun resetConsentForTesting() {
        if (::consentInformation.isInitialized) {
            consentInformation.reset()
            currentConsentStatus = ConsentInformation.ConsentStatus.UNKNOWN
            updateConsentState()
            Log.d(TAG, "Consent state reset for testing")
        }
    }
}
