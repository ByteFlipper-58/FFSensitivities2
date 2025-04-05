package com.byteflipper.ui_components.onboarding.screens

// Убраны импорты NavController, LocalContext, ActivityResultContracts, Manifest, Build

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingPermissionsScreen(
    paddingValues: PaddingValues,
    title: String,
    description: String,
    grantButtonText: String, // Текст кнопки "Предоставить"
    grantedButtonText: String, // Текст кнопки "Предоставлено"
    isPermissionGranted: Boolean, // Текущий статус разрешения
    onGrantPermissionClick: () -> Unit,
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
            Text(
                text = title, // Используем параметр String
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = description, // Используем параметр String
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = onGrantPermissionClick,
                enabled = !isPermissionGranted, // Кнопка неактивна, если разрешение уже есть
                colors = if (isPermissionGranted) { // Меняем цвет, если неактивна
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                } else {
                    ButtonDefaults.buttonColors() // Стандартные цвета
                }
            ) {
                Text(if (isPermissionGranted) grantedButtonText else grantButtonText) // Меняем текст кнопки
            }
        }
    }
}
