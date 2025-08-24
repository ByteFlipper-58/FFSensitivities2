package com.byteflipper.ffsensitivities.presentation.home

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.presentation.viewmodel.ManufacturerViewModel
import com.byteflipper.ffsensitivities.presentation.viewmodel.home.HomeScreenViewModel

@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeScreenViewModel = hiltViewModel(),
    manufacturerViewModel: ManufacturerViewModel = hiltViewModel()
) {
    HomeScreenLayout(
        navController = navController,
        homeViewModel = homeViewModel,
        manufacturerViewModel = manufacturerViewModel
    )
} 