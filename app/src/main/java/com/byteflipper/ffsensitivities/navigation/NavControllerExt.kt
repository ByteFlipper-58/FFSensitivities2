package com.byteflipper.ffsensitivities.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/**
 * Avoids duplicate destinations by checking current route before navigating.
 */
fun NavController.navigateSafe(
    route: String,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    if (currentDestination?.route == route) return
    navigate(route, builder)
}
