package com.example.myot.retrofit2

import com.example.myot.retrofit2.RetrofitClient
import com.example.myot.retrofit2.TicketService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun provideTicketService(): TicketService = RetrofitClient.ticketService
}
