package com.byteflipper.ffsensitivities.playcore

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.byteflipper.ffsensitivities.BuildConfig
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

enum class UpdateState {
    CHECKING, AVAILABLE, DOWNLOADING, DOWNLOADED, INSTALLING, INSTALLED, FAILED
}

class AppUpdateManagerWrapper(private val activity: ComponentActivity) : DefaultLifecycleObserver {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private var updateInfo: AppUpdateInfo? = null
    private var updateType: Int = AppUpdateType.FLEXIBLE
    val updateState = mutableStateOf(UpdateState.CHECKING)
    private var updateDialogLauncher: ActivityResultLauncher<Intent>? = null
    private val installStateUpdatedListener = InstallStateUpdatedListener { installState ->
        when (installState.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                updateState.value = UpdateState.DOWNLOADING
            }

            InstallStatus.DOWNLOADED -> {
                updateState.value = UpdateState.DOWNLOADED
                promptToCompleteUpdate()
            }

            InstallStatus.INSTALLING -> {
                updateState.value = UpdateState.INSTALLING
            }

            InstallStatus.INSTALLED -> {
                updateState.value = UpdateState.INSTALLED
            }

            InstallStatus.CANCELED, InstallStatus.FAILED -> {
                updateState.value = UpdateState.FAILED
            }
            else -> {}
        }
    }

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        updateDialogLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                updateState.value = UpdateState.FAILED
            }
        }
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        if (updateState.value == UpdateState.DOWNLOADED) {
            promptToCompleteUpdate()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        appUpdateManager.unregisterListener(installStateUpdatedListener)
        updateDialogLauncher?.unregister()
    }

    suspend fun checkForUpdate() {
        val currentVersionCode = BuildConfig.VERSION_CODE

        withContext(Dispatchers.IO) {
            updateState.value = UpdateState.CHECKING

            updateInfo = suspendCoroutine { cont ->
                appUpdateManager.appUpdateInfo.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        cont.resume(task.result)
                    } else {
                        updateState.value = UpdateState.FAILED
                        cont.resume(null)
                    }
                }
            }
        }.also {
            updateInfo?.let { info ->
                val latestVersionCode = info.availableVersionCode()

                updateType = if (latestVersionCode - currentVersionCode > 3) {
                    AppUpdateType.IMMEDIATE
                } else {
                    AppUpdateType.FLEXIBLE
                }

                when (info.updateAvailability()) {
                    UpdateAvailability.UPDATE_AVAILABLE -> {
                        if (info.isUpdateTypeAllowed(updateType)) {
                            updateState.value = UpdateState.AVAILABLE
                            startUpdate()
                        } else {
                            updateState.value = UpdateState.FAILED
                        }
                    }
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        updateState.value = UpdateState.DOWNLOADING
                    }
                    else -> {
                        updateState.value = UpdateState.INSTALLED
                    }
                }
            } ?: run {
                updateState.value = UpdateState.FAILED
            }
        }
    }

     fun startUpdate() {
        updateInfo?.let { info ->
            try {
                val updateOptions = AppUpdateOptions.newBuilder(updateType).build()
                val updateStarted = appUpdateManager.startUpdateFlowForResult(
                    info,
                    activity,
                    updateOptions,
                    REQUEST_CODE
                )
                if (!updateStarted) {
                    updateState.value = UpdateState.FAILED
                }
            } catch (e: Exception) {
                updateState.value = UpdateState.FAILED
            }
        }
    }

    private fun promptToCompleteUpdate() {
        appUpdateManager.completeUpdate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateState.value = UpdateState.INSTALLED
            } else {
                updateState.value = UpdateState.FAILED
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 100
    }
}
