package com.byteflipper.ffsensitivities.core.monetization

import android.app.Activity
import com.byteflipper.ffsensitivities.ads.consent.ConsentState
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.core.AdProvider
import com.byteflipper.ffsensitivities.ads.core.AdReadyKey
import com.byteflipper.ffsensitivities.ads.core.AdResult
import com.byteflipper.ffsensitivities.ads.core.AdType
import com.google.android.ump.FormError
import kotlinx.coroutines.flow.StateFlow

interface MonetizationCoordinator {
    val consentState: StateFlow<ConsentState>
    val adReadyState: StateFlow<Map<AdReadyKey, Boolean>>
    val isPrivacyOptionsRequired: Boolean

    fun checkAndRequestConsent(activity: Activity, onConsentResolved: (Boolean) -> Unit)
    fun initializeMobileAdsSdk(onInitialized: (() -> Unit)? = null)
    fun showPrivacyOptionsForm(activity: Activity, onDismissed: (FormError?) -> Unit)
    fun canRequestPersonalizedAds(): Boolean
    fun isAdReady(adType: AdType, location: AdLocation): Boolean
    fun getAdProvider(adType: AdType, location: AdLocation): AdProvider<*>?
    fun updateReadyState()
    suspend fun loadAd(adType: AdType, location: AdLocation)
    suspend fun showAd(
        adType: AdType,
        location: AdLocation,
        activity: Activity,
        onResult: (AdResult) -> Unit = {}
    ): Boolean
    suspend fun trackActionAndShowAd(
        location: AdLocation,
        adType: AdType,
        activity: Activity,
        onResult: (AdResult) -> Unit = {}
    ): Boolean
    suspend fun showInterstitialAfterCompletedSensitivityView(
        activity: Activity,
        onResult: (AdResult) -> Unit = {}
    ): Boolean
}
