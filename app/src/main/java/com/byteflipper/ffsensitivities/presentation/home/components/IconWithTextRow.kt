package com.byteflipper.ffsensitivities.presentation.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byteflipper.ffsensitivities.R

@Composable
fun IconWithTextRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(16.dp)
    ) {
        val icon: Painter = painterResource(id = R.drawable.logo)

        Image(
            painter = icon,
            contentDescription = stringResource(R.string.app_icon),
            modifier = Modifier.size(64.dp)
                .scale(1.5f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            textAlign = TextAlign.Start,
        )
    }
} 