package com.byteflipper.ffsensitivities.di

import com.byteflipper.ffsensitivities.core.monetization.DefaultMonetizationCoordinator
import com.byteflipper.ffsensitivities.core.monetization.MonetizationCoordinator
import com.byteflipper.ffsensitivities.core.update.AppUpdateCoordinatorFactory
import com.byteflipper.ffsensitivities.playcore.PlayCoreAppUpdateCoordinatorFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreBindingsModule {

    @Binds
    @Singleton
    abstract fun bindMonetizationCoordinator(
        coordinator: DefaultMonetizationCoordinator
    ): MonetizationCoordinator

    @Binds
    @Singleton
    abstract fun bindAppUpdateCoordinatorFactory(
        factory: PlayCoreAppUpdateCoordinatorFactory
    ): AppUpdateCoordinatorFactory
}
