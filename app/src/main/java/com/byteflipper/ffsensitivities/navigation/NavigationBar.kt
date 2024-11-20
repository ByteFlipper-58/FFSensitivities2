package com.byteflipper.ffsensitivities.navigation

import android.util.Log
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(
    navController: NavHostController
) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.About
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar() {
        items.forEach { item ->
            val isSelected = isRouteSelected(currentDestination?.route, item.route)
            Log.d("BottomNavigationBar", "currentRoute: ${currentDestination?.route}, routePattern: ${item.route}, isSelected: $isSelected")

            NavigationBarItem(
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.label)
                },
                label = { Text(item.label) },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                    }
                }
            )
        }
    }
}

/**
 * Проверка маршрута, учитывающая динамические параметры
 */
fun isRouteSelected(currentRoute: String?, routePattern: String): Boolean {
    if (currentRoute == null) return false

    // Проверка на точное совпадение
    if (currentRoute == routePattern) return true

    // Разбиваем маршруты на части
    val routeParts = routePattern.split("/")
    val currentParts = currentRoute.split("/")

    // Если размеры частей не совпадают, это разные маршруты
    if (routeParts.size != currentParts.size) return false

    // Проверяем каждую часть маршрута
    return routeParts.zip(currentParts).all { (patternPart, currentPart) ->
        patternPart.startsWith("{") || patternPart == currentPart
    }
}