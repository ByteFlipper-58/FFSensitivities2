package com.byteflipper.ffsensitivities.data.repository

import com.byteflipper.ffsensitivities.domain.model.DeviceModel
import com.byteflipper.ffsensitivities.domain.model.ManufacturerWithModels
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import android.util.Log
import io.ktor.http.contentType
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject // Import Inject

class DevicesRepository @Inject constructor(private val client: HttpClient) {
    private companion object {
        private const val TAG = "DevicesRepository"
    }
    private val baseUrl = "https://raw.githubusercontent.com/ByteFlipper-58/database/refs/heads/main/FFSensitivities/"

    suspend fun fetchDevices(manufacturer: String): UiState<List<DeviceModel>> {
        // Convert manufacturer name to lowercase to match filename convention
        val fileName = manufacturer.lowercase()
        val url = "$baseUrl$fileName.json"
        Log.d(TAG, "Fetching devices from URL: $url")
        return try {
            val response: String = client.get(url) {
                contentType(ContentType.Application.Json)
            }.body()

            val manufacturerWithModels = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }.decodeFromString(ManufacturerWithModels.serializer(), response)

            UiState.Success(manufacturerWithModels.models)
        } catch (e: SerializationException) {
            Log.e(TAG, "Serialization error", e)
            UiState.Error("Ошибка десериализации данных: ${e.message}")
        } catch (e: IOException) {
            Log.e(TAG, "No internet connection", e)
            UiState.NoInternet
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching devices", e)
            UiState.Error(e.localizedMessage ?: "Неизвестная ошибка")
        } as UiState<List<DeviceModel>> // Cast might be unnecessary if return types align
    }
}
