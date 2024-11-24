package com.byteflipper.ffsensitivities.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ui.screens.AboutScreen
import com.byteflipper.ffsensitivities.ui.screens.DevicesScreen
import com.byteflipper.ffsensitivities.ui.screens.HomeScreen
import com.byteflipper.ffsensitivities.ui.screens.SensitivitiesScreen
import com.byteflipper.ffsensitivities.ui.screens.SettingsScreen
import kotlinx.serialization.Serializable

/**
 * Composable function that sets up the navigation graph for the application.
 *
 * This function uses Jetpack Compose's Navigation component to define the different screens
 * of the application and how to navigate between them. It also handles updating the title
 * of the screen based on the current destination.
 *
 * @param navController The NavHostController used to manage navigation.
 * @param modifier Modifier used to modify the layout of the NavHost.
 * @param onTitleChange Callback function to update the title of the screen.
 */
@Composable
fun NavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onTitleChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
) {
    NavHost(navController, startDestination = NavigationItem.Home.route, modifier = modifier) {
        composable(
            NavigationItem.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(500)) },
            exitTransition = { fadeOut(animationSpec = tween(500)) }
        ) {
            onTitleChange(stringResource(R.string.app_name))
            HomeScreen(navController)
        }
        composable(
            NavigationItem.About.route,
            enterTransition = { fadeIn(animationSpec = tween(500)) },
            exitTransition = { fadeOut(animationSpec = tween(500)) }
        ) {
            onTitleChange(stringResource(R.string.about_app))
            AboutScreen(modifier)
        }
        composable(
            NavigationItem.Settings.route,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(500)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(500)) }
        ) {
            onTitleChange(stringResource(R.string.settings))
            SettingsScreen(modifier, onThemeChange)
        }
        composable(
            "devices/{name}/{model}",
            enterTransition = { fadeIn(animationSpec = tween(500)) },
            exitTransition = { fadeOut(animationSpec = tween(500)) }
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name").orEmpty()
            val model = backStackEntry.arguments?.getString("model").orEmpty()
            onTitleChange(stringResource(R.string.devices_list) + " $name")
            DevicesScreen(modifier, navController, model)
        }
        composable(
            "sensitivities/{manufacturer}/{model}/{device}",
            enterTransition = { fadeIn(animationSpec = tween(500)) },
            exitTransition = { fadeOut(animationSpec = tween(500)) }
        ) { backStackEntry ->
            val manufacturer = backStackEntry.arguments?.getString("manufacturer").orEmpty()
            val model = backStackEntry.arguments?.getString("model").orEmpty()
            val device = backStackEntry.arguments?.getString("device").orEmpty()
            onTitleChange("$manufacturer $model")
            SensitivitiesScreen(navController, device)
        }
    }
}