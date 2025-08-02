package com.byteflipper.ffsensitivities.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.LocalActivity
import android.app.Activity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.ads.viewmodel.SimpleAdViewModel
import com.byteflipper.ffsensitivities.data.repository.ManufacturerRepository
import com.byteflipper.ffsensitivities.presentation.viewmodel.ManufacturerViewModel
import com.byteflipper.ffsensitivities.presentation.viewmodel.home.HomeScreenViewModel
import com.byteflipper.ffsensitivities.presentation.home.dialogs.HomeScreenDialogs
import io.ktor.client.HttpClient

@Composable
fun HomeScreenLayout(
    navController: NavHostController,
    repository: ManufacturerRepository = ManufacturerRepository(HttpClient()),
    homeViewModel: HomeScreenViewModel = hiltViewModel(),
    manufacturerViewModel: ManufacturerViewModel = viewModel(
        factory = ManufacturerViewModel.Factory(repository)
    ),
    adViewModel: SimpleAdViewModel = hiltViewModel()
) {
    val uiState = manufacturerViewModel.uiState.collectAsState()
    val isRequestSent by homeViewModel.isRequestSent.collectAsState()
    val activity = LocalActivity.current as? Activity

    var showDialog by remember { mutableStateOf(false) }

    HomeScreenScaffold(
        navController = navController
    ) {
        HomeScreenContent(
            uiState = uiState.value,
            isRequestSent = isRequestSent,
            showDialog = showDialog,
            onShowDialogChange = { showDialog = it },
            onRequestSent = { homeViewModel.setRequestSent(true) },
            onRetry = { manufacturerViewModel.retry() },
            navController = navController,
            adViewModel = adViewModel,
            activity = activity
        )
    }

    // Диалоги
    HomeScreenDialogs(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        onRequestSent = {
            showDialog = false
            homeViewModel.setRequestSent(true)
            activity?.let { 
                adViewModel.trackActionAndShowInterstitial(
                    com.byteflipper.ffsensitivities.ads.core.AdLocation.HOME_SCREEN, 
                    it
                )
            }
        }
    )
} 