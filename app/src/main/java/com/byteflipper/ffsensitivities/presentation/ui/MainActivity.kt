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
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.ads.ConsentManager
import com.byteflipper.ffsensitivities.ads.repository.AdRepository
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

    @Inject lateinit var consentManager: ConsentManager
    @Inject lateinit var dataStoreManager: DataStoreManager
    @Inject lateinit var adRepository: AdRepository
    private lateinit var appUpdateManagerWrapper: AppUpdateManagerWrapper
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appUpdateManagerWrapper = AppUpdateManagerWrapper(this)

        consentManager.checkAndRequestConsent(this) { canRequestPersonalizedAds ->
            Log.d(TAG, "UMP согласие получено в MainActivity. Можно запрашивать персонализированную рекламу: $canRequestPersonalizedAds")
            // AdRepository автоматически начнет предзагрузку рекламы после получения согласия
            // через свой механизм отслеживания состояния ConsentManager
        }

        splashScreen.setKeepOnScreenCondition { !viewModel.isReady.value }

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
