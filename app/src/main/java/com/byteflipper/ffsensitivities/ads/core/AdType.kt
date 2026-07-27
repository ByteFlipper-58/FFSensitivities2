package com.byteflipper.ffsensitivities.ads.core

import com.byteflipper.ffsensitivities.BuildConfig

/**
 * Типы рекламы в приложении
 */
enum class AdType {
    BANNER,
    INTERSTITIAL,
    REWARDED,
    APP_OPEN
}

/**
 * Локации где может показываться реклама
 */
enum class AdLocation {
    // Экраны
    DEVICES_SCREEN,
    SENSITIVITIES_SCREEN,
    HOME_SCREEN,
    SETTINGS_SCREEN,
    
    // Специальные места
    MAIN_BANNER,
    PREMIUM_FEATURES,
    EXTRA_SENSITIVITIES,
    APP_STARTUP
}

/**
 * Конфигурация для каждого типа рекламы
 */
data class AdConfig(
    val adType: AdType,
    val adUnitId: String,
    val location: AdLocation = AdLocation.MAIN_BANNER,
    val frequency: Int = 1,
    val retryDelayMs: Long = 5000L,
    val timeoutMs: Long = 30000L
)

/**
 * Упрощенная фабрика для создания конфигурации рекламы
 */
object AdConfigFactory {
    private object TestAdUnitIds {
        const val BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
        const val REWARDED = "ca-app-pub-3940256099942544/5224354917"
        const val APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
    }
    
    /**
     * Создает конфигурацию для интерстициальной рекламы
     */
    fun createInterstitialConfig(location: AdLocation): AdConfig {
        val productionAdUnitId = when (location) {
            AdLocation.SENSITIVITIES_SCREEN -> com.byteflipper.ffsensitivities.utils.AdConstants.AdUnitIds.INTERSTITIAL_SECONDARY
            else -> com.byteflipper.ffsensitivities.utils.AdConstants.AdUnitIds.INTERSTITIAL_MAIN
        }
        
        val frequency = when (location) {
            AdLocation.SENSITIVITIES_SCREEN -> com.byteflipper.ffsensitivities.utils.AdConstants.Frequency.SENSITIVITIES_SCREEN
            AdLocation.DEVICES_SCREEN -> com.byteflipper.ffsensitivities.utils.AdConstants.Frequency.DEVICES_SCREEN
            AdLocation.HOME_SCREEN -> com.byteflipper.ffsensitivities.utils.AdConstants.Frequency.HOME_SCREEN
            AdLocation.SETTINGS_SCREEN -> com.byteflipper.ffsensitivities.utils.AdConstants.Frequency.SETTINGS_SCREEN
            else -> 1
        }
        
        return AdConfig(
            adType = AdType.INTERSTITIAL,
            adUnitId = if (BuildConfig.DEBUG) TestAdUnitIds.INTERSTITIAL else productionAdUnitId,
            location = location,
            frequency = frequency
        )
    }
    
    /**
     * Создает конфигурацию для баннерной рекламы
     */
    fun createBannerConfig(location: AdLocation): AdConfig {
        return AdConfig(
            adType = AdType.BANNER,
            adUnitId = if (BuildConfig.DEBUG) TestAdUnitIds.BANNER else com.byteflipper.ffsensitivities.utils.AdConstants.AdUnitIds.BANNER_MAIN,
            location = location
        )
    }
    
    /**
     * Создает конфигурацию для наградной рекламы
     */
    fun createRewardedConfig(location: AdLocation): AdConfig {
        return AdConfig(
            adType = AdType.REWARDED,
            adUnitId = if (BuildConfig.DEBUG) TestAdUnitIds.REWARDED else com.byteflipper.ffsensitivities.utils.AdConstants.AdUnitIds.REWARDED_MAIN,
            location = location
        )
    }
    
    /**
     * Создает конфигурацию для рекламы при открытии приложения
     */
    fun createAppOpenConfig(): AdConfig {
        return AdConfig(
            adType = AdType.APP_OPEN,
            adUnitId = if (BuildConfig.DEBUG) TestAdUnitIds.APP_OPEN else com.byteflipper.ffsensitivities.utils.AdConstants.AdUnitIds.APP_OPEN_MAIN,
            location = AdLocation.APP_STARTUP
        )
    }
}
