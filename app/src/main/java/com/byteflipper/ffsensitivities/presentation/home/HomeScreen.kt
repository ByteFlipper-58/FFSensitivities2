package com.byteflipper.ffsensitivities.presentation.home

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.navigation.navigateToDevices
import com.byteflipper.ffsensitivities.navigation.navigateToSettings

@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeScreenViewModel = hiltViewModel(),
    manufacturerViewModel: ManufacturerViewModel = hiltViewModel()
) {
    HomeScreenLayout(
        onOpenSettings = navController::navigateToSettings,
        onOpenDevices = navController::navigateToDevices,
        homeViewModel = homeViewModel,
        manufacturerViewModel = manufacturerViewModel
    )
} 
