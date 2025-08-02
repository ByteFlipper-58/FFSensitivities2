package com.byteflipper.ffsensitivities.utils

object AdConstants {
    // Основные ID рекламы
    object AdUnitIds {
        // Интерстициальная реклама
        const val INTERSTITIAL_MAIN = "ca-app-pub-4346225518624754/1656811533"
        const val INTERSTITIAL_SECONDARY = "ca-app-pub-4346225518624754/9596813057"
        
        // Баннерная реклама
        const val BANNER_MAIN = "ca-app-pub-4346225518624754/7047467848"
        
        // Наградная реклама (тестовые ID)
        const val REWARDED_TEST = "ca-app-pub-3940256099942544/5224354917"
        
        // Реклама при открытии приложения
        const val APP_OPEN_MAIN = "ca-app-pub-4346225518624754/5182925326"
    }

    // Частота показа рекламы
    object Frequency {
        const val SENSITIVITIES_SCREEN = 3
        const val DEVICES_SCREEN = 4
        const val HOME_SCREEN = 2
        const val SETTINGS_SCREEN = 5
    }

    // Размеры баннеров
    object BannerSizes {
        const val WIDTH = 320
        const val HEIGHT = 50
    }

    // Устаревшие константы для обратной совместимости
    @Deprecated("Используйте AdUnitIds.INTERSTITIAL_MAIN")
    object Interstitial {
        const val DEVICES_SCREEN = AdUnitIds.INTERSTITIAL_MAIN
        const val SENSITIVITIES_SCREEN = AdUnitIds.INTERSTITIAL_SECONDARY
        const val HOME_SCREEN = AdUnitIds.INTERSTITIAL_MAIN
        const val SETTINGS_SCREEN = AdUnitIds.INTERSTITIAL_SECONDARY
    }

    @Deprecated("Используйте AdUnitIds.BANNER_MAIN")
    object Banner {
        const val MAIN_BANNER = AdUnitIds.BANNER_MAIN
        const val DEVICES_SCREEN = AdUnitIds.BANNER_MAIN
        const val SENSITIVITIES_SCREEN = AdUnitIds.BANNER_MAIN
        const val HOME_SCREEN = AdUnitIds.BANNER_MAIN
        const val SETTINGS_SCREEN = AdUnitIds.BANNER_MAIN
    }

    @Deprecated("Используйте AdUnitIds.REWARDED_TEST")
    object Rewarded {
        const val MAIN = AdUnitIds.REWARDED_TEST
        const val PREMIUM_FEATURES = AdUnitIds.REWARDED_TEST
        const val EXTRA_SENSITIVITIES = AdUnitIds.REWARDED_TEST
    }

    @Deprecated("Используйте AdUnitIds.APP_OPEN_MAIN")
    object AppOpen {
        const val MAIN = AdUnitIds.APP_OPEN_MAIN
    }

    @Deprecated("Используйте BannerSizes.WIDTH")
    const val BANNER_WIDTH = BannerSizes.WIDTH
    
    @Deprecated("Используйте BannerSizes.HEIGHT")
    const val BANNER_HEIGHT = BannerSizes.HEIGHT
}
