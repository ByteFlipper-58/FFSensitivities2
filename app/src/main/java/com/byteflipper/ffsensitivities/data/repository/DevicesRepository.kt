package com.byteflipper.ffsensitivities.data.repository

import com.byteflipper.ffsensitivities.domain.model.DeviceModel
import com.byteflipper.ffsensitivities.domain.model.ManufacturerWithModels
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.IOException

class DeviceRepository(private val client: HttpClient) {
    private val baseUrl = "https://raw.githubusercontent.com/ByteFlipper-58/database/refs/heads/main/FFSensitivities/"

    suspend fun fetchDevices(model: String): UiState<List<DeviceModel>> {
        val url = "$baseUrl$model.json"
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
            Timber.tag("DeviceRepository").e(e, "Serialization error")
            UiState.Error("Ошибка десериализации данных: ${e.message}")
        } catch (e: IOException) {
            UiState.NoInternet
            Timber.tag("DeviceRepository").e(e, "No internet connection")
        } catch (e: Exception) {
            UiState.Error(e.localizedMessage ?: "Неизвестная ошибка")
            Timber.tag("DeviceRepository").e(e, "Error fetching devices")
        } as UiState<List<DeviceModel>>
    }
}