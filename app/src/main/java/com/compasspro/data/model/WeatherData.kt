package com.compasspro.data.model

import com.compasspro.domain.model.WindData

/**
 * Data model untuk response API cuaca
 */
data class WeatherApiResponse(
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
 * Data model untuk forecast cuaca
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
 * Extension function untuk konversi WeatherApiResponse ke WindData
 */
fun WeatherApiResponse.toWindData(): WindData {
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
