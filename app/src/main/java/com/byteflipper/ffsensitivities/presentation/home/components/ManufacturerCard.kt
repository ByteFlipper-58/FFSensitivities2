package com.byteflipper.ffsensitivities.presentation.home.components

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.ads.viewmodel.UnifiedAdViewModel
import com.byteflipper.ffsensitivities.domain.model.Manufacturer
import com.byteflipper.ffsensitivities.navigation.Screen
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import android.app.Activity

@Composable
fun ManufacturerCard(
    manufacturer: Manufacturer, 
    navController: NavHostController,
    adViewModel: UnifiedAdViewModel = hiltViewModel(),
    activity: Activity?
) {
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