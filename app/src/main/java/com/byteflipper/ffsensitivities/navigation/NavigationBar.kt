package com.byteflipper.ffsensitivities.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource // Import painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Отображает нижнюю панель навигации приложения.
 *
 * Эта панель содержит основные разделы приложения (например, "Главная", "О приложении")
 * и управляет навигацией между ними с использованием [NavHostController].
 * Обрабатывает выбор элементов, подсветку активного элемента (включая вложенные экраны)
 * и специфическую логику навигации для возврата на предыдущие экраны или
 * к корневому экрану вкладки.
 *
 * @param navController Контроллер навигации [NavHostController] для управления переходами.
 * @param modifier Модификатор Compose для настройки внешнего вида панели.
 */
@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.About
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any {
                isRouteSelected(it.route, item)
            } ?: false

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = if (isSelected) item.selectedIcon else item.unselectedIcon), // Use painterResource
                        contentDescription = stringResource(item.resourceId)
                    )
                },
                label = { Text(stringResource(item.resourceId)) },
                selected = isSelected,
                onClick = {
                    val currentBackStackEntry = navController.currentBackStackEntry
                    val currentDestination = currentBackStackEntry?.destination
                    val startDestinationId = navController.graph.findStartDestination().id

                    if (item == NavigationItem.Home && currentDestination?.route == NavigationItem.About.route) {
                        // Get the entry *before* About
                        val previousBackStackEntry = navController.previousBackStackEntry
                        val previousRoute = previousBackStackEntry?.destination?.route
                        // Check if the screen before About was part of the Home hierarchy (Devices/Sens)
                        if (previousRoute != null && (previousRoute.startsWith("devices/") || previousRoute.startsWith("sensitivities/") || previousRoute == NavigationItem.Home.route)) {
                            // If yes, just pop About to return to Devices/Sens (like system back)
                            navController.popBackStack()
                        } else {
                            // Otherwise (e.g., came from root Home or somewhere else), use standard Home navigation
                            navController.navigate(NavigationItem.Home.route) {
                                launchSingleTop = true
                                popUpTo(startDestinationId) { saveState = true }
                                restoreState = true
                            }
                        }
                        return@NavigationBarItem
                    }

                    if (item == NavigationItem.About) {
                        if (currentDestination?.route != NavigationItem.About.route) {
                            navController.navigate(NavigationItem.About.route) {
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                        return@NavigationBarItem
                    }

                    // --- Standard Home click behavior (not from About) ---
                    if (item == NavigationItem.Home) {
                        if (!isSelected) {
                             navController.navigate(NavigationItem.Home.route) {
                                 launchSingleTop = true
                                 popUpTo(startDestinationId) { saveState = true }
                                 restoreState = true
                             }
                        } else {
                             // If not already on the root Home screen (e.g., on Devices/Sens), pop back to the root Home screen.
                             if (currentDestination?.route != NavigationItem.Home.route) {
                                 navController.popBackStack(NavigationItem.Home.route, inclusive = false, saveState = false)
                             }
                        }
                        return@NavigationBarItem
                    }
                }
            )
        }
    }
}

/**
 * Проверяет, выбран ли данный элемент навигации [item] в текущем маршруте [currentRoute],
 * учитывая вложенные маршруты.
 *
 * Сравнивает [currentRoute] с маршрутом [item.route].
 * Особый случай: для элемента [NavigationItem.Home] проверяет, начинается ли
 * [currentRoute] с префиксов "devices/" или "sensitivities/", чтобы считать
 * вложенные экраны частью раздела "Home".
 *
 * @param currentRoute Текущий маршрут навигации (может быть null).
 * @param item Элемент навигации [NavigationItem] для проверки.
 * @return `true`, если элемент [item] считается выбранным для данного [currentRoute], иначе `false`.
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