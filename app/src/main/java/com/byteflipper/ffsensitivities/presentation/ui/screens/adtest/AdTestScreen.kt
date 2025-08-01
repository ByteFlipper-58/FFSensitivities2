package com.byteflipper.ffsensitivities.presentation.ui.screens.adtest

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.byteflipper.ffsensitivities.presentation.viewmodel.adtest.AdTestScreenViewModel

@Composable
fun AdTestScreen(
    navController: NavController,
    adTestViewModel: AdTestScreenViewModel = hiltViewModel()
) {
    AdTestScreenLayout(
        navController = navController
    )
} 