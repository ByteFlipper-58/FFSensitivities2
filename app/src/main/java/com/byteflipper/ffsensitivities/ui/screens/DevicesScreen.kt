package com.byteflipper.ffsensitivities.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun DevicesScreen(
    modifier: Modifier = Modifier,
    model: String?
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Это экран Devices для модели $model",
        )
    }
}