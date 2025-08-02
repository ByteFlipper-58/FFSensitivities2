package com.byteflipper.ffsensitivities.presentation.devices

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.ads.viewmodel.SimpleAdViewModel
import com.byteflipper.ffsensitivities.domain.model.DeviceModel
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import com.byteflipper.ffsensitivities.presentation.ui.components.ShimmerLazyItem
import com.byteflipper.ffsensitivities.presentation.ui.components.ErrorStateComponent
import com.byteflipper.ffsensitivities.presentation.ui.components.ErrorType
import com.byteflipper.ffsensitivities.presentation.viewmodel.devices.DevicesScreenViewModel
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.components.getDynamicBottomPadding
import com.byteflipper.ffsensitivities.presentation.devices.components.DeviceCard
import android.app.Activity

@Composable
fun DevicesScreenContent(
    uiState: UiState<List<DeviceModel>>,
    navController: NavHostController,
    adViewModel: SimpleAdViewModel,
    activity: Activity?,
    model: String?,
    deviceViewModel: DevicesScreenViewModel
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 8.dp,
            end = 8.dp,
            top = 8.dp,
            bottom = getDynamicBottomPadding(AdLocation.DEVICES_SCREEN, adViewModel)
        )
    ) {
        when (uiState) {
            is UiState.Loading -> {
                items(10, key = { "shimmer_$it" }, contentType = { "shimmer" }) {
                    ShimmerLazyItem()
                }
            }
            is UiState.Success<*> -> {
                val devices = uiState.data as? List<DeviceModel> ?: emptyList()
                items(
                    items = devices,
                    key = { device -> "${device.manufacturer}_${device.name}" },
                    contentType = { "deviceCard" }
                ) { device ->
                    DeviceCard(
                        device = device,
                        navController = navController,
                        adViewModel = adViewModel,
                        activity = activity
                    )
                }
            }
            is UiState.NoInternet -> {
                item(
                    key = "no_internet",
                    contentType = "error_state"
                ) {
                    ErrorStateComponent(
                        errorType = ErrorType.NO_INTERNET,
                        onRetry = { 
                            model?.let { deviceViewModel.fetchDevices(it) }
                        }
                    )
                }
            }
            is UiState.Error -> {
                item(
                    key = "error",
                    contentType = "error_state"
                ) {
                    ErrorStateComponent(
                        errorType = ErrorType.GENERAL_ERROR,
                        onRetry = { 
                            model?.let { deviceViewModel.fetchDevices(it) }
                        }
                    )
                }
            }
        }
    }
} 