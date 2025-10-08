package com.compasspro.data.repository

import com.compasspro.data.api.WeatherApi
import com.compasspro.data.model.WeatherData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Response

/**
 * Repository for weather data
 * Handles weather API calls and data caching
 */
class WeatherRepository(
    private val weatherApi: WeatherApi,
    private val apiKey: String
) {
    
    private val _weatherData = MutableStateFlow<WeatherData?>(null)
    val weatherData: StateFlow<WeatherData?> = _weatherData.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Get current weather by coordinates
     */
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): Result<WeatherData> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val response = weatherApi.getCurrentWeather(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey
            )
            
            if (response.isSuccessful) {
                val weatherResponse = response.body()
                if (weatherResponse != null) {
                    val weatherData = mapWeatherResponse(weatherResponse)
                    _weatherData.value = weatherData
                    _isLoading.value = false
                    Result.success(weatherData)
                } else {
                    _error.value = "No weather data received"
                    _isLoading.value = false
                    Result.failure(Exception("No weather data received"))
                }
            } else {
                val errorMessage = "Weather API error: ${response.code()}"
                _error.value = errorMessage
                _isLoading.value = false
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            _error.value = e.message ?: "Unknown error occurred"
            _isLoading.value = false
            Result.failure(e)
        }
    }
    
    /**
     * Get current weather by city name
     */
    suspend fun getCurrentWeatherByCity(cityName: String): Result<WeatherData> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val response = weatherApi.getCurrentWeatherByCity(
                cityName = cityName,
                apiKey = apiKey
            )
            
            if (response.isSuccessful) {
                val weatherResponse = response.body()
                if (weatherResponse != null) {
                    val weatherData = mapWeatherResponse(weatherResponse)
                    _weatherData.value = weatherData
                    _isLoading.value = false
                    Result.success(weatherData)
                } else {
                    _error.value = "No weather data received"
                    _isLoading.value = false
                    Result.failure(Exception("No weather data received"))
                }
            } else {
                val errorMessage = "Weather API error: ${response.code()}"
                _error.value = errorMessage
                _isLoading.value = false
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            _error.value = e.message ?: "Unknown error occurred"
            _isLoading.value = false
            Result.failure(e)
        }
    }
    
    /**
     * Map API response to WeatherData
     */
    private fun mapWeatherResponse(response: com.compasspro.data.api.WeatherResponse): WeatherData {
        return WeatherData(
            temperature = response.main.temp,
            humidity = response.main.humidity,
            pressure = response.main.pressure,
            windSpeed = response.wind.speed,
            windDirection = response.wind.deg,
            windGust = response.wind.gust ?: 0f,
            visibility = response.visibility.toFloat(),
            location = response.name,
            description = response.weather.firstOrNull()?.description ?: "",
            icon = response.weather.firstOrNull()?.icon ?: "",
            timestamp = response.dt * 1000L // Convert to milliseconds
        )
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Get cached weather data
     */
    fun getCachedWeatherData(): WeatherData? {
        return _weatherData.value
    }
    
    /**
     * Check if weather data is recent (within 10 minutes)
     */
    fun isWeatherDataRecent(): Boolean {
        val weatherData = _weatherData.value ?: return false
        val currentTime = System.currentTimeMillis()
        val dataTime = weatherData.timestamp
        val timeDifference = currentTime - dataTime
        return timeDifference < 10 * 60 * 1000 // 10 minutes
    }
    
    /**
     * Get wind direction from compass heading
     * This is a simplified calculation - in production, use proper meteorological data
     */
    fun getWindDirectionFromCompass(compassHeading: Float): Float {
        // Wind direction is where the wind is coming FROM
        // If compass shows North (0°), and wind is from North, wind direction is 0°
        // If compass shows North (0°), and wind is to North, wind direction is 180°
        
        // This is a simplified calculation
        // In reality, wind direction should come from weather API
        return compassHeading
    }
    
    /**
     * Estimate wind speed based on device sensors
     * This is a very rough estimation and should not be used for accurate measurements
     */
    fun estimateWindSpeedFromSensors(accelerometerData: FloatArray): Float {
        // This is a placeholder implementation
        // Real wind speed estimation would require specialized sensors
        val magnitude = kotlin.math.sqrt(
            accelerometerData[0] * accelerometerData[0] +
            accelerometerData[1] * accelerometerData[1] +
            accelerometerData[2] * accelerometerData[2]
        )
        
        // Rough estimation (not accurate)
        return when {
            magnitude < 9.8f -> 0f // No wind
            magnitude < 10.5f -> 1f // Light wind
            magnitude < 11.5f -> 3f // Moderate wind
            magnitude < 13f -> 6f // Strong wind
            else -> 10f // Very strong wind
        }
    }
}
