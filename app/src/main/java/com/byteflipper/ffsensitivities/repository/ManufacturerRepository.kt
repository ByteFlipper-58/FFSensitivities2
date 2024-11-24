package com.byteflipper.ffsensitivities.repository

import com.byteflipper.ffsensitivities.data.Manufacturer
import com.byteflipper.ffsensitivities.data.ManufacturerResponse
import com.byteflipper.ffsensitivities.ui.UiState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import java.io.IOException

class ManufacturerRepository(private val client: HttpClient) {
    private val url = "https://raw.githubusercontent.com/ByteFlipper-58/FFSensitivities/refs/heads/master/app/src/main/assets/sensitivity_settings/manufacturers.json"

    suspend fun fetchManufacturers(): UiState<List<Manufacturer>> {
        return try {
            val response: String = client.get(url) {
                contentType(ContentType.Application.Json)
            }.body()

            val manufacturers = Json { ignoreUnknownKeys = true }
                .decodeFromString(ManufacturerResponse.serializer(), response)
                .manufacturers
                .filter { it.showInProductionApp }

            UiState.Success(manufacturers)
        } catch (e: IOException) {
            UiState.NoInternet
        } catch (e: Exception) {
            UiState.Error(e.localizedMessage ?: "Неизвестная ошибка")
        }
    }
}