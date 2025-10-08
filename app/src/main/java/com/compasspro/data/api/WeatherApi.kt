package com.compasspro.data.api

import com.compasspro.data.model.WeatherData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API interface for weather data
 * Uses OpenWeatherMap API for weather information
 */
interface WeatherApi {
    
    /**
     * Get current weather data by coordinates
     */
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<WeatherResponse>
    
    /**
     * Get current weather data by city name
     */
    @GET("weather")
    suspend fun getCurrentWeatherByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<WeatherResponse>
    
    /**
     * Get weather forecast
     */
    @GET("forecast")
    suspend fun getWeatherForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<ForecastResponse>
}

/**
 * Response data class for current weather
 */
data class WeatherResponse(
    val coord: Coordinates,
    val weather: List<Weather>,
    val main: MainWeather,
    val wind: Wind,
    val visibility: Int,
    val dt: Long,
    val name: String
)

/**
 * Response data class for weather forecast
 */
data class ForecastResponse(
    val list: List<ForecastItem>
)

/**
 * Forecast item data class
 */
data class ForecastItem(
    val dt: Long,
    val main: MainWeather,
    val weather: List<Weather>,
    val wind: Wind,
    val visibility: Int
)

/**
 * Coordinates data class
 */
data class Coordinates(
    val lat: Double,
    val lon: Double
)

/**
 * Weather condition data class
 */
data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

/**
 * Main weather data class
 */
data class MainWeather(
    val temp: Float,
    val feels_like: Float,
    val temp_min: Float,
    val temp_max: Float,
    val pressure: Float,
    val humidity: Float
)

/**
 * Wind data class
 */
data class Wind(
    val speed: Float,
    val deg: Float,
    val gust: Float? = null
)
