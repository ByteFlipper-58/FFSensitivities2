package com.byteflipper.ffsensitivities.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Sealed class to define navigation routes for better type safety and organization.
 */
sealed class Screen(val route: String) {
    object Home : Screen(NavigationItem.Home.route)
    object About : Screen(NavigationItem.About.route)
    object Settings : Screen(NavigationItem.Settings.route)

    // Screens accessible from Settings
    object ThemeSettings : Screen("settings_theme")
    object PrivacySettings : Screen("settings_privacy")
    object LanguageSettings : Screen("settings_language")

    // Other Screens
    data class Devices(val name: String, val model: String) : Screen("devices/$name/$model") {
        companion object {
            const val baseRoute = "devices/{name}/{model}"
            // Define argument keys directly in companion object
            const val ArgName = "name"
            const val ArgModel = "model"
            val arguments = listOf(
                navArgument(ArgName) { type = NavType.StringType }, // Use direct constants
                navArgument(ArgModel) { type = NavType.StringType }  // Use direct constants
            )
        }
    }

    data class Sensitivities(val manufacturer: String, val modelName: String) : Screen("sensitivities/$manufacturer/$modelName") {
        companion object {
            const val baseRoute = "sensitivities/{manufacturer}/{modelName}"
            // Define argument keys directly in companion object
            const val ArgManufacturer = "manufacturer"
            const val ArgModelName = "modelName"
            val arguments = listOf(
                navArgument(ArgManufacturer) { type = NavType.StringType }, // Use direct constants
                navArgument(ArgModelName) { type = NavType.StringType }  // Use direct constants
            )
        }
    }

    object BugReport : Screen("bug_report")

    data class Policy(val documentType: String) : Screen("policy/$documentType") {
         companion object {
            const val baseRoute = "policy/{documentType}"
            // Define argument key directly in companion object
            const val ArgDocumentType = "documentType"
            val arguments = listOf(
                navArgument(ArgDocumentType) { type = NavType.StringType } // Use direct constants
            )
        }
    }
}
