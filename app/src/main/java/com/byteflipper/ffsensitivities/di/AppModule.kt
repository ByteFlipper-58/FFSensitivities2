package com.byteflipper.ffsensitivities.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.byteflipper.ffsensitivities.data.remote.BugReportApiService
import com.byteflipper.ffsensitivities.data.remote.BugReportApiClient
import com.byteflipper.ffsensitivities.domain.usecase.FetchDevicesUseCase
import com.byteflipper.ffsensitivities.domain.usecase.FetchManufacturersUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @ApplicationScope
    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun provideBugReportApiService(client: BugReportApiClient): BugReportApiService {
        return BugReportApiService(client)
    }

    @Provides
    @Singleton
    fun provideFetchManufacturersUseCase(
        manufacturerRepository: com.byteflipper.ffsensitivities.data.repository.ManufacturerRepository
    ): FetchManufacturersUseCase = FetchManufacturersUseCase(manufacturerRepository)

    @Provides
    @Singleton
    fun provideFetchDevicesUseCase(
        devicesRepository: com.byteflipper.ffsensitivities.data.repository.DevicesRepository
    ): FetchDevicesUseCase = FetchDevicesUseCase(devicesRepository)
}
