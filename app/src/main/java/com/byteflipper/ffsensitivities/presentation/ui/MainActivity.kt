package com.byteflipper.ffsensitivities.presentation.ui

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.MyApplication
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ads.AdsHelper
import com.byteflipper.ffsensitivities.ads.YandexBannerAd
import com.byteflipper.ffsensitivities.data.local.PreferencesManager
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
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var appUpdateManager: AppUpdateManagerWrapper
    //private var appOpenAdManager: AppOpenAdManager? = null

    @Inject lateinit var adsHelper: AdsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appUpdateManager = AppUpdateManagerWrapper(this)

        MobileAds.setAgeRestrictedUser(false)
        MobileAds.setLocationConsent(true)
        MobileAds.setUserConsent(adsHelper.isUserConsentGiven())
        //appOpenAdManager = (application as MyApplication).appOpenAdManager

        setContent {
            val viewModel: AppViewModel = viewModel(factory = AppViewModel.AppViewModelFactory(application))

            // Получаем текущий язык из AppViewModel
            val language by viewModel.language.collectAsState()

            // Устанавливаем язык через AppCompatDelegate при изменении
            LaunchedEffect(language) {
                val appLocale = LocaleListCompat.forLanguageTags(language)
                AppCompatDelegate.setApplicationLocales(appLocale)
            }

            /*appOpenAdManager?.let { adManager ->
                DisposableEffect(Unit) {
                    val lifecycleObserver = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> adManager.onActivityResumed(this@MainActivity)
                            Lifecycle.Event.ON_PAUSE -> adManager.onActivityPaused(this@MainActivity)
                            else -> {}
                        }
                    }

                    lifecycle.addObserver(lifecycleObserver)
                    onDispose {
                        lifecycle.removeObserver(lifecycleObserver)
                    }
                }
            }*/

            MainActivityContent(appUpdateManager, viewModel)
        }
    }

    private fun updateLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration().apply { setLocale(locale) }
        resources.updateConfiguration(config, resources.displayMetrics)
        recreate()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
fun MainActivityContent(
    appUpdateManager: AppUpdateManagerWrapper,
    appViewModel: AppViewModel
) {
    val context = LocalContext.current
    val preferencesManager = PreferencesManager(context)
    val coroutineScope = rememberCoroutineScope()

    val updateState by appUpdateManager.updateState

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            appUpdateManager.checkForUpdate()
        }
    }

    ProvidePreferenceLocals {
        val dynamicColorState by appViewModel.dynamicColor.collectAsState()
        val contrastThemeState by appViewModel.contrastTheme.collectAsState()
        val selectedTheme by appViewModel.theme.collectAsState()

        val darkTheme = when (selectedTheme) {
            "dark" -> true
            "light" -> false
            else -> isSystemInDarkTheme()
        }

        FFSensitivitiesTheme(
            darkTheme = darkTheme,
            dynamicColor = dynamicColorState,
            contrastTheme = contrastThemeState
        ) {
            val navController = rememberNavController()
            var toolbarTitle by remember { mutableStateOf("Главный экран") }
            var toolbarSubtitle by remember { mutableStateOf("") }

            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val hiddenRoutes = listOf("settings")
            val isBottomBarVisible = currentRoute !in hiddenRoutes

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
                    UpdateState.INSTALLED -> ""
                    UpdateState.FAILED -> context.getString(R.string.update_error_occurred)
                }

                if (updateState in autoClearStates) {
                    coroutineScope.launch {
                        delay(5000)
                        toolbarSubtitle = ""
                    }
                }

                when (updateState) {
                    UpdateState.AVAILABLE -> {
                        appUpdateManager.startUpdate()
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

                        // Bottom Navigation
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
                    onTitleChange = { newTitle -> toolbarTitle = newTitle },
                    onThemeChange = { newTheme ->
                        appViewModel.setTheme(newTheme)
                    }
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun MainActivityPreview() {
    val application = MyApplication()
    MainActivityContent(
        appUpdateManager = AppUpdateManagerWrapper(ComponentActivity()),
        appViewModel = AppViewModel(application)
    )
}