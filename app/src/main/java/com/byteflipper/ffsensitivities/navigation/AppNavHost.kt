package com.byteflipper.ffsensitivities.navigation

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ads.components.AdBanner
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.viewmodel.SimpleAdViewModel
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import com.byteflipper.ffsensitivities.playcore.AppUpdateManagerWrapper
import com.byteflipper.ffsensitivities.playcore.UpdateState
import com.byteflipper.ui_components.PolicyScreen
import com.byteflipper.ui_components.onboarding.OnboardingContainer
import com.byteflipper.ui_components.onboarding.OnboardingManager
import com.byteflipper.ui_components.onboarding.buildOnboardingSteps
import com.byteflipper.ui_components.onboarding.rememberOnboardingConditions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    dataStoreManager: DataStoreManager,
    appViewModel: AppViewModel,
    appUpdateManagerWrapper: AppUpdateManagerWrapper
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val updateState by appUpdateManagerWrapper.updateState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val shouldShowBottomBar = currentRoute?.let { route ->
        route.startsWith(NavigationItem.Home.route) ||
            route.startsWith("devices/") ||
            route.startsWith("sensitivities/") ||
            route == NavigationItem.About.route
    } ?: false

    LaunchedEffect(Unit) {
        coroutineScope.launch { appUpdateManagerWrapper.checkForUpdate() }
    }

    LaunchedEffect(updateState) {
        val message = when (updateState) {
            UpdateState.AVAILABLE -> context.getString(R.string.update_now_available)
            UpdateState.DOWNLOADING -> context.getString(R.string.update_downloading)
            UpdateState.DOWNLOADED -> context.getString(R.string.update_download_complete)
            UpdateState.FAILED -> context.getString(R.string.update_error_occurred)
            else -> null
        }
        message?.let { coroutineScope.launch { snackbarHostState.showSnackbar(it) } }
    }

    LaunchedEffect(updateState) {
        if (updateState == UpdateState.AVAILABLE) {
            appUpdateManagerWrapper.startUpdate()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (shouldShowBottomBar) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AdBannerContainer(currentRoute = currentRoute)
                    BottomNavigationBar(navController = navController)
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.LOADING,
            modifier = Modifier
                .fillMaxSize()
                //.padding(innerPadding)
        ) {
            composable(Route.LOADING) {
                LaunchedEffect(Unit) {
                    val completed = dataStoreManager.getFirstLaunchCompleted().first()
                    val target = if (completed) Route.MAIN_APP else Route.ONBOARDING
                    navController.navigate(target) {
                        popUpTo(Route.LOADING) { inclusive = true }
                    }
                }
            }
            composable(Route.ONBOARDING) {
                OnboardingScreen(
                    dataStoreManager = dataStoreManager,
                    onFinishOnboarding = {
                        navController.navigate(Route.MAIN_APP) {
                            popUpTo(Route.ONBOARDING) { inclusive = true }
                        }
                    },
                    rootNavController = navController
                )
            }
            navigation(
                startDestination = Screen.Home.route,
                route = Route.MAIN_APP
            ) {
                mainAppGraph(navController, appViewModel)
            }
            composable(
                Route.POLICY_BASE,
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
                    onBackClick = { navController.popBackStack() },
                    backContentDescription = stringResource(R.string.back)
                )
            }
        }
    }
}

@Composable
private fun OnboardingScreen(
    dataStoreManager: DataStoreManager,
    onFinishOnboarding: () -> Unit,
    rootNavController: NavHostController,
    adViewModel: SimpleAdViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var hasNotificationPermission by remember { mutableStateOf(hasNotificationPermission(context)) }
    var isChecked by remember { mutableStateOf(false) }

    val notificationConditionLabel = stringResource(id = R.string.onboarding_permissions_title)
    val termsConditionLabel = stringResource(id = R.string.welcome_screen_title)

    val conditions = rememberOnboardingConditions(
        "notification_permission" to notificationConditionLabel,
        "terms_accepted" to termsConditionLabel
    )

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    val consentState by adViewModel.consentState.collectAsState()
    LaunchedEffect(consentState) {
        Log.d("OnboardingScreen", "Consent State: $consentState")
    }

    val onboardingInfoTitle = stringResource(R.string.onboarding_info_title)
    val onboardingInfoDescription = stringResource(R.string.onboarding_info_description)
    val onboardingImageDesc = stringResource(R.string.onboarding_image_desc)
    val onboardingPermissionsTitle = stringResource(R.string.onboarding_permissions_title)
    val onboardingPermissionsDescription = stringResource(R.string.onboarding_permissions_description)
    val grantPermissionText = stringResource(R.string.grant_permission)
    val permissionGrantedText = stringResource(R.string.permission_granted)
    val welcomeScreenTitle = stringResource(R.string.welcome_screen_title)

    val steps = buildOnboardingSteps {
        step(
            id = "welcome",
            title = onboardingInfoTitle,
            isSkippable = false
        ) {
            OnboardingInfoStepContent(
                logoPainter = painterResource(id = R.drawable.logo),
                title = onboardingInfoTitle,
                description = onboardingInfoDescription,
                imageContentDescription = onboardingImageDesc
            )
        }
        step(
            id = "permissions",
            title = onboardingPermissionsTitle,
            isSkippable = true
        ) {
            OnboardingPermissionsStepContent(
                title = onboardingPermissionsTitle,
                description = onboardingPermissionsDescription,
                grantButtonText = grantPermissionText,
                grantedButtonText = permissionGrantedText,
                isPermissionGranted = hasNotificationPermission,
                onGrantPermissionClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        hasNotificationPermission = true
                    }
                }
            )
        }
        step(
            id = "agreement",
            title = welcomeScreenTitle,
            isSkippable = false
        ) {
            WelcomeAgreementStepContent(
                navController = rootNavController,
                isChecked = isChecked,
                onCheckedChange = { newValue ->
                    isChecked = newValue
                }
            )
        }
    }

    val onboardingManager = remember { OnboardingManager() }

    LaunchedEffect(steps) {
        onboardingManager.setupSteps(steps)
    }

    LaunchedEffect(conditions) {
        onboardingManager.setFinishConditions(conditions)
    }

    LaunchedEffect(hasNotificationPermission) {
        onboardingManager.updateCondition("notification_permission", hasNotificationPermission)
    }

    LaunchedEffect(isChecked) {
        onboardingManager.updateCondition("terms_accepted", isChecked)
    }

    OnboardingContainer(
        manager = onboardingManager,
        onComplete = suspend {
            dataStoreManager.setFirstLaunchCompleted(true)
            onFinishOnboarding()
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

@Composable
private fun AdBannerContainer(
    currentRoute: String?,
    adViewModel: SimpleAdViewModel = hiltViewModel()
) {
    val adLocation = when {
        currentRoute == Route.HOME -> AdLocation.HOME_SCREEN
        currentRoute?.startsWith("devices/") == true -> AdLocation.DEVICES_SCREEN
        currentRoute?.startsWith("sensitivities/") == true -> AdLocation.SENSITIVITIES_SCREEN
        currentRoute == Route.AD_TEST -> AdLocation.SETTINGS_SCREEN
        else -> null
    }

    val adReadyState by adViewModel.adReadyState.collectAsState()
    adLocation?.let { location ->
        if (adReadyState[location] == true) {
            AdBanner(location = location)
        }
    }
}
