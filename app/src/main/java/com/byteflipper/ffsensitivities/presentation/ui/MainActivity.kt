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
import com.byteflipper.ffsensitivities.ads.AdMobViewModel
import com.byteflipper.ffsensitivities.ads.ConsentManager
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
    private lateinit var appUpdateManagerWrapper: AppUpdateManagerWrapper
    private val viewModel: AppViewModel by viewModels()
    private val adMobViewModel: AdMobViewModel by viewModels() // Inject AdMobViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appUpdateManagerWrapper = AppUpdateManagerWrapper(this)

        consentManager.checkAndRequestConsent(this) { canRequestPersonalizedAds ->
            Log.d(TAG, "UMP Consent resolved in MainActivity. Can request personalized ads: $canRequestPersonalizedAds")
            // Attempt initial App Open Ad load after consent is resolved
            // Note: ConsentManager already attempts Interstitial preload internally
            // if canRequestPersonalizedAds is true and SDK is initialized.
            // We trigger AppOpenAd load here regardless of canRequestPersonalizedAds
            // as per AdMob guidance (load can happen even if showing is restricted).
            // AppOpenAdManager itself might have internal checks.
            // appOpenAdManager.loadInitialAd() // Remove this line - AdMobViewModel handles initial load
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
