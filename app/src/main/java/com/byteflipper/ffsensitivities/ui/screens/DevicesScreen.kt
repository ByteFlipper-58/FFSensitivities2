package com.byteflipper.ffsensitivities.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.data.DeviceModel
import com.byteflipper.ffsensitivities.repository.DeviceRepository
import com.byteflipper.ffsensitivities.ui.UiState
import com.byteflipper.ffsensitivities.ui.components.ShimmerLazyItem
import com.byteflipper.ffsensitivities.viewmodel.DeviceViewModel
import com.google.gson.Gson
import io.ktor.client.HttpClient
import java.net.URLEncoder

@Composable
fun DevicesScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    model: String?,
    repository: DeviceRepository = DeviceRepository(HttpClient()),
) {
    val viewModel: DeviceViewModel = viewModel(
        factory = DeviceViewModel.DeviceViewModelFactory(repository)
    )
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current as Activity

    LaunchedEffect(model) {
        model?.let {
            viewModel.fetchDevices(it)
        }
    }

    when (val uiState = uiState) {
        is UiState.Loading -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(10) {
                    ShimmerLazyItem()
                }
            }
        } is UiState.Success<*> -> {
            val devices = (uiState as UiState.Success<List<DeviceModel>>).data
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(devices) { device ->
                    DevicesCard(device, navController)
                }
            }
        } is UiState.NoInternet -> {
            NoInternetScreen(viewModel)
        } is UiState.Error -> {
            ErrorScreen(
                errorMessage = (uiState as UiState.Error).message,
                onRetry = { viewModel.retry() },
                onReportBug = {  }
            )
        }
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
            navController.navigate(
                "sensitivities/${devices.manufacturer}/${Uri.encode(devices.name)}/${URLEncoder.encode(Gson().toJson(devices), "UTF-8")}"
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