package com.byteflipper.ffsensitivities.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.data.Manufacturer
import com.byteflipper.ffsensitivities.getRequestSentStatus
import com.byteflipper.ffsensitivities.navigation.NavigationItem
import com.byteflipper.ffsensitivities.repository.ManufacturerRepository
import com.byteflipper.ffsensitivities.saveRequestSentStatus
import com.byteflipper.ffsensitivities.ui.UiState
import com.byteflipper.ffsensitivities.ui.components.ShimmerLazyItem
import com.byteflipper.ffsensitivities.ui.dialogs.SensitivitiesRequestDialog
import com.byteflipper.ffsensitivities.viewmodel.ManufacturerViewModel
import io.ktor.client.HttpClient

@Composable
fun HomeScreen(
    navController: NavHostController,
    repository: ManufacturerRepository = ManufacturerRepository(HttpClient())
) {
    val context = LocalContext.current
    val viewModel: ManufacturerViewModel = viewModel(
        factory = ManufacturerViewModel.Factory(repository)
    )

    val uiState = viewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var isRequestSent by remember { mutableStateOf(getRequestSentStatus(context)) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp, 8.dp, 14.dp, 0.dp)
                .clickable(enabled = !isRequestSent) {
                    showDialog = true
                },
            shape = ShapeDefaults.Large,
        ) {
            IconWithTextRow(
                text = if (isRequestSent) stringResource(R.string.request_sensitivities_settings_success) else stringResource(R.string.dont_have_your_phone_model)
            )
        }

        if (showDialog) {
            SensitivitiesRequestDialog(
                onDismiss = {
                    showDialog = false
                },
                onRequestSent = {
                    isRequestSent = true
                    showDialog = false
                    saveRequestSentStatus(context, true)
                }
            )
        }

        when (val state = uiState.value) {
            is UiState.Loading -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(10.dp)
                ) {
                    items(14) {
                        ShimmerLazyItem()
                    }
                }
            }
            is UiState.Success<*> -> {
                val manufacturers = state.data as? List<Manufacturer> ?: emptyList()
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(10.dp)
                ) {
                    items(manufacturers) { manufacturer ->
                        ManufacturerCard(manufacturer, navController)
                    }
                }
            }
            is UiState.NoInternet -> {
                NoInternetScreen(viewModel)
            }
            is UiState.Error -> {
                ErrorScreen(
                    errorMessage = state.message,
                    onRetry = { viewModel.retry() },
                    onCheckForUpdates = { },
                    onReportBug = { }
                )
            }
        }
    }
}

@Composable
fun ManufacturerCard(manufacturer: Manufacturer, navController: NavHostController) {
    OutlinedCard (
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = ShapeDefaults.Large,
        onClick = {
            navController.navigate(
                "devices/${manufacturer.name}/${manufacturer.model}"
            ) {
                launchSingleTop = true
            }
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
        val icon: Painter = painterResource(id = R.drawable.ic_launcher_foreground)

        Image(
            painter = icon,
            contentDescription = "App Icon",
            modifier = Modifier.size(64.dp)
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
        text = "App Name"
    )
}
