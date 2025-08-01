package com.byteflipper.ffsensitivities.presentation.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ShapeDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ads.viewmodel.UnifiedAdViewModel
import com.byteflipper.ffsensitivities.domain.model.Manufacturer
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import com.byteflipper.ffsensitivities.presentation.ui.components.ShimmerLazyItem
import com.byteflipper.ffsensitivities.presentation.ui.components.ErrorStateComponent
import com.byteflipper.ffsensitivities.presentation.ui.components.ErrorType
import com.byteflipper.ffsensitivities.presentation.ui.screens.home.components.HomeScreenHeader
import com.byteflipper.ffsensitivities.presentation.ui.screens.home.components.ManufacturerGrid
import com.byteflipper.ffsensitivities.utils.LazyListUtils.shimmerItems
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.components.getDynamicBottomPadding
import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun HomeScreenContent(
    uiState: UiState<List<Manufacturer>>,
    isRequestSent: Boolean,
    showDialog: Boolean,
    onShowDialogChange: (Boolean) -> Unit,
    onRequestSent: () -> Unit,
    onRetry: () -> Unit,
    navController: NavHostController,
    adViewModel: UnifiedAdViewModel,
    activity: Activity?
) {
    // Заголовок с карточкой запроса
    HomeScreenHeader(
        isRequestSent = isRequestSent,
        onCardClick = {
            onShowDialogChange(true)
            activity?.let { 
                adViewModel.trackActionAndShowInterstitial(AdLocation.HOME_SCREEN, it)
            }
        }
    )

    // Основной контент в зависимости от состояния
    when (uiState) {
        is UiState.Loading -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 10.dp,
                    end = 10.dp,
                    top = 10.dp,
                    bottom = getDynamicBottomPadding(AdLocation.HOME_SCREEN, adViewModel)
                )
            ) {
                shimmerItems(14) {
                    ShimmerLazyItem()
                }
            }
        }

        is UiState.Success<*> -> {
            val manufacturers = uiState.data as? List<Manufacturer> ?: emptyList()
            ManufacturerGrid(
                manufacturers = manufacturers,
                navController = navController,
                adViewModel = adViewModel,
                activity = activity
            )
        }
        
        is UiState.NoInternet -> {
            ErrorStateComponent(
                errorType = ErrorType.NO_INTERNET,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        is UiState.Error -> {
            ErrorStateComponent(
                errorType = ErrorType.GENERAL_ERROR,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
} 