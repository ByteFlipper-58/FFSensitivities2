package com.byteflipper.ffsensitivities.presentation.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.appcompat.app.AppCompatActivity
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.ads.lifecycle.AppOpenAdLifecycleObserver
import com.byteflipper.ffsensitivities.core.monetization.MonetizationCoordinator
import com.byteflipper.ffsensitivities.core.update.AppUpdateCoordinator
import com.byteflipper.ffsensitivities.core.update.AppUpdateCoordinatorFactory
import com.byteflipper.ffsensitivities.navigation.AppNavHost
import com.byteflipper.ffsensitivities.presentation.ui.theme.FFSensitivitiesTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "MainActivity"
    }

    @Inject lateinit var monetizationCoordinator: MonetizationCoordinator
    @Inject lateinit var appOpenAdObserver: AppOpenAdLifecycleObserver
    @Inject lateinit var appUpdateCoordinatorFactory: AppUpdateCoordinatorFactory
    private lateinit var appUpdateCoordinator: AppUpdateCoordinator
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Включаем Edge-to-Edge дизайн
        enableEdgeToEdge()
        
        // Настраиваем Window для правильной работы с системными панелями
        // Это особенно важно для Android 15+ (API 35)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        appUpdateCoordinator = appUpdateCoordinatorFactory.create(this)

        monetizationCoordinator.checkAndRequestConsent(this) { canRequestPersonalizedAds ->
            Log.d(TAG, "UMP согласие получено в MainActivity. Можно запрашивать персонализированную рекламу: $canRequestPersonalizedAds")
            if (canRequestPersonalizedAds) {
                appOpenAdObserver.preloadAfterConsent()
            }
        }

        splashScreen.setKeepOnScreenCondition { 
            !viewModel.isReady.value
        }

        setContent {
            val themeSetting by viewModel.theme.collectAsStateWithLifecycle()
            val dynamicColorSetting by viewModel.dynamicColor.collectAsStateWithLifecycle()
            val contrastThemeSetting by viewModel.contrastTheme.collectAsStateWithLifecycle()

            FFSensitivitiesTheme(
                themeSetting = themeSetting,
                dynamicColorSetting = dynamicColorSetting,
                contrastThemeSetting = contrastThemeSetting
            ) {
                AppNavHost(
                    appViewModel = viewModel,
                    appUpdateCoordinator = appUpdateCoordinator
                )
            }
        }
    }
}
