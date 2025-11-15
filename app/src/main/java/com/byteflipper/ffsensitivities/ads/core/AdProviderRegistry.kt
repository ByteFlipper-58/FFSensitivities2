package com.byteflipper.ffsensitivities.ads.core

import android.content.Context
import com.byteflipper.ffsensitivities.ads.consent.ConsentManager
import com.byteflipper.ffsensitivities.ads.providers.AppOpenAdProvider
import com.byteflipper.ffsensitivities.ads.providers.BannerAdProviderImpl
import com.byteflipper.ffsensitivities.ads.providers.InterstitialAdProvider
import com.byteflipper.ffsensitivities.ads.providers.RewardedAdProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central place for creating and storing references to ad providers.
 */
@Singleton
class AdProviderRegistry @Inject constructor(
    @ApplicationContext private val context: Context,
    private val consentManager: ConsentManager
) {

    private val adProviders = mutableMapOf<Pair<AdType, AdLocation>, AdProvider<*>>()

    init {
        initializeProviders()
    }

    private fun initializeProviders() {
        createProvider(AdType.INTERSTITIAL, AdLocation.DEVICES_SCREEN)
        createProvider(AdType.INTERSTITIAL, AdLocation.SENSITIVITIES_SCREEN)
        createProvider(AdType.INTERSTITIAL, AdLocation.HOME_SCREEN)
        createProvider(AdType.INTERSTITIAL, AdLocation.SETTINGS_SCREEN)

        createProvider(AdType.BANNER, AdLocation.MAIN_BANNER)
        createProvider(AdType.BANNER, AdLocation.DEVICES_SCREEN)
        createProvider(AdType.BANNER, AdLocation.SENSITIVITIES_SCREEN)
        createProvider(AdType.BANNER, AdLocation.HOME_SCREEN)
        createProvider(AdType.BANNER, AdLocation.SETTINGS_SCREEN)

        createProvider(AdType.REWARDED, AdLocation.PREMIUM_FEATURES)
        createProvider(AdType.REWARDED, AdLocation.EXTRA_SENSITIVITIES)

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
            AdType.INTERSTITIAL -> InterstitialAdProvider(context, config, consentManager)
            AdType.BANNER -> BannerAdProviderImpl(context, config, consentManager)
            AdType.REWARDED -> RewardedAdProvider(context, config, consentManager)
            AdType.APP_OPEN -> AppOpenAdProvider(context, config, consentManager)
        }

        adProviders[adType to location] = provider
    }

    fun getProvider(adType: AdType, location: AdLocation): AdProvider<*>? {
        return adProviders[adType to location]
    }

    fun readinessSnapshot(): Map<AdLocation, Boolean> {
        return adProviders.entries
            .groupBy { it.key.second }
            .mapValues { entry -> entry.value.any { it.value.isReady() } }
    }
}
