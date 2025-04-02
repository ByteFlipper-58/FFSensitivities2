package com.byteflipper.ffsensitivities.presentation.ui.screens

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ads.AdManagerHolder
import com.byteflipper.ffsensitivities.data.repository.DeviceRepository
import com.byteflipper.ffsensitivities.domain.model.DeviceModel
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import com.byteflipper.ffsensitivities.presentation.ui.components.ShimmerLazyItem
import com.byteflipper.ffsensitivities.presentation.viewmodel.DeviceViewModel
import com.google.gson.Gson
import io.ktor.client.HttpClient
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class) // Add OptIn for TopAppBar
@Composable
fun DevicesScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    name: String?, // Add name parameter
    model: String?,
    repository: DeviceRepository = DeviceRepository(HttpClient()),
) {
    val viewModel: DeviceViewModel = viewModel(
        factory = DeviceViewModel.DeviceViewModelFactory(repository)
    )
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val activity = context as Activity

    LaunchedEffect(Unit) {
        val visitCountt = context.incrementVisitCountt()
        if (visitCountt % 10 == 0) {
            AdManagerHolder.showInterstitialAd(
                activity = activity,
                onShown = {
                    Log.d("DevicesScreen", "Interstitial Ad shown via AdManagerHolder.")
                    context.resetVisitCountt()
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
                title = { Text(name ?: stringResource(R.string.devices)) }, // Use name for title
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is UiState.Loading -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(10) {
                        ShimmerLazyItem()
                    }
                }
            }
            is UiState.Success<*> -> {
                val devices = state.data as List<DeviceModel> // Явное приведение типа
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(devices.size) { index ->
                        DevicesCard(devices[index], navController)
                    }
                }
            }
            is UiState.NoInternet -> {
            }
            is UiState.Error -> {
            }
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

fun Context.getVisitCountt(): Int {
    val preferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return preferences.getInt("visit_countt", 0)
}

fun Context.incrementVisitCountt(): Int {
    val preferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val newCount = getVisitCountt() + 1
    preferences.edit().putInt("visit_countt", newCount).apply()
    return newCount
}

fun Context.resetVisitCountt() {
    val preferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    preferences.edit().putInt("visit_countt", 0).apply()
}
