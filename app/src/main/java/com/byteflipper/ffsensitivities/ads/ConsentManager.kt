package com.byteflipper.ffsensitivities.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.byteflipper.ffsensitivities.MyApplication
import com.byteflipper.ffsensitivities.di.ApplicationScope
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.yandex.mobile.ads.common.MobileAds
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
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
                    initializeMobileAdsIfNeeded(canRequestAds)
                    onConsentResolved(canRequestAds)
                }
            },
            { formError ->
                // Handle the error. Assume consent denied or non-personalized ads.
                Log.e(TAG, "Consent info update failed: ${formError.message} (Code: ${formError.errorCode})")
                currentConsentStatus = ConsentInformation.ConsentStatus.UNKNOWN // Or treat as denied
                val canRequestAds = canRequestPersonalizedAds() // Will likely be false
                initializeMobileAdsIfNeeded(canRequestAds)
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
            initializeMobileAdsIfNeeded(canRequestAds)
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

    private fun initializeMobileAdsIfNeeded(consentGranted: Boolean) {
        MobileAds.setUserConsent(consentGranted)
        Log.d(TAG, "Set Yandex MobileAds.setUserConsent($consentGranted)")

        if (!isMobileAdsInitialized) {
            // Initialize Yandex SDK only once
            MobileAds.initialize(context) {
                Log.d(TAG, "Yandex Mobile Ads initialized successfully.")
                isMobileAdsInitialized = true
                // Initialize AdManagerHolder here, ensuring it happens after SDK init and consent set
                // Ensure context is ApplicationContext if AdManagerHolder needs it long-term
                if (context is MyApplication) {
                     AdManagerHolder.initialize(context)
                     Log.d(TAG, "AdManagerHolder initialized.")
                } else {
                      AdManagerHolder.initialize(context.applicationContext)
                      Log.w(TAG, "Initialized AdManagerHolder with applicationContext fallback.")
                 }
             }
         } else {
              Log.d(TAG, "Yandex MobileAds already initialized. Consent status updated.")
        }
    }

    // Optional: Function to get current UMP status if needed elsewhere
    fun getCurrentUmpConsentStatus(): Int {
        return consentInformation.consentStatus // Returns UMP status code
    }
}
