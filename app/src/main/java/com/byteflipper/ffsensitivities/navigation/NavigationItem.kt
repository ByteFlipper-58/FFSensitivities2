package com.byteflipper.ffsensitivities.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.byteflipper.ffsensitivities.R

sealed class NavigationItem(val route: String, val resourceId: Int, val icon: ImageVector) {
    object Home : NavigationItem("home", R.string.home, Icons.Default.Home)
    object About : NavigationItem("about", R.string.about_app, Icons.Default.Info)
    object Settings : NavigationItem("settings", R.string.settings, Icons.Default.Settings)
}