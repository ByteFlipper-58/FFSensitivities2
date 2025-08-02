package com.byteflipper.ffsensitivities.presentation.about

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.presentation.viewmodel.about.AboutScreenViewModel

@Composable
fun AboutScreen(
    navController: NavHostController,
    aboutViewModel: AboutScreenViewModel = hiltViewModel()
) {
    AboutScreenLayout(
        navController = navController
    )
} 