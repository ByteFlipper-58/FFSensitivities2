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
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.byteflipper.ffsensitivities.navigation.navigateSafe

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

    NavigationBar(modifier = modifier) {
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
                    if (item == NavigationItem.Home && isSelected) {
                        navController.popBackStack(NavigationItem.Home.route, inclusive = false, saveState = false)
                    } else {
                        navController.navigateSafe(item.route) {
                            launchSingleTop = true
                            popUpTo(NavigationItem.Home.route) { saveState = true }
                            restoreState = true
                        }
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
