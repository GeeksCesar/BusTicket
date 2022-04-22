package com.smartgeeks.busticket.di

import com.smartgeeks.busticket.data.api.AuthApi
import com.smartgeeks.busticket.data.api.InterCitiesApi
import com.smartgeeks.busticket.data.api.PriceApi
import com.smartgeeks.busticket.data.api.TicketApi
import com.smartgeeks.busticket.data.api.VehicleApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class ServiceModule {

    @Provides
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    fun provideVehicleApi(retrofit: Retrofit): VehicleApi {
        return retrofit.create(VehicleApi::class.java)
    }

    @Provides
    fun provideTicketApi(retrofit: Retrofit): TicketApi {
        return retrofit.create(TicketApi::class.java)
    }

    @Provides
    fun providePriceApi(retrofit: Retrofit): PriceApi = retrofit.create(PriceApi::class.java)

    @Provides
    fun provideInterCityApi(retrofit: Retrofit): InterCitiesApi =
        retrofit.create(InterCitiesApi::class.java)
}