package com.byteflipper.ffsensitivities.presentation.sensitivities

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.activity.compose.LocalActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import android.app.Activity
import com.byteflipper.ffsensitivities.ads.viewmodel.SimpleAdViewModel
import com.byteflipper.ffsensitivities.domain.model.DeviceModel
import com.byteflipper.ffsensitivities.presentation.devices.DevicesScreenViewModel
import com.byteflipper.ffsensitivities.presentation.ui.UiState

@Composable
fun SensitivitiesScreenLayout(
    navController: NavController,
    manufacturerArg: String?,
    modelNameArg: String?,
    deviceViewModel: DevicesScreenViewModel = hiltViewModel(),
    adViewModel: SimpleAdViewModel = hiltViewModel()
) {
    val manufacturer = remember(manufacturerArg) { manufacturerArg?.let { Uri.decode(it) } ?: "" }
    val modelName = remember(modelNameArg) { modelNameArg?.let { Uri.decode(it) } ?: "" }
    val deviceListState by deviceViewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current as? Activity

    val deviceModelState = when (deviceListState) {
        is UiState.Success<*> -> {
            @Suppress("UNCHECKED_CAST")
            val devices = (deviceListState as UiState.Success<List<DeviceModel>>).data
            val foundDevice = devices.find { it.manufacturer == manufacturer && it.name == modelName }
            if (foundDevice != null) {
                UiState.Success(foundDevice)
            } else {
                UiState.Error("Device not found for $manufacturer $modelName")
            }
        }
        is UiState.Loading -> UiState.Loading
        is UiState.Error -> UiState.Error((deviceListState as UiState.Error).message)
        is UiState.NoInternet -> UiState.NoInternet
    }
    val dpiAvailable = (deviceModelState as? UiState.Success<*>)?.data
        ?.let { it as? DeviceModel }
        ?.dpi
        ?.let { it != 0 }
        ?: false
    var isDpiUnlocked by rememberSaveable(manufacturer, modelName) { mutableStateOf(false) }
    var isUnlockingDpi by rememberSaveable(manufacturer, modelName) { mutableStateOf(false) }

    LaunchedEffect(manufacturer) {
        if (manufacturer.isNotEmpty()) {
            deviceViewModel.fetchDevices(manufacturer)
        } else {
             Log.w("SensitivitiesScreen", "Manufacturer is empty, cannot fetch devices.")
        }
    }

    val onNavigateBack: () -> Unit = {
        if (deviceModelState is UiState.Success<*> && activity != null) {
            adViewModel.showInterstitialAfterCompletedSensitivityView(activity) {
                navController.popBackStack()
                Unit
            }
        } else {
            navController.popBackStack()
            Unit
        }
    }

    LaunchedEffect(dpiAvailable) {
        if (dpiAvailable) {
            adViewModel.preloadRewardedAd(com.byteflipper.ffsensitivities.ads.core.AdLocation.EXTRA_SENSITIVITIES)
        }
    }

    SensitivitiesScreenScaffold(
        navController = navController,
        title = "$manufacturer $modelName",
        onNavigateBack = onNavigateBack
    ) {
        SensitivitiesScreenContent(
            deviceModelState = deviceModelState,
            isDpiUnlocked = isDpiUnlocked,
            isUnlockingDpi = isUnlockingDpi,
            onUnlockDpi = {
                activity?.let { currentActivity ->
                    isUnlockingDpi = true
                    adViewModel.showRewardedAd(
                        com.byteflipper.ffsensitivities.ads.core.AdLocation.EXTRA_SENSITIVITIES,
                        currentActivity
                    ) { result ->
                        isUnlockingDpi = false
                        if (result.reward != null) isDpiUnlocked = true
                    }
                }
            }
        )
    }
} 
