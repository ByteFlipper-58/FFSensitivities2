package com.byteflipper.ffsensitivities.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Composable function that displays a bottom navigation bar.
 *
 * This function creates a bottom navigation bar with the specified navigation items.
 * It handles navigation between different screens based on the selected item.
 *
 * @param navController The NavHostController used for navigation.
 */
@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // List of navigation items to be displayed in the bottom navigation bar
    val items = listOf(
        NavigationItem.Home,  // Home navigation item
        NavigationItem.About  // About navigation item
    )

    // Get the current back stack entry to determine the selected item
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Build the bottom navigation bar UI
    NavigationBar {
        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any {
                isRouteSelected(it.route, item)
            } ?: false

            NavigationBarItem(
                icon = {
                    Icon(imageVector = item.icon, contentDescription = stringResource(item.resourceId))
                },
                label = { Text(stringResource(item.resourceId)) },
                selected = isSelected,
                onClick = {
                    if (isSelected) {
                        navController.popBackStack(item.route, inclusive = false)
                    } else {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
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

/**
 * Checks if the given navigation item is currently selected, considering nested navigation.
 *
 * This function compares the current route with the route of the navigation item.
 * It also handles special cases for nested routes, such as the "Home" section.
 *
 * @param currentRoute The current route string. Can be null.
 * @param item The navigation item to check.
 * @return True if the navigation item is selected, false otherwise.
 */
private fun isRouteSelected(currentRoute: String?, item: NavigationItem): Boolean {
    if (currentRoute == null) return false

    // Direct match with the item's route
    if (currentRoute == item.route) return true

    // Special case: Check if the "Home" section includes nested routes
    if (item == NavigationItem.Home) {
        return currentRoute.startsWith("devices/") ||  // Matches "devices" nested route
                currentRoute.startsWith("sensitivities/")  // Matches "sensitivities" nested route
    }

    return false
}