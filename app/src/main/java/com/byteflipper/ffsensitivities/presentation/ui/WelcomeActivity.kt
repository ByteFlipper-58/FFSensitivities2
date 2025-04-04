package com.byteflipper.ffsensitivities.presentation.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.data.local.isFirstLaunch
import com.byteflipper.ffsensitivities.data.local.setFirstLaunchCompleted
import com.byteflipper.ffsensitivities.presentation.ui.components.OnboardingBottomBar
import com.byteflipper.ffsensitivities.presentation.ui.screens.OnboardingInfoScreen
import com.byteflipper.ffsensitivities.presentation.ui.screens.OnboardingPermissionsScreen
import com.byteflipper.ffsensitivities.presentation.ui.screens.PolicyScreen
import com.byteflipper.ffsensitivities.presentation.ui.screens.WelcomeScreen
import com.byteflipper.ffsensitivities.presentation.ui.theme.FFSensitivitiesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Add Hilt annotation
class WelcomeActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels() // Inject ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val isFirstLaunch = isFirstLaunch(context = this)

        if (!isFirstLaunch) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContent {
            // Collect theme settings state
            val themeSetting by appViewModel.theme.collectAsStateWithLifecycle()
            val dynamicColorSetting by appViewModel.dynamicColor.collectAsStateWithLifecycle()
            val contrastThemeSetting by appViewModel.contrastTheme.collectAsStateWithLifecycle()

            FFSensitivitiesTheme(
                themeSetting = themeSetting,
                dynamicColorSetting = dynamicColorSetting,
                contrastThemeSetting = contrastThemeSetting
            ) {
                val navController = rememberNavController()
                val coroutineScope = rememberCoroutineScope()
                var isPolicyAccepted by remember { mutableStateOf(false) }

                // Get current route to determine screen index and visibility of bottom bar
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val currentRoute = currentDestination?.route

                val onboardingScreens = listOf("onboarding_info", "onboarding_permissions", "welcome")
                val currentScreenIndex = onboardingScreens.indexOf(currentRoute)
                val showBottomBar = currentRoute in onboardingScreens

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            OnboardingBottomBar(
                                totalScreens = onboardingScreens.size,
                                currentScreenIndex = currentScreenIndex,
                                onBackClick = { navController.popBackStack() },
                                onNextClick = {
                                    val nextRoute = onboardingScreens.getOrNull(currentScreenIndex + 1)
                                    if (nextRoute != null) {
                                        navController.navigate(nextRoute) {
                                            // Avoid multiple copies of the same destination when re-selecting
                                            launchSingleTop = true
                                            // Restore state when re-selecting a previously selected item
                                            restoreState = true
                                        }
                                    }
                                },
                                onFinishClick = {
                                    setFirstLaunchCompleted(context = this@WelcomeActivity)
                                    startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                                    finish()
                                },
                                backEnabled = currentScreenIndex > 0,
                                finishEnabled = currentScreenIndex == onboardingScreens.lastIndex && isPolicyAccepted,
                                nextEnabled = currentScreenIndex < onboardingScreens.lastIndex
                            )
                        }
                    }
                ) { innerPadding -> // Pass padding to NavHost
                    NavHost(
                        navController = navController,
                        startDestination = "onboarding_info", // Start with the info screen
                        modifier = Modifier.padding(innerPadding) // Apply padding from Scaffold
                    ) {
                        // Define smoother tween animation spec for IntOffset
                        val animationSpec: FiniteAnimationSpec<IntOffset> = tween(
                            durationMillis = 400, // Slightly longer duration
                            easing = FastOutSlowInEasing // Standard easing curve
                        )

                        composable(
                            route = "onboarding_info",
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = animationSpec) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = animationSpec) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = animationSpec) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = animationSpec) }
                        ) {
                            OnboardingInfoScreen(
                                navController = navController,
                                paddingValues = PaddingValues() // Pass empty padding, Scaffold handles it
                            )
                        }
                        composable(
                            route = "onboarding_permissions",
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = animationSpec) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = animationSpec) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = animationSpec) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = animationSpec) }
                        ) {
                            OnboardingPermissionsScreen(
                                navController = navController,
                                paddingValues = PaddingValues() // Pass empty padding
                            )
                        }
                        composable(
                            route = "welcome",
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = animationSpec) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = animationSpec) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = animationSpec) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = animationSpec) }
                        ) {
                            WelcomeScreen(
                                navController = navController,
                                isChecked = isPolicyAccepted, // Pass state
                                onCheckedChange = { isPolicyAccepted = it }, // Pass lambda
                                paddingValues = PaddingValues() // Pass empty padding
                            )
                        }

                        composable(
                            "policy/{documentType}",
                            arguments = listOf(navArgument("documentType") { type = NavType.StringType })
                            // No onboarding animations for policy screen
                        ) { backStackEntry ->
                            val documentType = backStackEntry.arguments?.getString("documentType") ?: "privacy_policy"
                            PolicyScreen(
                                documentType = documentType,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}
