package com.compasspro.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compasspro.data.model.WeatherData
import com.compasspro.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for weather functionality
 * Manages weather state and user interactions
 */
class WeatherViewModel(
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    
    private val _weatherData = MutableStateFlow<WeatherData?>(null)
    val weatherData: StateFlow<WeatherData?> = _weatherData.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _lastUpdateTime = MutableStateFlow(0L)
    val lastUpdateTime: StateFlow<Long> = _lastUpdateTime.asStateFlow()
    
    init {
        observeWeatherData()
    }
    
    private fun observeWeatherData() {
        viewModelScope.launch {
            weatherRepository.weatherData.collect { data ->
                _weatherData.value = data
                if (data != null) {
                    _lastUpdateTime.value = data.timestamp
                }
            }
        }
        
        viewModelScope.launch {
            weatherRepository.isLoading.collect { loading ->
                _isLoading.value = loading
            }
        }
        
        viewModelScope.launch {
            weatherRepository.error.collect { error ->
                _error.value = error
            }
        }
    }
    
    /**
     * Get current weather by coordinates
     */
    fun getCurrentWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val result = weatherRepository.getCurrentWeather(latitude, longitude)
            result.onFailure { exception ->
                _error.value = exception.message ?: "Failed to get weather data"
            }
        }
    }
    
    /**
     * Get current weather by city name
     */
    fun getCurrentWeatherByCity(cityName: String) {
        viewModelScope.launch {
            val result = weatherRepository.getCurrentWeatherByCity(cityName)
            result.onFailure { exception ->
                _error.value = exception.message ?: "Failed to get weather data"
            }
        }
    }
    
    /**
     * Refresh weather data
     */
    fun refreshWeatherData(latitude: Double, longitude: Double) {
        getCurrentWeather(latitude, longitude)
    }
    
    /**
     * Get current weather data
     */
    fun getCurrentWeatherData(): WeatherData? {
        return _weatherData.value
    }
    
    /**
     * Get wind direction string
     */
    fun getWindDirectionString(): String {
        return _weatherData.value?.getWindDirectionString() ?: "N/A"
    }
    
    /**
     * Get wind speed category
     */
    fun getWindSpeedCategory(): String {
        return _weatherData.value?.getWindSpeedCategory()?.name ?: "UNKNOWN"
    }
    
    /**
     * Get wind speed color
     */
    fun getWindSpeedColor(): String {
        return _weatherData.value?.getWindSpeedColor() ?: "gray"
    }
    
    /**
     * Get formatted temperature
     */
    fun getFormattedTemperature(): String {
        return _weatherData.value?.getFormattedTemperature() ?: "N/A"
    }
    
    /**
     * Get formatted wind speed
     */
    fun getFormattedWindSpeed(): String {
        return _weatherData.value?.getFormattedWindSpeed() ?: "N/A"
    }
    
    /**
     * Get formatted pressure
     */
    fun getFormattedPressure(): String {
        return _weatherData.value?.getFormattedPressure() ?: "N/A"
    }
    
    /**
     * Get formatted humidity
     */
    fun getFormattedHumidity(): String {
        return _weatherData.value?.getFormattedHumidity() ?: "N/A"
    }
    
    /**
     * Get formatted visibility
     */
    fun getFormattedVisibility(): String {
        return _weatherData.value?.getFormattedVisibility() ?: "N/A"
    }
    
    /**
     * Get weather status message
     */
    fun getWeatherStatus(): String {
        return when {
            _error.value != null -> _error.value!!
            _isLoading.value -> "Loading weather data..."
            _weatherData.value == null -> "No weather data"
            else -> "Weather data updated"
        }
    }
    
    /**
     * Get weather status color
     */
    fun getWeatherStatusColor(): String {
        return when {
            _error.value != null -> "red"
            _isLoading.value -> "orange"
            _weatherData.value == null -> "orange"
            else -> "green"
        }
    }
    
    /**
     * Check if weather data is recent
     */
    fun isWeatherDataRecent(): Boolean {
        return weatherRepository.isWeatherDataRecent()
    }
    
    /**
     * Get time since last update
     */
    fun getTimeSinceLastUpdate(): String {
        val lastUpdate = _lastUpdateTime.value
        if (lastUpdate == 0L) return "Never"
        
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - lastUpdate
        
        return when {
            timeDifference < 60000 -> "Just now" // Less than 1 minute
            timeDifference < 3600000 -> "${timeDifference / 60000} minutes ago" // Less than 1 hour
            timeDifference < 86400000 -> "${timeDifference / 3600000} hours ago" // Less than 1 day
            else -> "${timeDifference / 86400000} days ago" // More than 1 day
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
        weatherRepository.clearError()
    }
    
    /**
     * Get detailed weather information
     */
    fun getWeatherInfo(): WeatherInfo {
        val data = _weatherData.value
        return WeatherInfo(
            weatherData = data,
            isLoading = _isLoading.value,
            error = _error.value,
            status = getWeatherStatus(),
            statusColor = getWeatherStatusColor(),
            lastUpdateTime = _lastUpdateTime.value,
            timeSinceLastUpdate = getTimeSinceLastUpdate(),
            isRecent = isWeatherDataRecent()
        )
    }
}

/**
 * Data class for weather information
 */
data class WeatherInfo(
    val weatherData: WeatherData?,
    val isLoading: Boolean,
    val error: String?,
    val status: String,
    val statusColor: String,
    val lastUpdateTime: Long,
    val timeSinceLastUpdate: String,
    val isRecent: Boolean
)
