package com.byteflipper.ffsensitivities

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

class SendSensitivitiesRequestMessageToBot {

    private val TAG = "SendMessageToGroupTask"

    private val BOT_TOKEN = BuildConfig.TELEGRAM_BOT_TOKEN
    private val GROUP_CHAT_ID = BuildConfig.BYTEFLIPPER_CHANNEL_ID

    suspend fun sendMessageToGroup(message: String, onSuccess: () -> Unit): String {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()

                val formBody: RequestBody = FormBody.Builder()
                    .add("chat_id", GROUP_CHAT_ID)
                    .add("text", message)
                    .build()

                val request = Request.Builder()
                    .url("https://api.telegram.org/bot$BOT_TOKEN/sendMessage")
                    .post(formBody)
                    .build()

                val response: Response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.e(TAG, "Ошибка отправки сообщения: ${response.code}")
                    return@withContext "Ошибка отправки сообщения"
                }

                onSuccess()

                "Сообщение успешно отправлено в группу"
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка отправки сообщения: ${e.message}")
                "Ошибка отправки сообщения"
            }
        }
    }
}