package com.byteflipper.ui_components.onboarding.screens // Новый пакет

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter // Импорт Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun OnboardingInfoScreen(
    navController: NavController, // NavController может быть не нужен здесь, если экран не навигирует сам
    paddingValues: PaddingValues,
    logoPainter: Painter, // Параметр для изображения
    title: String, // Параметр для заголовка
    description: String, // Параметр для описания
    imageContentDescription: String?, // Параметр для описания изображения (может быть null)
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues) // Применяем padding от родительского Scaffold
            .padding(horizontal = 16.dp), // Сохраняем горизонтальный padding
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
         Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center, // Центрируем контент вертикально
            modifier = Modifier.weight(1f) // Занимает доступное пространство
        ) {
            Image(
                painter = logoPainter, // Используем параметр Painter
                contentDescription = imageContentDescription, // Используем параметр
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 32.dp)
            )
            Text(
                text = title, // Используем параметр String
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = description, // Используем параметр String
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}
