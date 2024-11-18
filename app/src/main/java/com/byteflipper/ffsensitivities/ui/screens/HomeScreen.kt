package com.byteflipper.ffsensitivities.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.data.Manufacturer
import com.byteflipper.ffsensitivities.ui.UiState
import com.byteflipper.ffsensitivities.ui.components.ShimmerLazyItem
import com.byteflipper.ffsensitivities.viewmodel.ManufacturerViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ManufacturerViewModel = viewModel(),
) {
    val uiState = viewModel.uiState.value

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp, 8.dp, 14.dp, 0.dp),
            shape = ShapeDefaults.Large,
        ) {
            IconWithTextRow()
        }

        when (uiState) {
            is UiState.Loading -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(16) {
                        ShimmerLazyItem()
                    }
                }
            }
            is UiState.Success -> {
                val manufacturers = uiState.data
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(8.dp)
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.message)
                }
            }
        }
    }
}

@Composable
fun NoInternetScreen(viewModel: ManufacturerViewModel = viewModel()) {
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
fun ManufacturerCard(manufacturer: Manufacturer, navController: NavController) {
    OutlinedCard (
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = ShapeDefaults.Large,
        onClick = {
            navController.navigate("devices/${manufacturer.name}/${manufacturer.model}") {
                launchSingleTop = true
                restoreState = true
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
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
fun IconWithTextRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ) {
        val icon: Painter = painterResource(id = R.drawable.ic_launcher_foreground)

        Image(
            painter = icon,
            contentDescription = "App Icon",
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "App Title",
            textAlign = TextAlign.Center,
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
    IconWithTextRow()
}
