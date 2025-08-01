package com.byteflipper.ffsensitivities.presentation.ui.screens.adtest

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.viewmodel.UnifiedAdViewModel

@Composable
fun AdTestScreenLayout(
    navController: NavController,
    adViewModel: UnifiedAdViewModel = hiltViewModel()
) {
    val activity = LocalActivity.current as? Activity
    val adReadyState by adViewModel.adReadyState.collectAsState()
    val lastAdResult by adViewModel.lastAdResult.collectAsState()

    AdTestScreenScaffold(
        navController = navController
    ) {
        AdTestScreenContent(
            adViewModel = adViewModel,
            activity = activity,
            adReadyState = adReadyState,
            lastAdResult = lastAdResult
        )
    }
} 