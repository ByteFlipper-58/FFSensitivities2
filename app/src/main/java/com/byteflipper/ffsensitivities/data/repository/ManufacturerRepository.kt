package com.byteflipper.ffsensitivities.data.repository

import com.byteflipper.ffsensitivities.domain.model.Manufacturer
import com.byteflipper.ffsensitivities.domain.model.ManufacturerResponse
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import com.byteflipper.ffsensitivities.di.GitHubClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject

class ManufacturerRepository @Inject constructor(
    @GitHubClient private val client: HttpClient
) {
    private val url = "https://raw.githubusercontent.com/ByteFlipper-58/database/refs/heads/main/FFSensitivities/manufacturers.json"

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