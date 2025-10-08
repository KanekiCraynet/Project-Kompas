package com.compasspro.data.api

import com.compasspro.domain.model.WindData
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
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") language: String = "id"
    ): Response<WeatherResponse>
    
    /**
     * Mendapatkan data cuaca untuk 5 hari ke depan
     */
    @GET("forecast")
    suspend fun getWeatherForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") language: String = "id"
    ): Response<WeatherForecastResponse>
}

/**
 * Response model untuk data cuaca saat ini
 */
data class WeatherResponse(
    val coord: Coordinates,
    val weather: List<Weather>,
    val main: MainWeather,
    val wind: Wind,
    val visibility: Int,
    val dt: Long,
    val sys: SystemInfo,
    val name: String
)

/**
 * Response model untuk forecast cuaca
 */
data class WeatherForecastResponse(
    val list: List<ForecastItem>,
    val city: City
)

/**
 * Model untuk koordinat
 */
data class Coordinates(
    val lon: Double,
    val lat: Double
)

/**
 * Model untuk informasi cuaca
 */
data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

/**
 * Model untuk data cuaca utama
 */
data class MainWeather(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
)

/**
 * Model untuk data angin
 */
data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double? = null
)

/**
 * Model untuk informasi sistem
 */
data class SystemInfo(
    val country: String,
    val sunrise: Long,
    val sunset: Long
)

/**
 * Model untuk item forecast
 */
data class ForecastItem(
    val dt: Long,
    val main: MainWeather,
    val weather: List<Weather>,
    val wind: Wind,
    val visibility: Int
)

/**
 * Model untuk informasi kota
 */
data class City(
    val id: Int,
    val name: String,
    val country: String,
    val coord: Coordinates
)

/**
 * Extension function untuk konversi WeatherResponse ke WindData
 */
fun WeatherResponse.toWindData(): WindData {
    return WindData(
        direction = wind.deg.toFloat(),
        speed = wind.speed.toFloat(),
        gust = wind.gust?.toFloat() ?: 0f,
        temperature = main.temp.toFloat(),
        humidity = main.humidity.toFloat(),
        pressure = main.pressure.toFloat(),
        visibility = visibility.toFloat(),
        timestamp = dt * 1000L // Convert to milliseconds
    )
}

/**
 * Extension function untuk konversi ForecastItem ke WindData
 */
fun ForecastItem.toWindData(): WindData {
    return WindData(
        direction = wind.deg.toFloat(),
        speed = wind.speed.toFloat(),
        gust = wind.gust?.toFloat() ?: 0f,
        temperature = main.temp.toFloat(),
        humidity = main.humidity.toFloat(),
        pressure = main.pressure.toFloat(),
        visibility = visibility.toFloat(),
        timestamp = dt * 1000L // Convert to milliseconds
    )
}
