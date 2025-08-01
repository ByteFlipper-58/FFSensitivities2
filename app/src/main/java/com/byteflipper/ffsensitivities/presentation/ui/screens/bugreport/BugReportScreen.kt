package com.byteflipper.ffsensitivities.presentation.ui.screens.bugreport

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.byteflipper.ffsensitivities.presentation.viewmodel.bugreport.BugReportScreenViewModel

@Composable
fun BugReportScreen(
    navController: NavController,
    bugReportViewModel: BugReportScreenViewModel = hiltViewModel()
) {
    BugReportScreenLayout(navController = navController)
} 