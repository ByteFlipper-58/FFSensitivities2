package com.byteflipper.ffsensitivities.ads.consent

import android.app.Activity
import android.content.Context
import android.util.Log
import com.byteflipper.ffsensitivities.ads.core.AdConsentProvider
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Handles User Messaging Platform consent flow and Mobile Ads SDK initialization.
 */
@Singleton
class ConsentManager @Inject constructor(
    @ApplicationContext private val context: Context
) : AdConsentProvider {

    private companion object {
        const val TAG = "ConsentManager"
    }

    private val firebaseAnalytics: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(context) }
    private lateinit var consentInformation: ConsentInformation
    private var currentConsentStatus: Int = ConsentInformation.ConsentStatus.UNKNOWN
    private var isMobileAdsInitialized = false

    private val _consentState = MutableStateFlow(ConsentState())
    val consentState: StateFlow<ConsentState> = _consentState.asStateFlow()

    fun checkAndRequestConsent(activity: Activity, onConsentResolved: (Boolean) -> Unit) {
        consentInformation = UserMessagingPlatform.getConsentInformation(context)

        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        Log.d(TAG, "Requesting consent info update...")

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                Log.d(TAG, "Consent info updated successfully. Status: ${consentInformation.consentStatus}")
                currentConsentStatus = consentInformation.consentStatus
                updateConsentState()

                if (consentInformation.isConsentFormAvailable &&
                    currentConsentStatus == ConsentInformation.ConsentStatus.REQUIRED
                ) {
                    loadAndShowForm(activity, onConsentResolved)
                } else {
                    val canRequestAds = canRequestPersonalizedAds()
                    Log.d(TAG, "Consent form not required or not available. Can request ads: $canRequestAds")
                    updateConsentStatus(canRequestAds)
                    onConsentResolved(canRequestAds)
                }
            },
            { formError ->
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

    fun initializeMobileAdsSdk(onInitialized: (() -> Unit)? = null) {
        if (!isMobileAdsInitialized) {
            Log.d(TAG, "Initializing Google Mobile Ads SDK...")
            MobileAds.initialize(context) {
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

    override fun canRequestPersonalizedAds(): Boolean {
        return when (currentConsentStatus) {
            ConsentInformation.ConsentStatus.OBTAINED,
            ConsentInformation.ConsentStatus.NOT_REQUIRED -> true
            else -> false
        }
    }

    fun showPrivacyOptionsForm(
        activity: Activity,
        onDismissed: (formError: com.google.android.ump.FormError?) -> Unit
    ) {
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

    val isPrivacyOptionsRequired: Boolean
        get() = if (::consentInformation.isInitialized) {
            consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        } else {
            Log.w(TAG, "isPrivacyOptionsRequired checked before consentInformation initialized.")
            false
        }

    private fun updateConsentStatus(consentGranted: Boolean) {
        Log.d(TAG, "Updating consent status: $consentGranted")

        val analyticsStatus = if (consentGranted) {
            FirebaseAnalytics.ConsentStatus.GRANTED
        } else {
            FirebaseAnalytics.ConsentStatus.DENIED
        }

        val consentMap = java.util.EnumMap<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>(
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

    private fun updateConsentState(error: String? = null) {
        val isConsentInitialized = ::consentInformation.isInitialized
        val newState = ConsentState(
            status = currentConsentStatus,
            canRequestPersonalizedAds = canRequestPersonalizedAds(),
            isPrivacyOptionsRequired = isPrivacyOptionsRequired,
            isInitialized = isConsentInitialized && isMobileAdsInitialized,
            isFormAvailable = if (isConsentInitialized) {
                consentInformation.isConsentFormAvailable
            } else {
                false
            },
            error = error
        )
        _consentState.value = newState
        Log.d(TAG, "Consent state updated: $newState")
    }
}

data class ConsentState(
    val status: Int = ConsentInformation.ConsentStatus.UNKNOWN,
    val canRequestPersonalizedAds: Boolean = false,
    val isPrivacyOptionsRequired: Boolean = false,
    val isInitialized: Boolean = false,
    val isFormAvailable: Boolean = false,
    val error: String? = null
)
