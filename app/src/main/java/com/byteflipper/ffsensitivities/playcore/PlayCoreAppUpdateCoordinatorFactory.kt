package com.byteflipper.ffsensitivities.playcore

import androidx.activity.ComponentActivity
import com.byteflipper.ffsensitivities.core.update.AppUpdateCoordinator
import com.byteflipper.ffsensitivities.core.update.AppUpdateCoordinatorFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayCoreAppUpdateCoordinatorFactory @Inject constructor() : AppUpdateCoordinatorFactory {
    override fun create(activity: ComponentActivity): AppUpdateCoordinator {
        return AppUpdateManagerWrapper(activity)
    }
}
