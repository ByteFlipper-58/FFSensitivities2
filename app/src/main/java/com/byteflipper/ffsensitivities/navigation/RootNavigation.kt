package com.byteflipper.ffsensitivities.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import com.byteflipper.ffsensitivities.playcore.AppUpdateManagerWrapper
import com.byteflipper.ffsensitivities.playcore.UpdateState
import com.byteflipper.ffsensitivities.presentation.ui.screens.onboarding.WelcomeAgreementContent
import com.byteflipper.ui_components.PolicyScreen
import com.byteflipper.ui_components.onboarding.OnboardingPage
import com.byteflipper.ui_components.onboarding.OnboardingPager
import com.byteflipper.ui_components.onboarding.screens.OnboardingInfoScreen
import com.byteflipper.ui_components.onboarding.screens.OnboardingPermissionsScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

/**
 * Корневой граф навигации, управляющий переключением между онбордингом и основным приложением.
 */
@Composable
fun RootAppNavigation(
    dataStoreManager: DataStoreManager,
    appViewModel: AppViewModel,
    appUpdateManagerWrapper: AppUpdateManagerWrapper
) {
    val onboardingCompleted by dataStoreManager.getFirstLaunchCompleted().collectAsState(initial = null)

    if (onboardingCompleted == null) {
        return
    }

    val rootNavController = rememberNavController()
    val startDestination = if (onboardingCompleted == false) "onboarding" else "main_app"

    NavHost(
        navController = rootNavController,
        startDestination = startDestination
    ) {
        composable("onboarding") {
            OnboardingScreen(
                dataStoreManager = dataStoreManager,
                onFinishOnboarding = {
                    rootNavController.navigate("main_app") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                rootNavController = rootNavController
            )
        }
        composable("main_app") {
            MainAppScaffold(
                appViewModel = appViewModel,
                appUpdateManagerWrapper = appUpdateManagerWrapper
            )
        }
        composable(
            "policy/{documentType}",
            arguments = listOf(navArgument("documentType") { type = NavType.StringType })
        ) { backStackEntry ->
            val documentType = backStackEntry.arguments?.getString("documentType") ?: "privacy_policy"
            val title = when (documentType) {
                "privacy_policy" -> stringResource(R.string.privacy_dialog_title)
                "terms" -> stringResource(R.string.terms_of_service)
                else -> stringResource(R.string.privacy_dialog_title)
            }
            val url = "file:///android_asset/${documentType}.html"

            PolicyScreen(
                title = title,
                contentUrl = url,
                onBackClick = { rootNavController.popBackStack() },
                backContentDescription = stringResource(R.string.back)
            )
        }
    }
}

@Composable
private fun OnboardingScreen(
    dataStoreManager: DataStoreManager,
    onFinishOnboarding: () -> Unit,
    rootNavController: NavHostController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isChecked by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember { mutableStateOf(hasNotificationPermission(context)) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasNotificationPermission = isGranted }
    )

    val onboardingPages = listOf(
        OnboardingPage { _, padding ->
            OnboardingInfoScreen(
                navController = rootNavController,
                paddingValues = padding,
                logoPainter = painterResource(id = R.drawable.logo),
                title = stringResource(R.string.onboarding_info_title),
                description = stringResource(R.string.onboarding_info_description),
                imageContentDescription = stringResource(R.string.onboarding_image_desc)
            )
        },
        OnboardingPage { _, padding ->
            OnboardingPermissionsScreen(
                paddingValues = padding,
                title = stringResource(R.string.onboarding_permissions_title),
                description = stringResource(R.string.onboarding_permissions_description),
                grantButtonText = stringResource(R.string.grant_permission),
                grantedButtonText = stringResource(R.string.permission_granted),
                isPermissionGranted = hasNotificationPermission,
                onGrantPermissionClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        hasNotificationPermission = true
                    }
                }
            )
        },
        OnboardingPage { _, padding ->
            WelcomeAgreementContent(
                navController = rootNavController,
                isChecked = isChecked,
                onCheckedChange = { isChecked = it },
                paddingValues = padding
            )
        }
    )

    val isFinishButtonEnabled by remember(hasNotificationPermission, isChecked) {
        mutableStateOf(hasNotificationPermission && isChecked)
    }

    OnboardingPager(
        pages = onboardingPages,
        navController = rootNavController,
        isFinishEnabled = isFinishButtonEnabled,
        onFinish = {
            coroutineScope.launch {
                dataStoreManager.setFirstLaunchCompleted(true)
                onFinishOnboarding()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
private fun MainAppScaffold(
    appViewModel: AppViewModel,
    appUpdateManagerWrapper: AppUpdateManagerWrapper
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val updateState by appUpdateManagerWrapper.updateState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() } // Add SnackbarHostState

    val mainAppNavController = rememberNavController()
    val navBackStackEntry by mainAppNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    // Hide bottom bar on settings screens (main and sub-screens) and bug report screen
    val isBottomBarVisible = currentRoute?.startsWith("settings") != true && currentRoute != "bug_report"

    LaunchedEffect(Unit) { coroutineScope.launch { appUpdateManagerWrapper.checkForUpdate() } }
    LaunchedEffect(updateState) {
        val message = when (updateState) {
            UpdateState.AVAILABLE -> context.getString(R.string.update_now_available)
            UpdateState.DOWNLOADING -> context.getString(R.string.update_downloading)
            UpdateState.DOWNLOADED -> context.getString(R.string.update_download_complete)
            UpdateState.FAILED -> context.getString(R.string.update_error_occurred)
            else -> null
        }
        message?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }
    LaunchedEffect(updateState) {
        if (updateState == UpdateState.AVAILABLE) {
            appUpdateManagerWrapper.startUpdate()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isBottomBarVisible) {
                    BottomNavigationBar(navController = mainAppNavController)
                }
            }
        },
        content = { innerPadding: PaddingValues ->
            NavigationHost(
                navController = mainAppNavController,
                modifier = Modifier.fillMaxSize(),
                appViewModel = appViewModel // Pass AppViewModel here
            )
        }
    )
}

private fun hasNotificationPermission(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}
