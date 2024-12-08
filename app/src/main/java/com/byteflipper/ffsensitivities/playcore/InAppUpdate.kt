package com.byteflipper.ffsensitivities.playcore

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.tasks.await

class InAppUpdateManager(
    private val context: Context,
    private val updateStrategy: UpdateStrategy = UpdateStrategy.AUTO,
    private val updateResultLauncher: ActivityResultLauncher<IntentSenderRequest>
) {
    private val appUpdateManager = AppUpdateManagerFactory.create(context)
    private val currentVersionCode = getCurrentVersionCode(context)
    val updateState: MutableState<UpdateState> = mutableStateOf(UpdateState.Idle)

    enum class UpdateStrategy {
        AUTO, FLEXIBLE, IMMEDIATE
    }

    sealed class UpdateState {
        object Idle : UpdateState()
        object Checking : UpdateState()
        object Downloading : UpdateState()
        object Downloaded : UpdateState()
        data class Error(val message: String) : UpdateState()
    }

    private fun getCurrentVersionCode(context: Context): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)
            packageInfo.longVersionCode
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Не удалось получить версию приложения", e)
            0L
        }
    }

    private suspend fun fetchAppUpdateInfo() = try {
        appUpdateManager.appUpdateInfo.await()
    } catch (e: Exception) {
        Log.e(TAG, "Ошибка получения информации об обновлении", e)
        null
    }

    private fun shouldStartUpdate(
        appUpdateInfo: com.google.android.play.core.appupdate.AppUpdateInfo,
        updateType: Int
    ) = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
            appUpdateInfo.isUpdateTypeAllowed(updateType)

    private fun determineUpdateType(availableVersionCode: Long): Int {
        return when (updateStrategy) {
            UpdateStrategy.FLEXIBLE -> AppUpdateType.FLEXIBLE
            UpdateStrategy.IMMEDIATE -> AppUpdateType.IMMEDIATE
            UpdateStrategy.AUTO -> {
                val versionDifference = availableVersionCode - currentVersionCode
                when {
                    versionDifference > 3 -> AppUpdateType.IMMEDIATE
                    else -> AppUpdateType.FLEXIBLE
                }
            }
        }
    }

    @Composable
    fun CheckForUpdates() {
        LaunchedEffect(Unit) {
            updateState.value = UpdateState.Checking
            val appUpdateInfo = fetchAppUpdateInfo()

            when {
                appUpdateInfo != null -> {
                    val updateType = determineUpdateType(appUpdateInfo.availableVersionCode().toLong())
                    if (shouldStartUpdate(appUpdateInfo, updateType)) {
                        startUpdate(appUpdateInfo, updateType)
                    } else {
                        updateState.value = UpdateState.Idle
                    }
                }
                else -> {
                    updateState.value = UpdateState.Error("Не удалось получить информацию об обновлении")
                }
            }
        }
    }

    private fun startUpdate(
        appUpdateInfo: com.google.android.play.core.appupdate.AppUpdateInfo,
        updateType: Int
    ) {
        try {
            val updateOptions = AppUpdateOptions.newBuilder(updateType).build()
            val activity = context as? androidx.activity.ComponentActivity
                ?: throw IllegalStateException("Context must be an Activity")

            val updateStarted = appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                activity,
                updateOptions,
                UPDATE_REQUEST_CODE
            )

            if (updateStarted) {
                updateState.value = UpdateState.Downloading
            } else {
                updateState.value = UpdateState.Error("Update flow could not be started")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при запуске обновления", e)
            updateState.value = UpdateState.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    fun completeUpdate() {
        appUpdateManager.completeUpdate()
        updateState.value = UpdateState.Downloaded
    }

    companion object {
        private const val TAG = "InAppUpdateManager"
        const val UPDATE_REQUEST_CODE = 100
    }
}