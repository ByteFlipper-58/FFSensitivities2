package com.byteflipper.ffsensitivities.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.byteflipper.ffsensitivities.ads.consent.ConsentManager
import com.byteflipper.ffsensitivities.ads.consent.ConsentState
import com.byteflipper.ffsensitivities.ads.core.AdProvider
import com.byteflipper.ffsensitivities.ads.core.AdProviderRegistry
import com.byteflipper.ffsensitivities.ads.core.AdResult
import com.byteflipper.ffsensitivities.ads.core.AdType
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.tracking.AdFrequencyTracker
import com.byteflipper.ffsensitivities.di.ApplicationScope
import com.byteflipper.ffsensitivities.utils.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Coordinates consent state, provider registry, and ad frequency tracking.
 */
@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val coroutineScope: CoroutineScope,
    private val consentManager: ConsentManager,
    private val adProviderRegistry: AdProviderRegistry,
    private val adFrequencyTracker: AdFrequencyTracker
) {

    private companion object {
        private const val TAG = "AdManager"
    }

    val consentState: StateFlow<ConsentState> = consentManager.consentState

    private val _adReadyState = MutableStateFlow<Map<AdLocation, Boolean>>(emptyMap())
    val adReadyState: StateFlow<Map<AdLocation, Boolean>> = _adReadyState.asStateFlow()

    init {
        updateReadyState()
    }

    fun checkAndRequestConsent(activity: Activity, onConsentResolved: (Boolean) -> Unit) {
        consentManager.checkAndRequestConsent(activity, onConsentResolved)
    }

    fun initializeMobileAdsSdk(onInitialized: (() -> Unit)? = null) {
        consentManager.initializeMobileAdsSdk(onInitialized)
    }

    fun showPrivacyOptionsForm(
        activity: Activity,
        onDismissed: (formError: com.google.android.ump.FormError?) -> Unit
    ) {
        consentManager.showPrivacyOptionsForm(activity, onDismissed)
    }

    val isPrivacyOptionsRequired: Boolean
        get() = consentManager.isPrivacyOptionsRequired

    fun canRequestPersonalizedAds(): Boolean = consentManager.canRequestPersonalizedAds()

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

        val provider = adProviderRegistry.getProvider(adType, location)
        if (provider == null) {
            Log.e(TAG, "No provider found for ad type: $adType at location: $location")
            onResult(AdResult(adType, false, error = IllegalStateException("No provider")))
            return false
        }

        return try {
            val result = provider.show(activity)
            onResult(result)

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

    suspend fun trackActionAndShowAd(
        location: AdLocation,
        adType: AdType,
        activity: Activity,
        onResult: (AdResult) -> Unit = {}
    ): Boolean {
        val provider = adProviderRegistry.getProvider(adType, location) ?: return false
        val frequency = provider.config.frequency

        val currentCount = adFrequencyTracker.increment(location)
        Log.d(TAG, "Action tracked for $location: $currentCount/$frequency")

        if (currentCount >= frequency) {
            Log.d(TAG, "Frequency reached for $location, attempting to show $adType ad")
            val shown = showAd(adType, location, activity, onResult)
            if (shown) {
                adFrequencyTracker.reset(location)
                Log.d(TAG, "Ad shown successfully, counter reset for $location")
            } else {
                Log.w(TAG, "Failed to show ad for $location, counter NOT reset")
            }
            return shown
        }

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

    fun isAdReady(adType: AdType, location: AdLocation): Boolean {
        return adProviderRegistry.getProvider(adType, location)?.isReady() ?: false
    }

    fun getAdProvider(adType: AdType, location: AdLocation): AdProvider<*>? {
        return adProviderRegistry.getProvider(adType, location)
    }

    suspend fun loadAd(adType: AdType, location: AdLocation) {
        val provider = adProviderRegistry.getProvider(adType, location)
        if (provider != null) {
            try {
                provider.load()
                updateReadyState()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load ad: $adType at $location", e)
            }
        }
    }

    fun updateReadyState() {
        val readyMap = adProviderRegistry.readinessSnapshot()
        _adReadyState.value = readyMap
        Log.d(TAG, "Ad ready state updated: $readyMap")
    }

    private fun canShowAds(): Boolean {
        return consentManager.canRequestPersonalizedAds() && NetworkUtils.isInternetConnected(context)
    }
}
