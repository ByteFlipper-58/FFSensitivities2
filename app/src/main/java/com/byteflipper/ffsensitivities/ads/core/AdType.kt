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
 * Фабрика для создания конфигурации рекламы на основе локации
 */
object AdConfigFactory {
    
    /**
     * Создает конфигурацию для интерстициальной рекламы
     */
    fun createInterstitialConfig(location: AdLocation): AdConfig {
        return when (location) {
            AdLocation.DEVICES_SCREEN -> AdConfig(
                adType = AdType.INTERSTITIAL,
                adUnitId = com.byteflipper.ffsensitivities.utils.AdConstants.Interstitial.DEVICES_SCREEN,
                location = location,
                frequency = com.byteflipper.ffsensitivities.utils.AdConstants.Frequency.DEVICES_SCREEN
            )
            AdLocation.SENSITIVITIES_SCREEN -> AdConfig(
                adType = AdType.INTERSTITIAL,
                adUnitId = com.byteflipper.ffsensitivities.utils.AdConstants.Interstitial.SENSITIVITIES_SCREEN,
                location = location,
                frequency = com.byteflipper.ffsensitivities.utils.AdConstants.Frequency.SENSITIVITIES_SCREEN
            )
            AdLocation.HOME_SCREEN -> AdConfig(
                adType = AdType.INTERSTITIAL,
                adUnitId = com.byteflipper.ffsensitivities.utils.AdConstants.Interstitial.HOME_SCREEN,
                location = location,
                frequency = com.byteflipper.ffsensitivities.utils.AdConstants.Frequency.HOME_SCREEN
            )
            AdLocation.SETTINGS_SCREEN -> AdConfig(
                adType = AdType.INTERSTITIAL,
                adUnitId = com.byteflipper.ffsensitivities.utils.AdConstants.Interstitial.SETTINGS_SCREEN,
                location = location,
                frequency = com.byteflipper.ffsensitivities.utils.AdConstants.Frequency.SETTINGS_SCREEN
            )
            else -> throw IllegalArgumentException("Неподдерживаемая локация для интерстициальной рекламы: $location")
        }
    }
    
    /**
     * Создает конфигурацию для баннерной рекламы
     */
    fun createBannerConfig(location: AdLocation): AdConfig {
        return when (location) {
            AdLocation.MAIN_BANNER -> AdConfig(
                adType = AdType.BANNER,
                adUnitId = com.byteflipper.ffsensitivities.utils.AdConstants.Banner.MAIN_BANNER,
                location = location
            )
            AdLocation.DEVICES_SCREEN -> AdConfig(
                adType = AdType.BANNER,
                adUnitId = com.byteflipper.ffsensitivities.utils.AdConstants.Banner.DEVICES_SCREEN,
                location = location
            )
            AdLocation.SENSITIVITIES_SCREEN -> AdConfig(
                adType = AdType.BANNER,
                adUnitId = com.byteflipper.ffsensitivities.utils.AdConstants.Banner.SENSITIVITIES_SCREEN,
                location = location
            )
            AdLocation.HOME_SCREEN -> AdConfig(
                adType = AdType.BANNER,
                adUnitId = com.byteflipper.ffsensitivities.utils.AdConstants.Banner.HOME_SCREEN,
                location = location
            )
            AdLocation.SETTINGS_SCREEN -> AdConfig(
                adType = AdType.BANNER,
                adUnitId = com.byteflipper.ffsensitivities.utils.AdConstants.Banner.SETTINGS_SCREEN,
                location = location
            )
            else -> throw IllegalArgumentException("Неподдерживаемая локация для баннерной рекламы: $location")
        }
    }
    
    /**
     * Создает конфигурацию для наградной рекламы
     */
    fun createRewardedConfig(location: AdLocation): AdConfig {
        return when (location) {
            AdLocation.PREMIUM_FEATURES -> AdConfig(
                adType = AdType.REWARDED,
                adUnitId = com.byteflipper.ffsensitivities.utils.AdConstants.Rewarded.PREMIUM_FEATURES,
                location = location
            )
            AdLocation.EXTRA_SENSITIVITIES -> AdConfig(
                adType = AdType.REWARDED,
                adUnitId = com.byteflipper.ffsensitivities.utils.AdConstants.Rewarded.EXTRA_SENSITIVITIES,
                location = location
            )
            else -> AdConfig(
                adType = AdType.REWARDED,
                adUnitId = com.byteflipper.ffsensitivities.utils.AdConstants.Rewarded.MAIN,
                location = location
            )
        }
    }
    
    /**
     * Создает конфигурацию для рекламы при открытии приложения
     */
    fun createAppOpenConfig(): AdConfig {
        return AdConfig(
            adType = AdType.APP_OPEN,
            adUnitId = com.byteflipper.ffsensitivities.utils.AdConstants.AppOpen.MAIN,
            location = AdLocation.APP_STARTUP
        )
    }
} 