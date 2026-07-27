package com.byteflipper.ffsensitivities.presentation.devices.components

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byteflipper.ffsensitivities.domain.model.DeviceModel

@Composable
fun DeviceCard(
    device: DeviceModel,
    onOpenSensitivities: (String, String) -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = ShapeDefaults.Large,
        onClick = {
            val encodedManufacturer = Uri.encode(device.manufacturer)
            val encodedDeviceName = Uri.encode(device.name)
            
            onOpenSensitivities(encodedManufacturer, encodedDeviceName)
        }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = device.manufacturer + " " + device.name, 
                textAlign = TextAlign.Center
            )
        }
    }
} 
