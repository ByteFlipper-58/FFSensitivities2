package com.byteflipper.ffsensitivities.core.monetization

import android.app.Activity
import com.byteflipper.ffsensitivities.ads.AdManager
import com.byteflipper.ffsensitivities.ads.consent.ConsentState
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.core.AdProvider
import com.byteflipper.ffsensitivities.ads.core.AdReadyKey
import com.byteflipper.ffsensitivities.ads.core.AdResult
import com.byteflipper.ffsensitivities.ads.core.AdType
import com.google.android.ump.FormError
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

@Singleton
class DefaultMonetizationCoordinator @Inject constructor(
    private val adManager: AdManager
) : MonetizationCoordinator {

    override val consentState: StateFlow<ConsentState> = adManager.consentState
    override val adReadyState: StateFlow<Map<AdReadyKey, Boolean>> = adManager.adReadyState
    override val isPrivacyOptionsRequired: Boolean
        get() = adManager.isPrivacyOptionsRequired

    override fun checkAndRequestConsent(activity: Activity, onConsentResolved: (Boolean) -> Unit) {
        adManager.checkAndRequestConsent(activity, onConsentResolved)
    }

    override fun initializeMobileAdsSdk(onInitialized: (() -> Unit)?) {
        adManager.initializeMobileAdsSdk(onInitialized)
    }

    override fun showPrivacyOptionsForm(activity: Activity, onDismissed: (FormError?) -> Unit) {
        adManager.showPrivacyOptionsForm(activity, onDismissed)
    }

    override fun canRequestPersonalizedAds(): Boolean = adManager.canRequestPersonalizedAds()

    override fun isAdReady(adType: AdType, location: AdLocation): Boolean {
        return adManager.isAdReady(adType, location)
    }

    override fun getAdProvider(adType: AdType, location: AdLocation): AdProvider<*>? {
        return adManager.getAdProvider(adType, location)
    }

    override fun updateReadyState() {
        adManager.updateReadyState()
    }

    override suspend fun loadAd(adType: AdType, location: AdLocation) {
        adManager.loadAd(adType, location)
    }

    override suspend fun showAd(
        adType: AdType,
        location: AdLocation,
        activity: Activity,
        onResult: (AdResult) -> Unit
    ): Boolean {
        return adManager.showAd(adType, location, activity, onResult)
    }

    override suspend fun trackActionAndShowAd(
        location: AdLocation,
        adType: AdType,
        activity: Activity,
        onResult: (AdResult) -> Unit
    ): Boolean {
        return adManager.trackActionAndShowAd(location, adType, activity, onResult)
    }

    override suspend fun showInterstitialAfterCompletedSensitivityView(
        activity: Activity,
        onResult: (AdResult) -> Unit
    ): Boolean = adManager.showInterstitialAfterCompletedSensitivityView(activity, onResult)
}
