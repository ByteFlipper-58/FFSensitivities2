package com.byteflipper.ffsensitivities.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.ads.ConsentManager
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import com.byteflipper.ffsensitivities.navigation.RootAppNavigation
import com.byteflipper.ffsensitivities.playcore.AppUpdateManagerWrapper
import android.util.Log
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
    private lateinit var appUpdateManagerWrapper: AppUpdateManagerWrapper
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appUpdateManagerWrapper = AppUpdateManagerWrapper(this)

        consentManager.checkAndRequestConsent(this) { canRequestPersonalizedAds ->
            Log.d(TAG, "UMP Consent resolved in MainActivity. Can request personalized ads: $canRequestPersonalizedAds")
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
