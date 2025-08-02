package com.byteflipper.ffsensitivities.presentation.home

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.data.repository.ManufacturerRepository
import com.byteflipper.ffsensitivities.presentation.viewmodel.ManufacturerViewModel
import com.byteflipper.ffsensitivities.presentation.viewmodel.home.HomeScreenViewModel
import io.ktor.client.HttpClient

@Composable
fun HomeScreen(
    navController: NavHostController,
    repository: ManufacturerRepository = ManufacturerRepository(HttpClient()),
    homeViewModel: HomeScreenViewModel = hiltViewModel(),
    manufacturerViewModel: ManufacturerViewModel = viewModel(
        factory = ManufacturerViewModel.Factory(repository)
    )
) {
    HomeScreenLayout(
        navController = navController,
        repository = repository,
        homeViewModel = homeViewModel,
        manufacturerViewModel = manufacturerViewModel
    )
} 