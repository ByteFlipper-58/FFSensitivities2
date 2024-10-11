package com.byteflipper.ffsensitivities.service

import com.byteflipper.ffsensitivities.data.ManufacturerResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ApiService {
    @GET("sensitivity_settings/manufacturers.json")
    suspend fun getManufacturers(): ManufacturerResponse
}

object RetrofitInstance {
    private const val BASE_URL = "https://raw.githubusercontent.com/ByteFlipper-58/FFSensitivities/refs/heads/master/app/src/main/assets/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
