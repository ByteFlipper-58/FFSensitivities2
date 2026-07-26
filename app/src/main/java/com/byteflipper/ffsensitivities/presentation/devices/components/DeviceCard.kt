package com.byteflipper.ffsensitivities.presentation.devices.components

import android.app.Activity
import android.net.Uri
import android.util.Log
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
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.viewmodel.SimpleAdViewModel
import com.byteflipper.ffsensitivities.domain.model.DeviceModel

@Composable
fun DeviceCard(
    device: DeviceModel,
    onOpenSensitivities: (String, String) -> Unit,
    adViewModel: SimpleAdViewModel,
    activity: Activity?
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = ShapeDefaults.Large,
        onClick = {
            val encodedManufacturer = Uri.encode(device.manufacturer)
            val encodedDeviceName = Uri.encode(device.name)
            
            activity?.let { act ->
                Log.d("DeviceCard", "Трекинг выбора устройства для показа рекламы")
                adViewModel.trackActionAndShowInterstitial(AdLocation.DEVICES_SCREEN, act) { result ->
                    if (result.success) {
                        Log.d("DeviceCard", "Реклама показана после выбора устройства")
                    }
                }
            } ?: Log.w("DeviceCard", "Activity is null, cannot show ad.")
            
            // Навигация к экрану настроек чувствительности
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
