package com.smartgeeks.busticket.di

import com.smartgeeks.busticket.repository.auth.AuthRepository
import com.smartgeeks.busticket.repository.auth.AuthRepositoryImpl
import com.smartgeeks.busticket.repository.vehicle.VehicleRepository
import com.smartgeeks.busticket.repository.vehicle.VehicleRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {

    /*@Singleton
    @Provides
    fun provideMainRepository(authApi: AuthApi): AuthRepository = AuthRepositoryImpl(authApi)*/

    @Binds
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    abstract fun bindVehicleRepository(
        ticketRepositoryImpl: VehicleRepositoryImpl
    ): VehicleRepository
}