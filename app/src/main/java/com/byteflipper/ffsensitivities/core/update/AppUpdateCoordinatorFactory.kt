package com.byteflipper.ffsensitivities.core.update

import androidx.activity.ComponentActivity

interface AppUpdateCoordinatorFactory {
    fun create(activity: ComponentActivity): AppUpdateCoordinator
}
