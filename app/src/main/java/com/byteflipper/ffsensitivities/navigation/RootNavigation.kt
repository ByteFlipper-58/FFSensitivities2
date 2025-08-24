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
import androidx.compose.runtime.*
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
import android.util.Log
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ads.components.AdBanner
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.viewmodel.SimpleAdViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import com.byteflipper.ffsensitivities.playcore.AppUpdateManagerWrapper
import com.byteflipper.ffsensitivities.playcore.UpdateState
import com.byteflipper.ui_components.PolicyScreen
import com.byteflipper.ui_components.onboarding.SimpleOnboarding
import com.byteflipper.ui_components.onboarding.buildOnboardingSteps
import com.byteflipper.ui_components.onboarding.rememberOnboardingConditions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

/**
 * Корневой граф навигации, управляющий переключением между онбордингом и основным приложением.
 */
@Composable
fun RootAppNavigation(
    dataStoreManager: DataStoreManager,
    appViewModel: AppViewModel,
    appUpdateManagerWrapper: AppUpdateManagerWrapper
) {
    Log.d("RootNavigation", "RootAppNavigation: Функция вызвана")
    val rootNavController = rememberNavController()

    // Фикс: используем промежуточный экран загрузки, чтобы корректно определить стартовый маршрут
    NavHost(
        navController = rootNavController,
        startDestination = Route.LOADING
    ) {
        composable(Route.LOADING) {
            LaunchedEffect(Unit) {
                val completed = dataStoreManager.getFirstLaunchCompleted().first()
                val target = if (completed) Route.MAIN_APP else Route.ONBOARDING
                rootNavController.navigate(target) {
                    popUpTo(Route.LOADING) { inclusive = true }
                }
            }
        }
        composable(Route.ONBOARDING) {
            Log.d("RootNavigation", "Composable 'onboarding' выполняется")
            OnboardingScreen(
                dataStoreManager = dataStoreManager,
                onFinishOnboarding = {
                    Log.d("RootNavigation", "onFinishOnboarding вызван")
                    rootNavController.navigate(Route.MAIN_APP) {
                        popUpTo(Route.ONBOARDING) { inclusive = true }
                    }
                },
                rootNavController = rootNavController
            )
        }
        composable(Route.MAIN_APP) {
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
    Log.d("OnboardingScreen", "OnboardingScreen: Функция вызвана")
    val context = LocalContext.current
    Log.d("OnboardingScreen", "Получили context: $context")
    
    var isChecked by remember { mutableStateOf(false) }
    Log.d("OnboardingScreen", "isChecked инициализирован: $isChecked")
    
    // Добавляем логирование изменения isChecked
    LaunchedEffect(isChecked) {
        Log.d("OnboardingScreen", "isChecked изменился на: $isChecked")
    }
    
    var hasNotificationPermission by remember { mutableStateOf(hasNotificationPermission(context)) }
    Log.d("OnboardingScreen", "hasNotificationPermission инициализировано: $hasNotificationPermission")

    // Создание условий завершения с помощью новой системы
    val conditions = rememberOnboardingConditions(
        "notification_permission" to "Разрешение на уведомления",
        "terms_accepted" to "Принятие условий"
    )

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> 
            hasNotificationPermission = isGranted 
            Log.d("OnboardingScreen", "Результат запроса разрешения: $isGranted")
            // Обновляем условие напрямую
            conditions.find { it.id == "notification_permission" }?.isMet?.value = isGranted
        }
    )

    // Инициализация и обновление условий
    LaunchedEffect(conditions, hasNotificationPermission, isChecked) {
        Log.d("OnboardingScreen", "Обновление условий: hasNotificationPermission=$hasNotificationPermission, isChecked=$isChecked")
        conditions.find { it.id == "notification_permission" }?.isMet?.value = hasNotificationPermission
        conditions.find { it.id == "terms_accepted" }?.let { condition ->
            Log.d("OnboardingScreen", "Устанавливаем terms_accepted: ${condition.isMet.value} -> $isChecked")
            condition.isMet.value = isChecked
        }
        Log.d("OnboardingScreen", "Условия установлены")
    }
    
    // Дополнительное обновление при изменении isChecked
    LaunchedEffect(isChecked) {
        Log.d("OnboardingScreen", "Дополнительное обновление isChecked: $isChecked")
        conditions.find { it.id == "terms_accepted" }?.isMet?.value = isChecked
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
                        // Прямое обновление через OnboardingManager
                        Log.d("OnboardingScreen", "Обновляем разрешение через manager")
                        manager.updateCondition("notification_permission", true)
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
                    Log.d("OnboardingScreen", "onCheckedChange вызван: $isChecked -> $newValue")
                    isChecked = newValue
                    // Прямое обновление через OnboardingManager
                    Log.d("OnboardingScreen", "Обновляем условие через manager")
                    manager.updateCondition("terms_accepted", newValue)
                    // Также обновляем условие напрямую
                    conditions.find { it.id == "terms_accepted" }?.isMet?.value = newValue
                }
            )
        }
    }

    SimpleOnboarding(
        steps = steps,
        conditions = conditions,
        onComplete = suspend {
            Log.d("OnboardingScreen", "OnBoarding завершен, сохраняем состояние")
            dataStoreManager.setFirstLaunchCompleted(true)
            onFinishOnboarding()
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
    val snackbarHostState = remember { SnackbarHostState() }

    val mainAppNavController = rememberNavController()
    val navBackStackEntry by mainAppNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isBottomBarVisible = currentRoute?.startsWith("settings") != true && currentRoute != Route.BUG_REPORT && currentRoute != Route.AD_TEST

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
            if (isBottomBarVisible) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AdBannerContainer(
                        currentRoute = currentRoute,
                        navController = mainAppNavController
                    )
                    BottomNavigationBar(navController = mainAppNavController)
                }
            }
        },
        content = { innerPadding: PaddingValues ->
            NavigationHost(
                navController = mainAppNavController,
                modifier = Modifier.fillMaxSize(),
                appViewModel = appViewModel
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

/**
 * Контейнер для отображения рекламных баннеров в зависимости от текущего экрана
 */
@Composable
private fun AdBannerContainer(
    currentRoute: String?,
    navController: NavHostController,
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
        val isAdReady = adReadyState[location] ?: false
        if (isAdReady) {
            AdBanner(location = location)
        }
    }
}


