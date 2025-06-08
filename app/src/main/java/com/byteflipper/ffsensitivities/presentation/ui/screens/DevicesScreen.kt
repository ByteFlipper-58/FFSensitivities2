package com.byteflipper.ffsensitivities.presentation.ui.screens

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.viewmodel.UnifiedAdViewModel
import com.byteflipper.ffsensitivities.ads.components.getDynamicBottomPadding
import com.byteflipper.ffsensitivities.domain.model.DeviceModel
import com.byteflipper.ffsensitivities.navigation.Screen
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import com.byteflipper.ffsensitivities.presentation.ui.components.ShimmerLazyItem
import com.byteflipper.ffsensitivities.presentation.ui.components.ErrorStateComponent
import com.byteflipper.ffsensitivities.presentation.ui.components.ErrorType
import com.byteflipper.ffsensitivities.presentation.viewmodel.DeviceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    name: String?,
    model: String?,
    deviceViewModel: DeviceViewModel = hiltViewModel(),
    adViewModel: UnifiedAdViewModel = hiltViewModel()
) {
    val uiState by deviceViewModel.uiState.collectAsState()
    val activity = LocalActivity.current as? Activity

    LaunchedEffect(model) {
        model?.let {
            deviceViewModel.fetchDevices(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(name ?: stringResource(R.string.devices)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                top = 8.dp,
                bottom = getDynamicBottomPadding(AdLocation.DEVICES_SCREEN, adViewModel)
            )
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    items(10, key = { "shimmer_$it" }, contentType = { "shimmer" }) {
                        ShimmerLazyItem()
                    }
                }
                is UiState.Success<*> -> {
                    val devices = state.data as? List<DeviceModel> ?: emptyList()
                    items(
                        count = devices.size,
                        key = { index -> "${devices[index].manufacturer}_${devices[index].name}" },
                        contentType = { "deviceCard" }
                    ) { index ->
                        DevicesCard(
                            devices = devices[index],
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
}

@Composable
fun DevicesCard(
    devices: DeviceModel,
    navController: NavHostController,
    adViewModel: UnifiedAdViewModel,
    activity: Activity?
) {
    OutlinedCard (
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = ShapeDefaults.Large,
        onClick = {
            val encodedManufacturer = Uri.encode(devices.manufacturer)
            val encodedDeviceName = Uri.encode(devices.name)
            
            activity?.let { act ->
                Log.d("DevicesCard", "Трекинг выбора устройства для показа рекламы")
                adViewModel.trackActionAndShowInterstitial(AdLocation.DEVICES_SCREEN, act) { result ->
                    if (result.success) {
                        Log.d("DevicesCard", "Реклама показана после выбора устройства")
                    }
                }
            } ?: Log.w("DevicesCard", "Activity is null, cannot show ad.")
            
            // Навигация к экрану настроек чувствительности
            navController.navigate(
                Screen.Sensitivities(manufacturer = encodedManufacturer, modelName = encodedDeviceName).route
            ) {
                launchSingleTop = true
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = devices.manufacturer + " " + devices.name, textAlign = TextAlign.Center)
        }
    }
}


