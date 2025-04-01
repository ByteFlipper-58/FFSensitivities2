package com.byteflipper.ffsensitivities.presentation.ui

// import androidx.lifecycle.ViewModelProvider // No longer needed for Hilt VM
// import com.byteflipper.ffsensitivities.playcore.AppUpdateHandler // Removed
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.MyApplication
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ads.AdsHelper
import com.byteflipper.ffsensitivities.ads.YandexBannerAd
import com.byteflipper.ffsensitivities.navigation.BottomNavigationBar
import com.byteflipper.ffsensitivities.navigation.NavigationHost
import com.byteflipper.ffsensitivities.playcore.AppUpdateManagerWrapper
import com.byteflipper.ffsensitivities.playcore.UpdateState
import com.byteflipper.ffsensitivities.presentation.ui.theme.FFSensitivitiesTheme
import com.yandex.mobile.ads.common.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var adsHelper: AdsHelper
    // @Inject lateinit var appUpdateHandler: AppUpdateHandler // Removed injection

    private lateinit var appUpdateManagerWrapper: AppUpdateManagerWrapper // Create instance here
    private val viewModel: AppViewModel by viewModels() // Get ViewModel using Hilt delegate

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create the wrapper instance, it will observe lifecycle automatically via its init block
        appUpdateManagerWrapper = AppUpdateManagerWrapper(this)
        // lifecycle.addObserver(appUpdateHandler) // Removed

        MobileAds.setAgeRestrictedUser(false)
        MobileAds.setLocationConsent(true)
        MobileAds.setUserConsent(adsHelper.isUserConsentGiven())
        // val viewModel: AppViewModel = ViewModelProvider(this, AppViewModel.AppViewModelFactory(application))[AppViewModel::class.java] // Removed old way

        splashScreen.setKeepOnScreenCondition { !viewModel.isReady.value } // Use the delegated viewModel

        setContent {
            // Use the delegated viewModel
            val language by viewModel.language.collectAsState()

            LaunchedEffect(language) {
                val appLocale = LocaleListCompat.forLanguageTags(language)
                AppCompatDelegate.setApplicationLocales(appLocale)
            }

            // Collect update state here and pass it down
            val updateState by appUpdateManagerWrapper.updateState.collectAsState()

            MainActivityContent(
                appViewModel = viewModel, // Correct parameter name
                appUpdateManagerWrapper = appUpdateManagerWrapper, // Pass the instance
                updateState = updateState // Pass the collected state
            )
        }
    }
    // Removed updateLocale as it seems unused now
    // private fun updateLocale(language: String) { ... }
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
        var toolbarTitle by remember { mutableStateOf("Главный экран") }
        var toolbarSubtitle by remember { mutableStateOf("") }

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val hiddenRoutes = listOf("settings")
        val isBottomBarVisible = currentRoute !in hiddenRoutes

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                appUpdateManagerWrapper.checkForUpdate()
            }
        }

        LaunchedEffect(updateState) {
            val autoClearStates = listOf(
                UpdateState.AVAILABLE,
                UpdateState.FAILED,
                UpdateState.DOWNLOADED
            )

            toolbarSubtitle = when (updateState) {
                UpdateState.CHECKING -> context.getString(R.string.update_checking)
                UpdateState.AVAILABLE -> context.getString(R.string.update_now_available)
                UpdateState.DOWNLOADING -> context.getString(R.string.update_downloading)
                UpdateState.DOWNLOADED -> context.getString(R.string.update_download_complete)
                UpdateState.INSTALLING -> context.getString(R.string.update_installing)
                UpdateState.INSTALLED -> "" // Clear subtitle when installed
                UpdateState.FAILED -> context.getString(R.string.update_error_occurred)
            }

            if (updateState in autoClearStates && updateState != UpdateState.INSTALLED) {
                coroutineScope.launch {
                    delay(5000)
                    if (appUpdateManagerWrapper.updateState.value == updateState) {
                        toolbarSubtitle = ""
                    }
                }
            }
        }

        LaunchedEffect(updateState) {
            when (updateState) {
                UpdateState.AVAILABLE -> {
                    // Call startUpdate directly on the wrapper instance
                    appUpdateManagerWrapper.startUpdate()
                }
                UpdateState.DOWNLOADED -> {
                    // completeUpdate is now called automatically by the listener/onResume
                    // If manual prompt is desired, call appUpdateManagerWrapper.completeUpdate() here
                    // appUpdateManagerWrapper.completeUpdate()
                }
                else -> {}
            }
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Column {
                                Text(toolbarTitle)
                                if (toolbarSubtitle.isNotEmpty()) {
                                    Text(
                                        text = toolbarSubtitle,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            if (navController.previousBackStackEntry != null) {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Назад"
                                    )
                                }
                            }
                        },
                        actions = {
                            if (currentRoute != "settings") {
                                IconButton(onClick = {
                                    navController.navigate("settings")
                                }) {
                                    Icon(Icons.Default.Settings, contentDescription = stringResource(
                                        R.string.settings))
                                }
                            }
                        },
                        scrollBehavior = scrollBehavior
                    )

                    if (updateState in listOf(
                            UpdateState.CHECKING,
                            UpdateState.DOWNLOADING,
                            UpdateState.INSTALLING
                        )) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                        )
                    }
                }
            },
            bottomBar = {
                Column {
                    YandexBannerAd(
                        adUnitId = "R-M-13549181-2",
                        modifier = Modifier
                    )

                    AnimatedVisibility(
                        visible = isBottomBarVisible,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(animationSpec = tween(durationMillis = 300)),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(animationSpec = tween(durationMillis = 300))
                    ) {
                        BottomNavigationBar(
                            navController = navController,
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavigationHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                onTitleChange = { newTitle -> toolbarTitle = newTitle }
                // Remove onThemeChange - it will be handled directly in settings screen
                // onThemeChange = { newTheme ->
                //     appViewModel.setTheme(newTheme)
                // }
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun MainActivityPreview() {
    // Preview might need adjustment as AppViewModel now requires AppUpdateHandler
    // For simplicity, we can skip the update logic in the preview or mock it.
    // This preview will likely fail without Hilt setup.
    // Consider creating a specific preview composable or using Hilt testing utilities.
    val application = MyApplication()
    // MainActivityContent(
    //     appViewModel = AppViewModel(application) // This needs AppUpdateHandler now
    // )
    Text("Preview needs update due to DI changes") // Placeholder
}
