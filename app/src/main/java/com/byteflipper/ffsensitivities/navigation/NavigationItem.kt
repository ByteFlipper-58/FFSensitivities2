package com.byteflipper.ffsensitivities.navigation

import androidx.annotation.DrawableRes
import com.byteflipper.ffsensitivities.R

sealed class NavigationItem(
    val route: String,
    val resourceId: Int,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int
) {
    object Home : NavigationItem(
        route = "home",
        resourceId = R.string.home,
        selectedIcon = R.drawable.home_filled_24px,
        unselectedIcon = R.drawable.home_24px
    )
    object About : NavigationItem(
        route = "about",
        resourceId = R.string.about_app,
        selectedIcon = R.drawable.info_filled_24px,
        unselectedIcon = R.drawable.info_24px
    )

    object Settings : NavigationItem(
        route = "settings",
        resourceId = R.string.settings,
        selectedIcon = R.drawable.settings_24px,
        unselectedIcon = R.drawable.settings_24px
    )
}
