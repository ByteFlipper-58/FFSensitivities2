package com.byteflipper.ffsensitivities.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.MyApplication
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ads.AdsHelper
import com.byteflipper.ffsensitivities.navigation.BottomNavigationBar
import com.byteflipper.ffsensitivities.navigation.NavigationHost
import com.byteflipper.ffsensitivities.playcore.AppUpdateManagerWrapper
import com.byteflipper.ffsensitivities.playcore.UpdateState
import com.byteflipper.ffsensitivities.presentation.ui.theme.FFSensitivitiesTheme
import com.yandex.mobile.ads.common.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var adsHelper: AdsHelper
    private lateinit var appUpdateManagerWrapper: AppUpdateManagerWrapper
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appUpdateManagerWrapper = AppUpdateManagerWrapper(this)
        MobileAds.setAgeRestrictedUser(false)
        MobileAds.setLocationConsent(true)
        MobileAds.setUserConsent(adsHelper.isUserConsentGiven())
        splashScreen.setKeepOnScreenCondition { !viewModel.isReady.value }
        setContent {
            val updateState by appUpdateManagerWrapper.updateState.collectAsState()
            MainActivityContent(
                appViewModel = viewModel,
                appUpdateManagerWrapper = appUpdateManagerWrapper,
                updateState = updateState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
fun MainActivityContent(
    appViewModel: AppViewModel,
    appUpdateManagerWrapper: AppUpdateManagerWrapper,
    updateState: UpdateState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val themeSetting by appViewModel.theme.collectAsState()
    val dynamicColorSetting by appViewModel.dynamicColor.collectAsState()
    val contrastThemeSetting by appViewModel.contrastTheme.collectAsState()

    FFSensitivitiesTheme(
        themeSetting = themeSetting,
        dynamicColorSetting = dynamicColorSetting,
        contrastThemeSetting = contrastThemeSetting
    ) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val hiddenRoutes = listOf("settings")
        val isBottomBarVisible = currentRoute !in hiddenRoutes

        LaunchedEffect(Unit) { coroutineScope.launch { appUpdateManagerWrapper.checkForUpdate() } }
        LaunchedEffect(updateState) {
            // Removed toolbarSubtitle updates, but keep the logic for potential future use (e.g., snackbar)
            val subtitleText = when (updateState) { // Store text temporarily if needed elsewhere
                UpdateState.CHECKING -> context.getString(R.string.update_checking)
                UpdateState.AVAILABLE -> context.getString(R.string.update_now_available)
                UpdateState.DOWNLOADING -> context.getString(R.string.update_downloading)
                UpdateState.DOWNLOADED -> context.getString(R.string.update_download_complete)
                UpdateState.INSTALLING -> context.getString(R.string.update_installing)
                UpdateState.INSTALLED -> ""
                UpdateState.FAILED -> context.getString(R.string.update_error_occurred)
                else -> "" // Default empty string
            }
            // Logic to auto-clear subtitle after delay is removed as subtitle state is gone.
            // Consider showing this info in a Snackbar or similar if needed.
        }
        LaunchedEffect(updateState) {
            when (updateState) {
                UpdateState.AVAILABLE -> appUpdateManagerWrapper.startUpdate()
                else -> {}
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                Column(modifier = Modifier.fillMaxWidth()) {

                    if (isBottomBarVisible) {
                        BottomNavigationBar(navController = navController)
                    }
                }
            },
            content = { innerPadding: PaddingValues ->
                NavigationHost(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        )
    }
}

@Composable
@Preview(showBackground = true)
fun MainActivityPreview() {
    val application = MyApplication()
    Text("Preview needs update due to DI changes") // Placeholder
}
