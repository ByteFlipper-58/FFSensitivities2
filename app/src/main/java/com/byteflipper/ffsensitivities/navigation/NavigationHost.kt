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
import com.byteflipper.ffsensitivities.presentation.ui.screens.AboutScreen
import com.byteflipper.ffsensitivities.presentation.ui.screens.bugreport.BugReportScreen // Import BugReportScreen
import com.byteflipper.ffsensitivities.presentation.ui.screens.DevicesScreen
import com.byteflipper.ffsensitivities.presentation.ui.screens.HomeScreen
import com.byteflipper.ffsensitivities.presentation.ui.screens.SensitivitiesScreen
import com.byteflipper.ffsensitivities.presentation.ui.screens.SettingsScreen

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
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec
                )
            }
        ) {
            SettingsScreen(navController = navController, modifier = modifier)
        }
        composable(
            route = "devices/{name}/{model}",
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
            val name = backStackEntry.arguments?.getString("name").orEmpty()
            val model = backStackEntry.arguments?.getString("model").orEmpty()
            DevicesScreen(
                modifier = modifier,
                navController = navController,
                name = name,
                model = model
            )
        }
        composable(
            route = "sensitivities/{manufacturer}/{modelName}",
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
            val manufacturerArg = backStackEntry.arguments?.getString("manufacturer")
            val modelNameArg = backStackEntry.arguments?.getString("modelName")
            SensitivitiesScreen(
                navController = navController,
                manufacturerArg = manufacturerArg,
                modelNameArg = modelNameArg
            )
        }
        composable(
            route = "bug_report", // Define route for bug report screen
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, // Or Up/Down as preferred
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
