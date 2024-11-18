package com.byteflipper.ffsensitivities.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.data.DeviceModel
import com.byteflipper.ffsensitivities.ui.UiState
import com.byteflipper.ffsensitivities.ui.components.ShimmerLazyItem
import com.byteflipper.ffsensitivities.viewmodel.DeviceViewModel
import com.google.gson.Gson

@Composable
fun DevicesScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    model: String?,
    viewModel: DeviceViewModel = viewModel()
) {
    val uiState by viewModel.uiState

    LaunchedEffect(model) {
        model?.let { viewModel.fetchDevices(it) }
    }

    when (uiState) {
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
        } is UiState.Success -> {
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Отсутствует интернет-соединение")
            }
        } is UiState.Error -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(text = (uiState as UiState.Error).message)
            }
        }
    }
}

@Composable
fun NoInternetScreen(viewModel: DeviceViewModel = viewModel()) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp, 8.dp, 14.dp, 8.dp),
        shape = ShapeDefaults.Large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val icon: Painter = painterResource(id = R.drawable.no_internet)

            Image(
                painter = icon,
                contentDescription = "App Icon",
                modifier = Modifier.size(192.dp)
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Text(text = "Отсутствует интернет-соединение")
            Spacer(modifier = Modifier.padding(8.dp))
            Button(
                onClick = { viewModel.retry() },
            ) {
                Text(text = "Повторить")
            }
        }
    }
}

@Composable
fun DevicesCard(devices: DeviceModel, navController: NavController) {
    OutlinedCard (
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = ShapeDefaults.Large,
        onClick = {
            navController.navigate("sensitivities/${devices.manufacturer}/${devices.name}/${Gson().toJson(devices)}")
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