package com.byteflipper.ffsensitivities.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.activity.compose.LocalActivity
import android.app.Activity
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ads.viewmodel.UnifiedAdViewModel
import com.byteflipper.ffsensitivities.data.repository.ManufacturerRepository
import com.byteflipper.ffsensitivities.domain.model.Manufacturer
import com.byteflipper.ffsensitivities.navigation.Screen
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import com.byteflipper.ffsensitivities.presentation.ui.components.ShimmerLazyItem
import com.byteflipper.ffsensitivities.presentation.ui.components.ErrorStateComponent
import com.byteflipper.ffsensitivities.presentation.ui.components.ErrorType
import com.byteflipper.ffsensitivities.presentation.ui.dialogs.SensitivitiesRequestDialog
import com.byteflipper.ffsensitivities.presentation.viewmodel.HomeScreenViewModel
import com.byteflipper.ffsensitivities.presentation.viewmodel.ManufacturerViewModel
import com.byteflipper.ffsensitivities.utils.LazyListUtils.shimmerItems
import com.byteflipper.ffsensitivities.utils.LazyListUtils.optimizedItems
import io.ktor.client.HttpClient
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.components.getDynamicBottomPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    repository: ManufacturerRepository = ManufacturerRepository(HttpClient()),
    homeViewModel: HomeScreenViewModel = hiltViewModel(),
    manufacturerViewModel: ManufacturerViewModel = viewModel(
        factory = ManufacturerViewModel.Factory(repository)
    ),
    adViewModel: UnifiedAdViewModel = hiltViewModel()
) {

    val uiState = manufacturerViewModel.uiState.collectAsState()
    val isRequestSent by homeViewModel.isRequestSent.collectAsState()
    val activity = LocalActivity.current as? Activity

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp, 8.dp, 14.dp, 0.dp)
                    .clickable(enabled = !isRequestSent) {
                        showDialog = true
                        // Трекинг открытия диалога запроса
                        activity?.let { 
                            adViewModel.trackActionAndShowInterstitial(AdLocation.HOME_SCREEN, it)
                        }
                    },
                shape = ShapeDefaults.Large,
            ) {
                IconWithTextRow(
                    text = if (isRequestSent) stringResource(R.string.request_sensitivities_settings_success) else stringResource(
                        R.string.dont_have_your_phone_model
                    )
                )
            }

            if (showDialog) {
                SensitivitiesRequestDialog(
                    onDismiss = {
                        showDialog = false
                    },
                    onRequestSent = {
                        showDialog = false
                        homeViewModel.setRequestSent(true)
                        // Трекинг отправки запроса
                        activity?.let { 
                            adViewModel.trackActionAndShowInterstitial(AdLocation.HOME_SCREEN, it)
                        }
                    }
                )
            }

            when (val state = uiState.value) {
                is UiState.Loading -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(
                            start = 10.dp,
                            end = 10.dp,
                            top = 10.dp,
                            bottom = getDynamicBottomPadding(AdLocation.HOME_SCREEN, adViewModel)
                        )
                    ) {
                        shimmerItems(14) {
                            ShimmerLazyItem()
                        }
                    }
                }

                is UiState.Success<*> -> {
                    val manufacturers = state.data as? List<Manufacturer> ?: emptyList()
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(
                            start = 10.dp,
                            end = 10.dp,
                            top = 10.dp,
                            bottom = getDynamicBottomPadding(AdLocation.HOME_SCREEN, adViewModel)
                        )
                    ) {
                        optimizedItems(
                            items = manufacturers,
                            key = { manufacturer -> manufacturer.name },
                            contentType = "manufacturer"
                        ) { manufacturer ->
                            ManufacturerCard(
                                manufacturer = manufacturer, 
                                navController = navController,
                                adViewModel = adViewModel
                            )
                        }
                    }
                }
                
                is UiState.NoInternet -> {
                    ErrorStateComponent(
                        errorType = ErrorType.NO_INTERNET,
                        onRetry = { manufacturerViewModel.retry() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                is UiState.Error -> {
                    ErrorStateComponent(
                        errorType = ErrorType.GENERAL_ERROR,
                        onRetry = { manufacturerViewModel.retry() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun ManufacturerCard(
    manufacturer: Manufacturer, 
    navController: NavHostController,
    adViewModel: UnifiedAdViewModel = hiltViewModel()
) {
    val activity = LocalActivity.current as? Activity
    
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = ShapeDefaults.Large,
        onClick = {
            // Трекинг выбора производителя
            activity?.let { 
                adViewModel.trackActionAndShowInterstitial(AdLocation.HOME_SCREEN, it)
            }
            
            // Навигация к экрану устройств
            navController.navigate(
                Screen.Devices(name = manufacturer.name, model = manufacturer.model).route
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = manufacturer.name,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun IconWithTextRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(16.dp)
    ) {
        val icon: Painter = painterResource(id = R.drawable.logo)

        Image(
            painter = icon,
            contentDescription = stringResource(R.string.app_icon), // Use resource
            modifier = Modifier.size(64.dp)
                .scale(1.5f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            textAlign = TextAlign.Start,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    val navController = rememberNavController()
    HomeScreen(navController = navController)
}

@Preview(showBackground = true)
@Composable
fun PreviewManufacturerCard() {
    val navController = rememberNavController()
    ManufacturerCard(
        manufacturer = Manufacturer(
            showInProductionApp = true,
            isAvailable = false,
            name = "Samsung",
            model = "samsung"
        ),
        navController = navController
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewIconWithTextRow() {
    IconWithTextRow(
        text = stringResource(R.string.app_name) // Use resource
    )
}