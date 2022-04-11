package com.smartgeeks.busticket.di

import com.smartgeeks.busticket.data.local.AppDatabase
import com.smartgeeks.busticket.data.local.TicketDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EntityModule {

    @Provides
    @Singleton
    fun provideEntityDao(appDatabase: AppDatabase): TicketDAO = appDatabase.ticketDao()

}