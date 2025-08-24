package com.byteflipper.ffsensitivities.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.ads.AdManager
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import com.byteflipper.ffsensitivities.navigation.RootAppNavigation
import com.byteflipper.ffsensitivities.playcore.AppUpdateManagerWrapper
import com.byteflipper.ffsensitivities.presentation.ui.theme.FFSensitivitiesTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private companion object {
        private const val TAG = "MainActivity"
    }

    @Inject lateinit var adManager: AdManager
    @Inject lateinit var dataStoreManager: DataStoreManager
    private lateinit var appUpdateManagerWrapper: AppUpdateManagerWrapper
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Включаем Edge-to-Edge дизайн
        enableEdgeToEdge()
        
        // Настраиваем Window для правильной работы с системными панелями
        // Это особенно важно для Android 15+ (API 35)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        appUpdateManagerWrapper = AppUpdateManagerWrapper(this)

        adManager.checkAndRequestConsent(this) { canRequestPersonalizedAds ->
            Log.d(TAG, "UMP согласие получено в MainActivity. Можно запрашивать персонализированную рекламу: $canRequestPersonalizedAds")
        }

        splashScreen.setKeepOnScreenCondition { 
            !viewModel.isReady.value
        }

        setContent {
            val themeSetting by viewModel.theme.collectAsState()
            val dynamicColorSetting by viewModel.dynamicColor.collectAsState()
            val contrastThemeSetting by viewModel.contrastTheme.collectAsState()

            FFSensitivitiesTheme(
                themeSetting = themeSetting,
                dynamicColorSetting = dynamicColorSetting,
                contrastThemeSetting = contrastThemeSetting
            ) {
                RootAppNavigation(
                    dataStoreManager = dataStoreManager,
                    appViewModel = viewModel,
                    appUpdateManagerWrapper = appUpdateManagerWrapper
                )
            }
        }
    }
}
