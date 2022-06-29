package com.smartgeeks.busticket.di

import com.smartgeeks.busticket.repository.auth.AuthRepository
import com.smartgeeks.busticket.repository.auth.AuthRepositoryImpl
import com.smartgeeks.busticket.repository.intercities.InterCitiesRepository
import com.smartgeeks.busticket.repository.intercities.InterCitiesRepositoryImpl
import com.smartgeeks.busticket.repository.price.PriceRepository
import com.smartgeeks.busticket.repository.price.PriceRepositoryImpl
import com.smartgeeks.busticket.repository.ticket.TicketRepository
import com.smartgeeks.busticket.repository.ticket.TicketRepositoryImpl
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
        vehicleRepositoryImpl: VehicleRepositoryImpl
    ): VehicleRepository

    @Binds
    abstract fun bindTicketRepository(
        ticketRepositoryImpl: TicketRepositoryImpl
    ): TicketRepository

    @Binds
    abstract fun bindPriceRepository(
        priceRepositoryImpl: PriceRepositoryImpl
    ): PriceRepository

    @Binds
    abstract fun bindInterCitiesRepository(
        interCitiesRepositoryImpl: InterCitiesRepositoryImpl
    ) : InterCitiesRepository
}