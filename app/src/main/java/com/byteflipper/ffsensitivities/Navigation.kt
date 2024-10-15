package com.byteflipper.ffsensitivities.navigation

import HomeScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.byteflipper.ffsensitivities.ui.screens.AboutScreen
import com.byteflipper.ffsensitivities.ui.screens.DevicesScreen
import com.byteflipper.ffsensitivities.ui.screens.SensitivitiesScreen
import com.byteflipper.ffsensitivities.ui.screens.SettingsScreen

sealed class NavigationItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : NavigationItem("home", "Home", Icons.Default.Home)
    object About : NavigationItem("about", "About", Icons.Default.Info)
    object Settings : NavigationItem("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun NavigationHost(
    navController: NavHostController,
    modifier: Modifier,
    onTitleChange: (String) -> Unit
) {
    NavHost(navController, startDestination = NavigationItem.Home.route, modifier = modifier) {
        composable(NavigationItem.Home.route) {
            onTitleChange("Домашний экран")
            HomeScreen(modifier = modifier, navController)
        }
        composable(NavigationItem.About.route) {
            onTitleChange("О приложении")
            AboutScreen(modifier = modifier)
        }
        composable(NavigationItem.Settings.route) {
            onTitleChange("Настройки")
            SettingsScreen(modifier = modifier)
        }
        composable("devices/{model}") { backStackEntry ->
            val model = backStackEntry.arguments?.getString("model")
            onTitleChange("Устройства $model")
            DevicesScreen(modifier = modifier, navController, model = model ?: "")
        }
        composable("sensitivities/{manufacturer}/{model}/{device}") { backStackEntry ->
            val manufacturer = backStackEntry.arguments?.getString("manufacturer")
            val model = backStackEntry.arguments?.getString("model")
            val device = backStackEntry.arguments?.getString("device")
            onTitleChange("$manufacturer" + " " + "$model")
            SensitivitiesScreen(navController, device)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.About
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        restoreState = true

                        if (currentDestination?.route != item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                        }
                    }
                }
            )
        }
    }
}