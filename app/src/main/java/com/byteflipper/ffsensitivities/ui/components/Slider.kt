package com.byteflipper.ffsensitivities.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

@Composable
fun SliderView(
    label: Any,
    initialValue: Float = 0f,
    onValueChange: (Float) -> Unit,
    enabled: Boolean = true
) {
    var sliderPosition by remember { mutableFloatStateOf(initialValue) }

    Column(
        Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = when (label) {
                    is Int -> stringResource(id = label)
                    is String -> label
                    else -> "Неизвестная метка"
                },
                Modifier.weight(0.8f),
            )

            Text(
                text = sliderPosition.toInt().toString(),
                Modifier.weight(0.2f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }

        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
                onValueChange(it)
            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            valueRange = 0f..200f,
            enabled = enabled
        )
    }
}