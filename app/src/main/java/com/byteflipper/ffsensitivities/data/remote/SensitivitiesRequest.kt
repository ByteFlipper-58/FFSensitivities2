package com.byteflipper.ffsensitivities.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SendSensitivitiesRequestMessageToBot {

    private val TAG = "SendSensitivitiesRequest"

    suspend fun sendMessageToGroup(message: String, onSuccess: () -> Unit): String {
        return withContext(Dispatchers.IO) {
            try {
                val apiClient = BugReportApiClient()
                
                Log.i(TAG, "Пробуем отправить с тегом 'settings_request'")
                val settingsResult = apiClient.sendBugReport("settings_request", message)
                
                when (settingsResult) {
                    is ApiResult.Success -> {
                        Log.i(TAG, "Settings request sent successfully with tag 'settings_request'")
                        onSuccess()
                        return@withContext "Запрос настроек успешно отправлен"
                    }
                    is ApiResult.Error -> {
                        Log.w(TAG, "Не удалось отправить с 'settings_request': ${settingsResult.message}")
                        
                        // Если не получилось, попробуем с тегом other
                        Log.i(TAG, "Пробуем отправить с тегом 'other'")
                        val otherResult = apiClient.sendBugReport("other", "ЗАПРОС НАСТРОЕК:\n\n$message")
                        
                        when (otherResult) {
                            is ApiResult.Success -> {
                                Log.i(TAG, "Settings request sent successfully with tag 'other'")
                                onSuccess()
                                "Запрос настроек отправлен (через общий канал)"
                            }
                            is ApiResult.Error -> {
                                Log.e(TAG, "Обе попытки неудачны. settings_request: ${settingsResult.message}, other: ${otherResult.message}")
                                "Ошибка отправки запроса настроек: ${otherResult.message}"
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка отправки запроса настроек: ${e.message}")
                "Ошибка отправки запроса настроек: ${e.message}"
            }
        }
    }
}