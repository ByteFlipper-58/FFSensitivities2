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

    companion object {
        private const val TAG = "ConsentManager"
    }

    /**
     * Initializes ConsentInformation and checks/requests consent.
     */
    fun checkAndRequestConsent(activity: Activity, onConsentResolved: (Boolean) -> Unit) {
        consentInformation = UserMessagingPlatform.getConsentInformation(context)

        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            { // Success listener
                Log.d(TAG, "Consent info updated successfully. Status: ${consentInformation.consentStatus}")
                currentConsentStatus = consentInformation.consentStatus

                if (consentInformation.isConsentFormAvailable &&
                    currentConsentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                    loadAndShowForm(activity, onConsentResolved)
                } else {
                    val canRequestAds = canRequestPersonalizedAds()
                    Log.d(TAG, "Consent form not required or not available. Can request ads: $canRequestAds")
                    updateConsentStatus(canRequestAds)
                    // Interstitial preload logic removed - handled by ViewModel
                    onConsentResolved(canRequestAds)
                }
            },
            { formError -> // Error listener
                Log.e(TAG, "Consent info update failed: ${formError.message} (Code: ${formError.errorCode})")
                currentConsentStatus = ConsentInformation.ConsentStatus.UNKNOWN
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
            } else {
                currentConsentStatus = consentInformation.consentStatus
                Log.d(TAG, "Consent form shown and dismissed. New status: $currentConsentStatus")
            }
            val canRequestAds = canRequestPersonalizedAds()
            Log.d(TAG, "After form attempt, can request ads: $canRequestAds")
            updateConsentStatus(canRequestAds)
            // Interstitial preload logic removed - handled by ViewModel
            onConsentResolved(canRequestAds)
        }
    }

    /**
     * Determines if personalized ads can be requested based on the consent status.
     */
    fun canRequestPersonalizedAds(): Boolean {
        return when (currentConsentStatus) {
            ConsentInformation.ConsentStatus.OBTAINED,
            ConsentInformation.ConsentStatus.NOT_REQUIRED -> true
            else -> false
        }
    }

    /**
     * Initializes the Google Mobile Ads SDK.
     */
    fun initializeMobileAdsSdk(onInitialized: (() -> Unit)? = null) {
        if (!isMobileAdsInitialized) {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "Google Mobile Ads initialized successfully. Status: ${initializationStatus.adapterStatusMap}")
                isMobileAdsInitialized = true
                onInitialized?.invoke()
                // Interstitial preload logic removed - handled by ViewModel
            }
        } else {
            Log.d(TAG, "Google Mobile Ads SDK already initialized.")
            // If SDK already initialized, still invoke callback if provided
            onInitialized?.invoke()
        }
    }

    /**
     * Updates the user consent status for relevant SDKs.
     */
    private fun updateConsentStatus(consentGranted: Boolean) {
        Log.d(TAG, "AdMob consent is handled automatically by UMP integration.")

        val analyticsStatus = if (consentGranted) FirebaseAnalytics.ConsentStatus.GRANTED else FirebaseAnalytics.ConsentStatus.DENIED
        val consentMap = EnumMap<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>(FirebaseAnalytics.ConsentType::class.java)
        consentMap[FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE] = analyticsStatus
        consentMap[FirebaseAnalytics.ConsentType.AD_STORAGE] = analyticsStatus
        consentMap[FirebaseAnalytics.ConsentType.AD_USER_DATA] = analyticsStatus
        consentMap[FirebaseAnalytics.ConsentType.AD_PERSONALIZATION] = analyticsStatus
        firebaseAnalytics.setConsent(consentMap)
        Log.d(TAG, "Set FirebaseAnalytics consent: $consentMap")
    }

    /**
     * Checks if the privacy options form is required.
     */
    val isPrivacyOptionsRequired: Boolean
        get() = if (::consentInformation.isInitialized) {
            consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        } else {
            // Should ideally not happen if checkAndRequestConsent was called
            Log.w(TAG, "isPrivacyOptionsRequired checked before consentInformation initialized.")
            false
        }


    /**
     * Shows the privacy options form.
     */
    fun showPrivacyOptionsForm(activity: Activity, onDismissed: (formError: com.google.android.ump.FormError?) -> Unit) {
        Log.d(TAG, "Showing privacy options form...")
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.e(TAG, "Privacy options form error: ${formError.message} (Code: ${formError.errorCode})")
            } else {
                Log.d(TAG, "Privacy options form dismissed.")
                currentConsentStatus = consentInformation.consentStatus
                val canRequestAds = canRequestPersonalizedAds()
                updateConsentStatus(canRequestAds)
            }
            onDismissed(formError)
        }
    }

    /**
     * Gets the current UMP consent status.
     */
    fun getCurrentUmpConsentStatus(): Int {
        return if (::consentInformation.isInitialized) {
            consentInformation.consentStatus
        } else {
            ConsentInformation.ConsentStatus.UNKNOWN
        }
    }
}
