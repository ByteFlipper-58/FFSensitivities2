package com.byteflipper.ffsensitivities.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.presentation.home.components.IconWithTextRow

@Composable
fun HomeScreenHeader(
    isRequestSent: Boolean,
    onCardClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isRequestSent) {
                onCardClick()
            },
        shape = ShapeDefaults.Large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        IconWithTextRow(
            text = if (isRequestSent) 
                stringResource(R.string.request_sensitivities_settings_success) 
            else 
                stringResource(R.string.dont_have_your_phone_model)
        )
    }
} 