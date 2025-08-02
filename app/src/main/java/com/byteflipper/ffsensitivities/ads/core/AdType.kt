package com.byteflipper.ffsensitivities.ads.core

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
    
    /**
     * Создает конфигурацию для интерстициальной рекламы
     */
    fun createInterstitialConfig(location: AdLocation): AdConfig {
        val adUnitId = when (location) {
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
            adUnitId = adUnitId,
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
            adUnitId = com.byteflipper.ffsensitivities.utils.AdConstants.AdUnitIds.BANNER_MAIN,
            location = location
        )
    }
    
    /**
     * Создает конфигурацию для наградной рекламы
     */
    fun createRewardedConfig(location: AdLocation): AdConfig {
        return AdConfig(
            adType = AdType.REWARDED,
            adUnitId = com.byteflipper.ffsensitivities.utils.AdConstants.AdUnitIds.REWARDED_TEST,
            location = location
        )
    }
    
    /**
     * Создает конфигурацию для рекламы при открытии приложения
     */
    fun createAppOpenConfig(): AdConfig {
        return AdConfig(
            adType = AdType.APP_OPEN,
            adUnitId = com.byteflipper.ffsensitivities.utils.AdConstants.AdUnitIds.APP_OPEN_MAIN,
            location = AdLocation.APP_STARTUP
        )
    }
} 