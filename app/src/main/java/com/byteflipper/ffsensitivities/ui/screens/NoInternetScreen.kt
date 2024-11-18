package com.byteflipper.ffsensitivities.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.viewmodel.DeviceViewModel
import com.byteflipper.ffsensitivities.viewmodel.ManufacturerViewModel

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