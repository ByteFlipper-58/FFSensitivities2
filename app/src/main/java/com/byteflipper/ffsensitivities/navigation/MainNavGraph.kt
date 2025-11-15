package com.byteflipper.ffsensitivities.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.presentation.about.AboutScreen
import com.byteflipper.ffsensitivities.presentation.adtest.AdTestScreen
import com.byteflipper.ffsensitivities.presentation.bugreport.BugReportScreen
import com.byteflipper.ffsensitivities.presentation.devices.DevicesScreen
import com.byteflipper.ffsensitivities.presentation.home.HomeScreen
import com.byteflipper.ffsensitivities.presentation.sensitivities.SensitivitiesScreen
import com.byteflipper.ffsensitivities.presentation.settings.LanguageSettingsScreen
import com.byteflipper.ffsensitivities.presentation.settings.PrivacySettingsScreen
import com.byteflipper.ffsensitivities.presentation.settings.SettingsScreen
import com.byteflipper.ffsensitivities.presentation.settings.ThemeSettingsScreen

private val defaultAnimation = tween<IntOffset>(durationMillis = 500)

private fun AnimatedContentTransitionScope<*>.defaultEnter(direction: AnimatedContentTransitionScope.SlideDirection) =
    slideIntoContainer(direction, defaultAnimation)

private fun AnimatedContentTransitionScope<*>.defaultExit(direction: AnimatedContentTransitionScope.SlideDirection) =
    slideOutOfContainer(direction, defaultAnimation)

fun NavGraphBuilder.mainAppGraph(
    navController: NavHostController,
    appViewModel: AppViewModel
) {
    composable(
        route = Screen.Home.route,
        enterTransition = { defaultEnter(AnimatedContentTransitionScope.SlideDirection.Start) },
        exitTransition = { defaultExit(AnimatedContentTransitionScope.SlideDirection.Start) },
        popEnterTransition = { defaultEnter(AnimatedContentTransitionScope.SlideDirection.End) },
        popExitTransition = { defaultExit(AnimatedContentTransitionScope.SlideDirection.End) }
    ) {
        HomeScreen(navController)
    }

    composable(
        route = Screen.About.route,
        enterTransition = { defaultEnter(AnimatedContentTransitionScope.SlideDirection.Start) },
        exitTransition = { defaultExit(AnimatedContentTransitionScope.SlideDirection.Start) },
        popEnterTransition = { defaultEnter(AnimatedContentTransitionScope.SlideDirection.End) },
        popExitTransition = { defaultExit(AnimatedContentTransitionScope.SlideDirection.End) }
    ) {
        AboutScreen(navController = navController)
    }

    composable(
        route = Screen.Settings.route,
        enterTransition = { defaultEnter(AnimatedContentTransitionScope.SlideDirection.Start) },
        exitTransition = { defaultExit(AnimatedContentTransitionScope.SlideDirection.Start) },
        popEnterTransition = { defaultEnter(AnimatedContentTransitionScope.SlideDirection.End) },
        popExitTransition = { defaultExit(AnimatedContentTransitionScope.SlideDirection.End) }
    ) {
        SettingsScreen(navController = navController, appViewModel = appViewModel)
    }

    composable(
        route = Screen.ThemeSettings.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        ThemeSettingsScreen(
            navController = navController,
            appViewModel = appViewModel
        )
    }

    composable(
        route = Screen.LanguageSettings.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        LanguageSettingsScreen(navController = navController)
    }

    composable(
        route = Screen.PrivacySettings.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        PrivacySettingsScreen(navController = navController)
    }

    composable(
        route = Screen.Devices.baseRoute,
        arguments = Screen.Devices.arguments,
        enterTransition = { defaultEnter(AnimatedContentTransitionScope.SlideDirection.Start) },
        exitTransition = { defaultExit(AnimatedContentTransitionScope.SlideDirection.Start) },
        popEnterTransition = { defaultEnter(AnimatedContentTransitionScope.SlideDirection.End) },
        popExitTransition = { defaultExit(AnimatedContentTransitionScope.SlideDirection.End) }
    ) { backStack ->
        val name = backStack.arguments?.getString(Screen.Devices.ArgName)
        val model = backStack.arguments?.getString(Screen.Devices.ArgModel)
        DevicesScreen(navController = navController, name = name, model = model)
    }

    composable(
        route = Screen.Sensitivities.baseRoute,
        arguments = Screen.Sensitivities.arguments,
        enterTransition = { defaultEnter(AnimatedContentTransitionScope.SlideDirection.Start) },
        exitTransition = { defaultExit(AnimatedContentTransitionScope.SlideDirection.Start) },
        popEnterTransition = { defaultEnter(AnimatedContentTransitionScope.SlideDirection.End) },
        popExitTransition = { defaultExit(AnimatedContentTransitionScope.SlideDirection.End) }
    ) { backStack ->
        val manufacturer = backStack.arguments?.getString(Screen.Sensitivities.ArgManufacturer)
        val modelName = backStack.arguments?.getString(Screen.Sensitivities.ArgModelName)
        SensitivitiesScreen(
            navController = navController,
            manufacturerArg = manufacturer,
            modelNameArg = modelName
        )
    }

    composable(
        route = Screen.BugReport.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        BugReportScreen(navController = navController)
    }

    composable(
        route = Screen.AdTest.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        AdTestScreen(navController = navController)
    }
}
