package com.byteflipper.ffsensitivities.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SensitivitiesScreen (
    modifier: Modifier = Modifier,
    model: String?
) {
    Column(
    modifier = Modifier.fillMaxSize()
        .padding(8.dp)
    ) {
        Text(
            text = "Это экран Devices для модели $model",
        )

        SliderView()
        SliderView()
        SliderView()
    }
}

@Preview(showBackground = true)
@Composable
fun SliderView(
    value: Int? = null,
) {
    var sliderPosition by remember { mutableFloatStateOf(0f) }

    Column(
        Modifier.fillMaxWidth()
    ) {
        Row() { 
            Text(
                text = "Text",
                Modifier.weight(.8f),
            )

            Text(
                text = sliderPosition.toInt().toString(),
                Modifier.weight(.2f)
            )
        }

        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            valueRange = 0f..200f
        )
    }
}