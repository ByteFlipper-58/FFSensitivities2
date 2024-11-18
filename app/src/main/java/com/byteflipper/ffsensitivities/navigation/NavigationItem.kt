package com.byteflipper.ffsensitivities.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : NavigationItem("home", "Home", Icons.Default.Home)
    object About : NavigationItem("about", "About", Icons.Default.Info)
    object Settings : NavigationItem("settings", "Settings", Icons.Default.Settings)
}