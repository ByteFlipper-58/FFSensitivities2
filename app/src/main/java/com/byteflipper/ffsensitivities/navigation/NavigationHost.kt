package com.byteflipper.ffsensitivities.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.presentation.ui.screens.AboutScreen
import com.byteflipper.ffsensitivities.presentation.ui.screens.bugreport.BugReportScreen
import com.byteflipper.ffsensitivities.presentation.ui.screens.DevicesScreen
import com.byteflipper.ffsensitivities.presentation.ui.screens.HomeScreen
import com.byteflipper.ffsensitivities.presentation.ui.screens.SensitivitiesScreen
import com.byteflipper.ffsensitivities.presentation.ui.screens.SettingsScreen
import com.byteflipper.ffsensitivities.presentation.ui.screens.settings.LanguageSettingsScreen
import com.byteflipper.ffsensitivities.presentation.ui.screens.settings.PrivacySettingsScreen
import com.byteflipper.ffsensitivities.presentation.ui.screens.settings.ThemeSettingsScreen


/**
 * Composable function that sets up the navigation graph for the application.
 *
 * This function uses Jetpack Compose's Navigation component to define the different screens
 * of the application and how to navigate between them. It also handles updating the title
 * of the screen based on the current destination.
 *
 * @param navController The NavHostController used to manage navigation.
 * @param modifier Modifier used to modify the layout of the NavHost.
 */
@Composable
fun NavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel
) {
    val animationSpec = tween<IntOffset>(durationMillis = 500)

    NavHost(
        navController = navController,
        startDestination = NavigationItem.Home.route,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(
            route = NavigationItem.Home.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec
                )
            }
        ) {
            HomeScreen(navController)
        }
        composable(
            route = NavigationItem.About.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec
                )
            }
        ) {
            AboutScreen(navController = navController, modifier = modifier)
        }
        composable(
            route = NavigationItem.Settings.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec
                )
            }
        ) {
            SettingsScreen(navController = navController, modifier = modifier, appViewModel = appViewModel)
        }
        composable(
            route = Screen.ThemeSettings.route,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec) }
        ) {
            ThemeSettingsScreen(navController = navController, modifier = modifier, appViewModel = appViewModel)
        }
        composable(
            route = Screen.PrivacySettings.route,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec) }
        ) {
            PrivacySettingsScreen(navController = navController, modifier = modifier, appViewModel = appViewModel)
        }
         composable(
            route = Screen.LanguageSettings.route,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec) }
        ) {
            LanguageSettingsScreen(navController = navController, modifier = modifier, appViewModel = appViewModel)
        }
        composable(
            route = Screen.Devices.baseRoute,
            arguments = Screen.Devices.arguments,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec
                )
            }
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString(Screen.Devices.ArgName).orEmpty()
            val model = backStackEntry.arguments?.getString(Screen.Devices.ArgModel).orEmpty()
            DevicesScreen(
                modifier = modifier,
                navController = navController,
                name = name,
                model = model
            )
        }
        composable(
            route = Screen.Sensitivities.baseRoute,
            arguments = Screen.Sensitivities.arguments,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec
                )
            }
        ) { backStackEntry ->
            val manufacturerArg = backStackEntry.arguments?.getString(Screen.Sensitivities.ArgManufacturer)
            val modelNameArg = backStackEntry.arguments?.getString(Screen.Sensitivities.ArgModelName)
            SensitivitiesScreen(
                navController = navController,
                manufacturerArg = manufacturerArg,
                modelNameArg = modelNameArg
            )
        }
        composable(
            route = Screen.BugReport.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec
                )
            }
        ) {
            BugReportScreen(navController = navController)
        }
    }
}
