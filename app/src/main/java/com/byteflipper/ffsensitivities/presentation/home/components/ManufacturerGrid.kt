package com.byteflipper.ffsensitivities.presentation.home.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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

@Composable
fun ManufacturerGrid(
    manufacturers: List<Manufacturer>,
    navController: NavHostController,
    adViewModel: SimpleAdViewModel,
    activity: Activity?
) {
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