package com.byteflipper.ffsensitivities.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.ffsensitivities.presentation.home.dialogs.HomeScreenDialogs

@Composable
fun HomeScreenLayout(
    onOpenSettings: () -> Unit,
    onOpenDevices: (String, String) -> Unit,
    homeViewModel: HomeScreenViewModel = hiltViewModel(),
    manufacturerViewModel: ManufacturerViewModel = hiltViewModel()
) {
    val uiState = manufacturerViewModel.uiState.collectAsStateWithLifecycle()
    val isRequestSent by homeViewModel.isRequestSent.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(false) }

    HomeScreenScaffold(
        onOpenSettings = onOpenSettings
    ) {
        HomeScreenContent(
            uiState = uiState.value,
            isRequestSent = isRequestSent,
            showDialog = showDialog,
            onShowDialogChange = { showDialog = it },
            onRetry = { manufacturerViewModel.retry() },
            onOpenDevices = onOpenDevices
        )
    }

    // Диалоги
    HomeScreenDialogs(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        onRequestSent = {
            showDialog = false
            homeViewModel.setRequestSent(true)
        }
    )
} 
