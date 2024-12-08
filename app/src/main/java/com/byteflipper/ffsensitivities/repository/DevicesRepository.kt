package com.byteflipper.ffsensitivities.repository

import android.util.Log
import com.byteflipper.ffsensitivities.data.DeviceModel
import com.byteflipper.ffsensitivities.data.ManufacturerWithModels
import com.byteflipper.ffsensitivities.ui.UiState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import java.io.IOException

class DeviceRepository(private val client: HttpClient) {
    private val baseUrl = "https://raw.githubusercontent.com/ByteFlipper-58/database/refs/heads/main/FFSensitivities/"

    suspend fun fetchDevices(model: String): UiState<List<DeviceModel>> {
        val url = "$baseUrl$model.json"
        return try {
            val response: String = client.get(url) {
                contentType(ContentType.Application.Json)
            }.body()

            val deviceData = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }.decodeFromString(ManufacturerWithModels.serializer(), response)


            UiState.Success(deviceData.models)
        } catch (e: IOException) {
            UiState.NoInternet
            Log.e("DeviceRepository", "No internet connection", e)
        } catch (e: Exception) {
            UiState.Error(e.localizedMessage ?: "Неизвестная ошибка")
            Log.e("DeviceRepository", "Error fetching devices", e)
        } as UiState<List<DeviceModel>>
    }
}