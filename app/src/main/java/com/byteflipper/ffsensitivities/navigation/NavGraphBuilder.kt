package com.byteflipper.ffsensitivities.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.composableWithTransitions(
    route: String,
    enterTitle: String,
    onTitleChange: (String) -> Unit,
    enterTransition: (AnimatedContentTransitionScope<*>.() -> EnterTransition) = { fadeIn(animationSpec = tween(500)) },
    exitTransition: (AnimatedContentTransitionScope<*>.() -> ExitTransition) = { fadeOut(animationSpec = tween(500)) },
    content: @Composable () -> Unit
) {
    composable(
        route = route,
        enterTransition = enterTransition,
        exitTransition = exitTransition
    ) {
        onTitleChange(enterTitle)
        content()
    }
}

fun NavGraphBuilder.composableWithArguments(
    route: String,
    argumentKeys: List<String>,
    enterTitle: (Map<String, String>) -> String,
    onTitleChange: (String) -> Unit,
    enterTransition: (AnimatedContentTransitionScope<*>.() -> EnterTransition) = { fadeIn(animationSpec = tween(500)) },
    exitTransition: (AnimatedContentTransitionScope<*>.() -> ExitTransition) = { fadeOut(animationSpec = tween(500)) },
    content: @Composable (Map<String, String>) -> Unit
) {
    composable(
        route = route,
        enterTransition = enterTransition,
        exitTransition = exitTransition
    ) { backStackEntry ->
        val args = argumentKeys.associateWith { key ->
            backStackEntry.arguments?.getString(key).orEmpty()
        }
        onTitleChange(enterTitle(args))
        content(args)
    }
}