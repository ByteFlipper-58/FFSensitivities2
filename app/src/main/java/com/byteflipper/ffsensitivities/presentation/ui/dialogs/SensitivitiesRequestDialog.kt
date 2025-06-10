package com.byteflipper.ffsensitivities.presentation.ui.dialogs

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.data.remote.SendSensitivitiesRequestMessageToBot
import kotlinx.coroutines.launch

@Composable
fun SensitivitiesRequestDialog(onDismiss: () -> Unit, onRequestSent: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val infoBuffer = StringBuilder().apply {
        append("${context.getString(R.string.request_sensitivities_settings_message)}\n")
        append("-------------------------------------\n")
        append("Model: ${Build.MODEL}\n")
        append("Device: ${Build.DEVICE}\n")
        append("Manufacturer: ${Build.MANUFACTURER}\n")
        append("-------------------------------------\n")
        append("\n${context.getString(R.string.request_sensitivities_settings_message_two)}")
    }

    val requestBuffer = StringBuilder().apply {
        append("Запрос настроек чувствительности\n\n")
        append("Модель: ${Build.MODEL}\n")
        append("Устройство: ${Build.DEVICE}\n")
        append("Производитель: ${Build.MANUFACTURER}\n")
        append("Android: ${Build.VERSION.RELEASE}\n")
        append("SDK: ${Build.VERSION.SDK_INT}\n\n")
        append("Пользователь запрашивает настройки чувствительности для данного устройства.")
    }

    AlertDialog(
        onDismissRequest = if (isLoading) { {} } else onDismiss,
        title = { Text(text = context.getString(R.string.request_sensitivities_settings_title)) },
        text = {
            Text(text = infoBuffer.toString())
        },
        confirmButton = {
            Button(
                enabled = !isLoading,
                onClick = {
                    isLoading = true
                    scope.launch {
                        try {
                            val sendMessageToBot = SendSensitivitiesRequestMessageToBot()
                            val result = sendMessageToBot.sendMessageToGroup(requestBuffer.toString()) {
                                // Callback вызывается при успехе
                            }

                            Log.d("SendMessageResult", result)
                            
                            // Показываем результат пользователю
                            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                            
                            // Если сообщение содержит "успешно", считаем запрос отправленным
                            if (result.contains("успешно", ignoreCase = true)) {
                                onRequestSent()
                            } else {
                                // Логируем ошибку для отладки
                                Log.e("SensitivitiesRequestDialog", "Request failed: $result")
                            }
                            
                        } catch (e: Exception) {
                            Log.e("SensitivitiesRequestDialog", "Error sending request", e)
                            Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                            onDismiss()
                        }
                    }
                }
            ) {
                Text(
                    text = if (isLoading) context.getString(R.string.request_sensitivities_settings_button_sending)
                          else context.getString(R.string.request_sensitivities_settings_button)
                )
            }
        },
        dismissButton = {
            Button(
                enabled = !isLoading,
                onClick = onDismiss
            ) {
                Text(text = context.getString(R.string.request_sensitivities_settings_cancel))
            }
        }
    )
}

@Preview
@Composable
fun PreviewSensitivitiesRequestDialog() {
    SensitivitiesRequestDialog(
        onDismiss = {},
        onRequestSent = {}
    )
}