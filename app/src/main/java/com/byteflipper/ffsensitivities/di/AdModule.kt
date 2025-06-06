package com.byteflipper.ffsensitivities.di

import android.content.Context
import com.byteflipper.ffsensitivities.ads.ConsentManager
import com.byteflipper.ffsensitivities.ads.repository.AdRepository
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdModule {

    @Provides
    @Singleton
    fun provideConsentManager(
        @ApplicationContext context: Context,
        @ApplicationScope coroutineScope: CoroutineScope
    ): ConsentManager {
        return ConsentManager(context, coroutineScope)
    }

    @Provides
    @Singleton
    fun provideAdRepository(
        @ApplicationContext context: Context,
        consentManager: ConsentManager,
        @ApplicationScope coroutineScope: CoroutineScope,
        dataStoreManager: DataStoreManager
    ): AdRepository {
        return AdRepository(context, consentManager, coroutineScope, dataStoreManager)
    }
} 