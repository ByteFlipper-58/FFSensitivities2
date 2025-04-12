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
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ads.AdManagerHolder
import com.byteflipper.ffsensitivities.domain.model.DeviceModel
import com.byteflipper.ffsensitivities.navigation.Screen
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import com.byteflipper.ffsensitivities.presentation.ui.components.ShimmerLazyItem
import com.byteflipper.ffsensitivities.presentation.viewmodel.DeviceViewModel
import com.byteflipper.ffsensitivities.utils.AdConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    name: String?,
    model: String?,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val viewModel: DeviceViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val activity = LocalActivity.current as Activity
    val visitCountState by appViewModel.visitCount.collectAsState()

    LaunchedEffect(Unit) {
        val currentVisitCount = visitCountState
        val newVisitCount = currentVisitCount + 1
        appViewModel.setVisitCount(newVisitCount)

        if (newVisitCount % AdConstants.DEVICES_SCREEN_AD_FREQUENCY == 0) { // Use constant
            AdManagerHolder.showInterstitialAd(
                activity = activity,
                onShown = {
                    Log.d("DevicesScreen", "Interstitial Ad shown via AdManagerHolder.")
                    appViewModel.setVisitCount(0)
                },
                onDismissed = {
                    Log.d("DevicesScreen", "Interstitial Ad dismissed via AdManagerHolder.")
                }
            )
        }
    }

    LaunchedEffect(model) {
        model?.let {
            viewModel.fetchDevices(it)
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
            contentPadding = PaddingValues(8.dp)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    items(10, key = { "shimmer_$it" }, contentType = { "shimmer" }) {
                        ShimmerLazyItem()
                    }
                }
                is UiState.Success<*> -> {
                    val devices = state.data as? List<DeviceModel> ?: emptyList() // Safe cast
                    items(
                        count = devices.size,
                        key = { index -> "${devices[index].manufacturer}_${devices[index].name}" },
                        contentType = { "deviceCard" } // Use content type
                    ) { index ->
                        DevicesCard(devices[index], navController)
                    }
                }
                is UiState.NoInternet -> {
                }
                is UiState.Error -> {
                    // Optionally display an error message
                }
            }
        }
    }
}

// Placeholder composables (add these or similar implementations if needed)
@Composable
fun NoInternetConnection(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.no_internet_connection))
    }
}

@Composable
fun ErrorMessage(message: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.error_prefix) + (message ?: stringResource(R.string.unknown_error)))
    }
}

@Composable
fun DevicesCard(devices: DeviceModel, navController: NavHostController) {
    OutlinedCard (
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = ShapeDefaults.Large,
        onClick = {
            val encodedManufacturer = Uri.encode(devices.manufacturer)
            val encodedDeviceName = Uri.encode(devices.name)
            // Navigate using the Screen object
            navController.navigate(
                Screen.Sensitivities(manufacturer = encodedManufacturer, modelName = encodedDeviceName).route
            ) {
                launchSingleTop = true // Keep the launchSingleTop behavior
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
