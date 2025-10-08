package com.compasspro.di

import com.compasspro.BuildConfig
import com.compasspro.data.api.WeatherApi
import com.compasspro.data.repository.WeatherRepository
import com.compasspro.service.CompassService
import com.compasspro.service.LocationService
import com.compasspro.ui.screen.CompassViewModel
import com.compasspro.ui.screen.LocationViewModel
import com.compasspro.ui.screen.WeatherViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Koin dependency injection module
 * Provides all necessary dependencies for the application
 */
val appModule = module {
    
    // Network components
    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    single {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    single<WeatherApi> {
        get<Retrofit>().create(WeatherApi::class.java)
    }
    
    // Repository
    single {
        WeatherRepository(
            weatherApi = get(),
            apiKey = BuildConfig.WEATHER_API_KEY
        )
    }
    
    // Services
    single { CompassService() }
    single { LocationService() }
    
    // ViewModels
    viewModel { CompassViewModel(get()) }
    viewModel { LocationViewModel(get()) }
    viewModel { WeatherViewModel(get()) }
}
