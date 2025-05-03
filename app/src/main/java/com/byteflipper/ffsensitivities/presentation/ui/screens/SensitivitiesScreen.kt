package com.byteflipper.ffsensitivities.presentation.ui.screens

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ads.InterstitialAdViewModel
import com.byteflipper.ffsensitivities.domain.model.DeviceModel
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import com.byteflipper.ffsensitivities.presentation.ui.components.SliderView
import com.byteflipper.ffsensitivities.presentation.viewmodel.DeviceViewModel
import com.byteflipper.ffsensitivities.utils.AdConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensitivitiesScreen(
    navController: NavController,
    manufacturerArg: String?,
    modelNameArg: String?,
    deviceViewModel: DeviceViewModel = hiltViewModel()
) {
    val manufacturer = remember(manufacturerArg) { manufacturerArg?.let { Uri.decode(it) } ?: "" }
    val modelName = remember(modelNameArg) { modelNameArg?.let { Uri.decode(it) } ?: "" }
    val activity = LocalActivity.current as? Activity
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val interstitialViewModel: InterstitialAdViewModel = viewModel(
        key = "interstitial_sensitivities",
        factory = InterstitialAdViewModel.Factory(
            application = application,
            adUnitId = AdConstants.INTERSTITIAL_SENSITIVITIES_AD_UNIT_ID,
            adFrequency = AdConstants.SENSITIVITIES_SCREEN_AD_FREQUENCY
        )
    )

    val deviceModelState by produceState<UiState<DeviceModel>>(initialValue = UiState.Loading, manufacturer, modelName, deviceViewModel) {
        deviceViewModel.uiState.collect { deviceListState ->
            value = when (deviceListState) {
                is UiState.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val devices = deviceListState.data as? List<DeviceModel>
                    val foundDevice = devices?.find { it.manufacturer == manufacturer && it.name == modelName }
                    if (foundDevice != null) {
                        UiState.Success(foundDevice)
                    } else {
                        UiState.Error("Device not found for $manufacturer $modelName")
                    }
                }
                is UiState.Loading -> UiState.Loading
                is UiState.Error -> UiState.Error(deviceListState.message)
                is UiState.NoInternet -> UiState.NoInternet
            }
        }
    }

    LaunchedEffect(manufacturer) {
        if (manufacturer.isNotEmpty()) {
            deviceViewModel.fetchDevices(manufacturer)
        } else {
             Log.w("SensitivitiesScreen", "Manufacturer is empty, cannot fetch devices.")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$manufacturer $modelName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = deviceModelState) {
            is UiState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(R.string.loading_device_data))
                }
            }

            is UiState.Error -> {
            }

            is UiState.NoInternet -> {
            }

            is UiState.Success<*> -> {
                val deviceModel = state.data as DeviceModel

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = ShapeDefaults.ExtraLarge
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(21.dp)
                        ) {
                            SliderView(
                                label = R.string.review,
                                initialValue = deviceModel.sensitivities?.review?.toFloat() ?: 0f,
                                onValueChange = { },
                                enabled = false
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 1.dp
                            )

                            SliderView(
                                label = R.string.collimator,
                                initialValue = deviceModel.sensitivities?.collimator?.toFloat()
                                    ?: 0f,
                                onValueChange = { },
                                enabled = false
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 1.dp
                            )

                            SliderView(
                                label = R.string.x2_scope,
                                initialValue = deviceModel.sensitivities?.x2_scope?.toFloat() ?: 0f,
                                onValueChange = { },
                                enabled = false
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 1.dp
                            )

                            SliderView(
                                label = R.string.x4_scope,
                                initialValue = deviceModel.sensitivities?.x4_scope?.toFloat() ?: 0f,
                                onValueChange = { },
                                enabled = false
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(0.dp, 8.dp, 0.dp, 16.dp),
                                thickness = 1.dp
                            )

                            Row {
                                if (deviceModel.dpi == null || deviceModel.dpi == 0) {
                                    Text(
                                        text = stringResource(id = R.string.fire_button) + ": " + deviceModel.fire_button,
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Text(
                                        text = stringResource(id = R.string.dpi) + ": " + deviceModel.dpi + " | " + stringResource(
                                            id = R.string.fire_button
                                        ) + ": " + deviceModel.fire_button,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                thickness = 1.dp
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(id = R.string.it_works),
                                    modifier = Modifier.weight(1f)
                                )

                                FilledTonalIconButton(
                                    onClick = { /* Действие для первой иконки */ },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.thumb_up_24px),
                                        contentDescription = stringResource(R.string.favorite_icon_desc)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                FilledTonalIconButton(
                                    onClick = { /* Действие для второй иконки */ },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.thumb_down_24px),
                                        contentDescription = stringResource(R.string.favorite_icon_desc)
                                    )
                                }
                            }


                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                thickness = 1.dp
                            )

                            val clipboardManager = LocalClipboardManager.current

                            val settingsText = buildString {
                                append("${stringResource(id = R.string.dpi)}: ${deviceModel.dpi}\n")
                                append("${stringResource(id = R.string.review)}: ${deviceModel.sensitivities?.review}\n")
                                append("${stringResource(id = R.string.collimator)}: ${deviceModel.sensitivities?.collimator}\n")
                                append("${stringResource(id = R.string.x2_scope)}: ${deviceModel.sensitivities?.x2_scope}\n")
                                append("${stringResource(id = R.string.x4_scope)}: ${deviceModel.sensitivities?.x4_scope}\n")
                                append("${stringResource(id = R.string.sniper_scope)}: ${deviceModel.sensitivities?.sniper_scope}\n")
                                append("${stringResource(id = R.string.free_review)}: ${deviceModel.sensitivities?.free_review}\n")
                                append("${stringResource(id = R.string.source)} ${deviceModel.settings_source_url}")
                            }

                            FilledTonalButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(settingsText))
                                    // Track action and potentially show ad AFTER copying using the manually created ViewModel
                                    activity?.let { act ->
                                        Log.d("SensitivitiesScreen", "Tracking action (copy settings) for potential interstitial ad.")
                                        interstitialViewModel.trackActionAndShowAdIfNeeded(act) // Use the created instance
                                    } ?: Log.w("SensitivitiesScreen", "Activity is null, cannot show interstitial ad.")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(id = R.string.copy),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
