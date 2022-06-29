package com.smartgeeks.busticket.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.smartgeeks.busticket.data.local.AppDatabase
import com.smartgeeks.busticket.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

private const val BASE_URL = "https://mi.appbusticket.com/"
private val TAG: String = "OkHTTP"
private const val DB_NAME = "busticket"

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val interceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG, message)
    }

    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val okHttpBuilder = OkHttpClient.Builder()
        okHttpBuilder.addInterceptor(HttpLoggingInterceptor())
        okHttpBuilder.addInterceptor(interceptor)
        return okHttpBuilder.build()
    }

    @Singleton
    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) = app.getSharedPreferences(
        Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE
    )

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext app: Context) : AppDatabase =
        Room.databaseBuilder(app, AppDatabase::class.java, DB_NAME).build()

}