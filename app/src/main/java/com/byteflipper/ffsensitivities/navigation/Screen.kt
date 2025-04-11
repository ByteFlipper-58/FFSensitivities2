package com.byteflipper.ffsensitivities.navigation

/**
 * Sealed class to define navigation routes for better type safety and organization.
 */
sealed class Screen(val route: String) {
    // Bottom Navigation Items (referencing NavigationItem for consistency)
    object Home : Screen(NavigationItem.Home.route)
    object About : Screen(NavigationItem.About.route)
    object Settings : Screen(NavigationItem.Settings.route)

    // Screens accessible from Settings
    object ThemeSettings : Screen("settings_theme")
    object PrivacySettings : Screen("settings_privacy")
    object LanguageSettings : Screen("settings_language") // Uncommented route

    // Other Screens
    object Devices : Screen("devices/{name}/{model}") {
        fun createRoute(name: String, model: String) = "devices/$name/$model"
    }
    object Sensitivities : Screen("sensitivities/{manufacturer}/{modelName}") {
        fun createRoute(manufacturer: String, modelName: String) = "sensitivities/$manufacturer/$modelName"
    }
    object BugReport : Screen("bug_report")

    // Policy Screen (defined in RootNavigation, but good to have a route definition)
    object Policy : Screen("policy/{documentType}") {
         fun createRoute(documentType: String) = "policy/$documentType"
    }
}
