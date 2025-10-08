package com.compasspro.data.api

import com.compasspro.data.model.WeatherApiResponse
import com.compasspro.data.model.WeatherForecastResponse
import com.compasspro.utils.Config
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface untuk API cuaca yang menyediakan data arah angin
 * Menggunakan OpenWeatherMap API sebagai contoh
 */
interface WeatherApiService {
    
    /**
     * Mendapatkan data cuaca saat ini berdasarkan koordinat
     */
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = Config.WEATHER_API_KEY,
        @Query("units") units: String = "metric",
        @Query("lang") language: String = "id"
    ): Response<WeatherApiResponse>
    
    /**
     * Mendapatkan data cuaca untuk 5 hari ke depan
     */
    @GET("forecast")
    suspend fun getWeatherForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = Config.WEATHER_API_KEY,
        @Query("units") units: String = "metric",
        @Query("lang") language: String = "id"
    ): Response<WeatherForecastResponse>
}
