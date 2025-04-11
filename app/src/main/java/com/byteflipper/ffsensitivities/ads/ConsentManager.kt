package com.byteflipper.ffsensitivities.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.byteflipper.ffsensitivities.MyApplication
import com.byteflipper.ffsensitivities.di.ApplicationScope
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.google.firebase.analytics.FirebaseAnalytics
import com.yandex.mobile.ads.common.MobileAds
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import java.util.EnumMap
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ConsentManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val coroutineScope: CoroutineScope, // Use qualifier
    private val appOpenAdManager: AppOpenAdManager
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
     * This should be called early, e.g., in Activity's onCreate.
     *
     * @param activity The Activity context needed to show the consent form.
     * @param onConsentResolved Callback invoked after consent is determined or the form is dismissed.
     *                          Parameter indicates if personalized ads are allowed based on consent status.
     */
    fun checkAndRequestConsent(activity: Activity, onConsentResolved: (Boolean) -> Unit) {
        consentInformation = UserMessagingPlatform.getConsentInformation(context)

        // Set up debug settings if needed (use device's Advertising ID)
        // val debugSettings = ConsentDebugSettings.Builder(context)
        //     .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
        //     .addTestDeviceHashedId("YOUR_TEST_DEVICE_HASHED_ID") // Replace with your ID
        //     .build()

        val params = ConsentRequestParameters.Builder()
            // .setConsentDebugSettings(debugSettings) // Uncomment for testing
            .setTagForUnderAgeOfConsent(false) // Set appropriately based on your app's target audience
            .build()

        // Request consent information update
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // Consent info updated successfully. Check if form is available and needed.
                Log.d(TAG, "Consent info updated successfully. Status: ${consentInformation.consentStatus}")
                currentConsentStatus = consentInformation.consentStatus

                // Load and show the form if required.
                if (consentInformation.isConsentFormAvailable &&
                    currentConsentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                    loadAndShowForm(activity, onConsentResolved)
                } else {
                    // Form not needed or not available, proceed with current status
                    val canRequestAds = canRequestPersonalizedAds()
                    Log.d(TAG, "Consent form not required or not available. Can request ads: $canRequestAds")
                    // Update consent status for relevant SDKs
                    updateConsentStatus(canRequestAds)
                    onConsentResolved(canRequestAds)
                }
            },
            { formError ->
                // Handle the error. Assume consent denied or non-personalized ads.
                Log.e(TAG, "Consent info update failed: ${formError.message} (Code: ${formError.errorCode})")
                currentConsentStatus = ConsentInformation.ConsentStatus.UNKNOWN // Or treat as denied
                val canRequestAds = canRequestPersonalizedAds() // Will likely be false
                // Update consent status for relevant SDKs
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
                currentConsentStatus = ConsentInformation.ConsentStatus.UNKNOWN // Or treat as denied
            } else {
                currentConsentStatus = consentInformation.consentStatus
                Log.d(TAG, "Consent form shown and dismissed. New status: $currentConsentStatus")
            }

            val canRequestAds = canRequestPersonalizedAds()
            Log.d(TAG, "After form attempt, can request ads: $canRequestAds")
            // Update consent status for relevant SDKs
            updateConsentStatus(canRequestAds)
            onConsentResolved(canRequestAds)
        }
    }

    /**
     * Determines if personalized ads can be requested based on the consent status.
     * Yandex expects a simple boolean.
     */
    private fun canRequestPersonalizedAds(): Boolean {
        return when (currentConsentStatus) {
            ConsentInformation.ConsentStatus.OBTAINED -> true
            ConsentInformation.ConsentStatus.NOT_REQUIRED -> true
            ConsentInformation.ConsentStatus.REQUIRED -> false
            ConsentInformation.ConsentStatus.UNKNOWN -> false
            else -> false
        }
    }

    /**
     * Initializes the Yandex Mobile Ads SDK if it hasn't been initialized yet.
     * This should be called once, early in the application lifecycle (e.g., Application.onCreate).
     */
    fun initializeMobileAdsSdk() {
        if (!isMobileAdsInitialized) {
            MobileAds.initialize(context) {
                Log.d(TAG, "Yandex Mobile Ads initialized successfully.")
                isMobileAdsInitialized = true
                // Initialize AdManagerHolder here, ensuring it happens after SDK init
                // Ensure context is ApplicationContext if AdManagerHolder needs it long-term
                // Consider moving AdManagerHolder initialization also to Application.onCreate
                // if it doesn't strictly depend on the SDK init callback.
                if (context is MyApplication) {
                    AdManagerHolder.initialize(context)
                    Log.d(TAG, "AdManagerHolder initialized.")
                } else {
                    // Using applicationContext is generally safer here
                    AdManagerHolder.initialize(context.applicationContext)
                    Log.w(TAG, "Initialized AdManagerHolder with applicationContext.")
                }
            }
        } else {
            Log.d(TAG, "Yandex Mobile Ads SDK already initialized.")
        }
    }

    /**
     * Updates the user consent status for relevant SDKs (Yandex Ads, Firebase Analytics).
     * This should be called after the consent status is determined by UMP.
     */
    private fun updateConsentStatus(consentGranted: Boolean) {
        // Update Yandex Ads Consent
        MobileAds.setUserConsent(consentGranted)
        Log.d(TAG, "Set Yandex MobileAds.setUserConsent($consentGranted)")

        // Update Firebase Analytics Consent Mode
        val analyticsStatus = if (consentGranted) FirebaseAnalytics.ConsentStatus.GRANTED else FirebaseAnalytics.ConsentStatus.DENIED
        val consentMap = EnumMap<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>(FirebaseAnalytics.ConsentType::class.java)
        // Assuming basic mapping: if ads are allowed, analytics/ad data is too.
        // Adjust this logic if you have more granular consent options in your UMP form.
        consentMap[FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE] = analyticsStatus
        consentMap[FirebaseAnalytics.ConsentType.AD_STORAGE] = analyticsStatus
        consentMap[FirebaseAnalytics.ConsentType.AD_USER_DATA] = analyticsStatus
        consentMap[FirebaseAnalytics.ConsentType.AD_PERSONALIZATION] = analyticsStatus
        firebaseAnalytics.setConsent(consentMap)
        Log.d(TAG, "Set FirebaseAnalytics consent: $consentMap")


        // Note: We might need to call AdManagerHolder initialization or update
        // here as well if its behavior depends on the *updated* consent status,
        // but currently it seems tied only to the SDK initialization itself.
    }

    // --- Privacy Options ---

    /**
     * Checks if the privacy options form is required to be shown.
     * This should be checked after `requestConsentInfoUpdate` completes.
     */
    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /**
     * Shows the privacy options form to the user.
     * This should be called from a user interface element (e.g., a button in settings)
     * only when `isPrivacyOptionsRequired` is true.
     *
     * @param activity The Activity context needed to show the form.
     * @param onDismissed Callback invoked when the form is dismissed. You should re-check
     *                    consent status and update SDKs accordingly after dismissal.
     */
    fun showPrivacyOptionsForm(activity: Activity, onDismissed: (formError: com.google.android.ump.FormError?) -> Unit) {
        Log.d(TAG, "Showing privacy options form...")
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.e(TAG, "Privacy options form error: ${formError.message} (Code: ${formError.errorCode})")
            } else {
                Log.d(TAG, "Privacy options form dismissed.")
                // Important: After the form is dismissed, the consent status might have changed.
                // You MUST re-query the status and update the SDKs.
                // The simplest way is often to re-run the consent check logic.
                // The caller (e.g., Settings screen) should handle this via the callback.
                currentConsentStatus = consentInformation.consentStatus // Update local status
                val canRequestAds = canRequestPersonalizedAds()
                updateConsentStatus(canRequestAds) // Update SDKs immediately based on new status
            }
            onDismissed(formError)
        }
    }


    // Optional: Function to get current UMP status if needed elsewhere
    fun getCurrentUmpConsentStatus(): Int {
        // Ensure consentInformation is initialized before accessing
        return if (::consentInformation.isInitialized) {
            consentInformation.consentStatus
        } else {
            ConsentInformation.ConsentStatus.UNKNOWN
        }
    }
}
