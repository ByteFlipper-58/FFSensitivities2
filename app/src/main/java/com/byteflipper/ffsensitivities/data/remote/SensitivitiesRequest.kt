package com.byteflipper.ffsensitivities.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SendSensitivitiesRequestMessageToBot {

    private val TAG = "SendSensitivitiesRequest"

    suspend fun sendMessageToGroup(message: String, onSuccess: () -> Unit): String {
        return withContext(Dispatchers.IO) {
            try {
                // Сначала попробуем отправить с тегом settings_request
                Log.i(TAG, "Пробуем отправить с тегом 'settings_request'")
                var result = BugReportApiService.submitBugReport(
                    tag = "settings_request",
                    message = message
                )

                if (result.isSuccess) {
                    Log.i(TAG, "Settings request sent successfully with tag 'settings_request'")
                    onSuccess()
                    return@withContext "Запрос настроек успешно отправлен"
                }

                // Если не получилось, попробуем с тегом other
                Log.w(TAG, "Не удалось отправить с 'settings_request', пробуем 'other'")
                val errorFromSettingsRequest = result.exceptionOrNull()?.message ?: "Неизвестная ошибка"
                
                result = BugReportApiService.submitBugReport(
                    tag = "other",
                    message = "ЗАПРОС НАСТРОЕК:\n\n$message"
                )

                if (result.isSuccess) {
                    Log.i(TAG, "Settings request sent successfully with tag 'other'")
                    onSuccess()
                    "Запрос настроек отправлен (через общий канал)"
                } else {
                    val errorFromOther = result.exceptionOrNull()?.message ?: "Неизвестная ошибка"
                    Log.e(TAG, "Обе попытки неудачны. settings_request: $errorFromSettingsRequest, other: $errorFromOther")
                    "Ошибка отправки запроса настроек: $errorFromOther"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка отправки запроса настроек: ${e.message}")
                "Ошибка отправки запроса настроек: ${e.message}"
            }
        }
    }
}