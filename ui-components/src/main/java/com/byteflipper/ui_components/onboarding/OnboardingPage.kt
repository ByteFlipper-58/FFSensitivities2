package com.byteflipper.ui_components.onboarding

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

/**
 * Represents a single page within the onboarding flow.
 *
 * @param content The Composable function that defines the UI content of this page.
 *                It receives NavController and PaddingValues as parameters.
 */
data class OnboardingPage(
    val content: @Composable (navController: NavController, paddingValues: PaddingValues) -> Unit
)
