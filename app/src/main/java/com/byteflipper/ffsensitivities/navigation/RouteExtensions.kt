package com.byteflipper.ffsensitivities.navigation

import androidx.navigation.NavController

fun NavController.navigateToHome() = navigate(Screen.Home.route)
fun NavController.navigateToAbout() = navigate(Screen.About.route)
fun NavController.navigateToSettings() = navigate(Screen.Settings.route)
fun NavController.navigateToThemeSettings() = navigate(Screen.ThemeSettings.route)
fun NavController.navigateToPrivacySettings() = navigate(Screen.PrivacySettings.route)
fun NavController.navigateToLanguageSettings() = navigate(Screen.LanguageSettings.route)
fun NavController.navigateToBugReport() = navigate(Screen.BugReport.route)
fun NavController.navigateToAdTest() = navigate(Route.AD_TEST)

fun NavController.navigateToDevices(name: String, model: String) =
    navigate(Screen.Devices(name, model).route)

fun NavController.navigateToSensitivities(manufacturer: String, modelName: String) =
    navigate(Screen.Sensitivities(manufacturer, modelName).route)

