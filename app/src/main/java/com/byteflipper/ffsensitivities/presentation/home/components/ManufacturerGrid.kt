package com.byteflipper.ffsensitivities.presentation.home.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.ads.viewmodel.SimpleAdViewModel
import com.byteflipper.ffsensitivities.domain.model.Manufacturer
import com.byteflipper.ffsensitivities.utils.LazyListUtils.optimizedItems
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.components.getDynamicBottomPadding
import android.app.Activity
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid

@Composable
fun ManufacturerGrid(
    manufacturers: List<Manufacturer>,
    navController: NavHostController,
    adViewModel: SimpleAdViewModel,
    activity: Activity?,
    header: (@Composable () -> Unit)? = null
) {
    val topPadding = if (header != null) 0.dp else 16.dp

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = topPadding,
            bottom = getDynamicBottomPadding(AdLocation.HOME_SCREEN, adViewModel)
        )
    ) {
        if (header != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    header()
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.padding(top = 16.dp))
            }
        }
        optimizedItems(
            items = manufacturers,
            key = { manufacturer -> manufacturer.name },
            contentType = "manufacturer"
        ) { manufacturer ->
            ManufacturerCard(
                manufacturer = manufacturer, 
                navController = navController,
                adViewModel = adViewModel,
                activity = activity
            )
        }
    }
} 