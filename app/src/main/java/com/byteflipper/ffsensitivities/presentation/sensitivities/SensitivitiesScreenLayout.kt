package com.byteflipper.ffsensitivities.presentation.sensitivities

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
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

    LaunchedEffect(manufacturer) {
        if (manufacturer.isNotEmpty()) {
            deviceViewModel.fetchDevices(manufacturer)
        } else {
             Log.w("SensitivitiesScreen", "Manufacturer is empty, cannot fetch devices.")
        }
    }

    SensitivitiesScreenScaffold(
        navController = navController,
        title = "$manufacturer $modelName"
    ) {
        SensitivitiesScreenContent(
            deviceModelState = deviceModelState,
            adViewModel = adViewModel
        )
    }
} 
