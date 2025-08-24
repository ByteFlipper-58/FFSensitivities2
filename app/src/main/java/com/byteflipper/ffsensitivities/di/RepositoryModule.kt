package com.byteflipper.ffsensitivities.di

import com.byteflipper.ffsensitivities.data.repository.DevicesRepository
import com.byteflipper.ffsensitivities.data.repository.ManufacturerRepository
import com.byteflipper.ffsensitivities.utils.CacheManager
import com.byteflipper.ffsensitivities.utils.ErrorHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideManufacturerRepository(
        @GitHubClient client: HttpClient
    ): ManufacturerRepository {
        return ManufacturerRepository(client)
    }

    @Provides
    @Singleton
    fun provideDevicesRepository(
        @GitHubClient client: HttpClient,
        errorHandler: ErrorHandler,
        cacheManager: CacheManager
    ): DevicesRepository {
        return DevicesRepository(client, errorHandler, cacheManager)
    }
}

