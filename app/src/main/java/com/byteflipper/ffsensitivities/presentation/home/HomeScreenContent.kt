package com.byteflipper.ffsensitivities.presentation.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.ads.viewmodel.SimpleAdViewModel
import com.byteflipper.ffsensitivities.domain.model.Manufacturer
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import com.byteflipper.ffsensitivities.presentation.ui.components.ShimmerLazyItem
import com.byteflipper.ffsensitivities.presentation.ui.components.ErrorStateComponent
import com.byteflipper.ffsensitivities.presentation.ui.components.ErrorType
import com.byteflipper.ffsensitivities.presentation.home.components.HomeScreenHeader
import com.byteflipper.ffsensitivities.presentation.home.components.ManufacturerGrid
import com.byteflipper.ffsensitivities.utils.LazyListUtils.shimmerItems
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.components.getDynamicBottomPadding
import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

@Composable
fun HomeScreenContent(
    uiState: UiState<List<Manufacturer>>,
    isRequestSent: Boolean,
    showDialog: Boolean,
    onShowDialogChange: (Boolean) -> Unit,
    onRequestSent: () -> Unit,
    onRetry: () -> Unit,
    navController: NavHostController,
    adViewModel: SimpleAdViewModel,
    activity: Activity?
) {
    // Основной контент в зависимости от состояния
    when (uiState) {
        is UiState.Loading -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 10.dp,
                    end = 10.dp,
                    top = 0.dp,
                    bottom = getDynamicBottomPadding(AdLocation.HOME_SCREEN, adViewModel)
                )
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        HomeScreenHeader(
                            isRequestSent = isRequestSent,
                            onCardClick = {
                                onShowDialogChange(true)
                                activity?.let { 
                                    adViewModel.trackActionAndShowInterstitial(AdLocation.HOME_SCREEN, it)
                                }
                            }
                        )
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.padding(top = 16.dp))
                }
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
                activity = activity,
                header = {
                    HomeScreenHeader(
                        isRequestSent = isRequestSent,
                        onCardClick = {
                            onShowDialogChange(true)
                            activity?.let { 
                                adViewModel.trackActionAndShowInterstitial(AdLocation.HOME_SCREEN, it)
                            }
                        }
                    )
                }
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