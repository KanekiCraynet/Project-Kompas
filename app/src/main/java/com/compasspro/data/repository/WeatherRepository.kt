package com.compasspro.data.repository

import com.compasspro.data.api.WeatherApiService
import com.compasspro.data.model.toWindData
import com.compasspro.domain.model.WindData
import com.compasspro.utils.Config
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository untuk mengelola data cuaca dan arah angin
 * Mengintegrasikan API cuaca dengan cache lokal
 */
@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService
) {
    
    private var cachedWindData: WindData? = null
    private var lastUpdateTime: Long = 0
    private val cacheValidityDuration = Config.CACHE_VALIDITY_DURATION_MS
    
    /**
     * Mendapatkan data arah angin berdasarkan lokasi
     */
    suspend fun getWindData(latitude: Double, longitude: Double): Flow<WindData> = flow {
        try {
            // Cek cache terlebih dahulu
            if (isCacheValid()) {
                cachedWindData?.let { 
                    emit(it)
                    return@flow
                }
            }
            
            // Ambil data dari API
            val response = weatherApiService.getCurrentWeather(
                latitude = latitude,
                longitude = longitude
            )
            
            if (response.isSuccessful) {
                val weatherResponse = response.body()
                if (weatherResponse != null) {
                    val windData = weatherResponse.toWindData()
                    
                    // Update cache
                    cachedWindData = windData
                    lastUpdateTime = System.currentTimeMillis()
                    
                    emit(windData)
                } else {
                    // Fallback ke data default jika API gagal
                    emit(getDefaultWindData())
                }
            } else {
                // Fallback ke data default jika API gagal
                emit(getDefaultWindData())
            }
            
        } catch (e: Exception) {
            // Fallback ke data default jika terjadi error
            emit(getDefaultWindData())
        }
    }
    
    /**
     * Mendapatkan forecast arah angin untuk beberapa jam ke depan
     */
    suspend fun getWindForecast(latitude: Double, longitude: Double): Flow<List<WindData>> = flow {
        try {
            val response = weatherApiService.getWeatherForecast(
                latitude = latitude,
                longitude = longitude
            )
            
            if (response.isSuccessful) {
                val forecastResponse = response.body()
                if (forecastResponse != null) {
                    val windForecast = forecastResponse.list.take(8).map { it.toWindData() }
                    emit(windForecast)
                } else {
                    emit(emptyList())
                }
            } else {
                emit(emptyList())
            }
            
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Mendapatkan data arah angin yang sudah di-cache
     */
    fun getCachedWindData(): WindData? {
        return if (isCacheValid()) cachedWindData else null
    }
    
    /**
     * Cek apakah cache masih valid
     */
    private fun isCacheValid(): Boolean {
        return cachedWindData != null && 
               (System.currentTimeMillis() - lastUpdateTime) < cacheValidityDuration
    }
    
    /**
     * Data angin default jika API tidak tersedia
     */
    private fun getDefaultWindData(): WindData {
        return WindData(
            direction = 0f,
            speed = 0f,
            gust = 0f,
            temperature = 25f,
            humidity = 60f,
            pressure = 1013f,
            visibility = 10000f,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Clear cache data cuaca
     */
    fun clearCache() {
        cachedWindData = null
        lastUpdateTime = 0
    }
    
    /**
     * Simulasi data angin untuk testing (jika API tidak tersedia)
     */
    fun getSimulatedWindData(): WindData {
        val currentTime = System.currentTimeMillis()
        val hour = (currentTime / (1000 * 60 * 60)) % 24
        
        // Simulasi arah angin yang berubah sepanjang hari
        val baseDirection = (hour * 15f) % 360f
        val windSpeed = 2f + (kotlin.math.sin(hour * 0.5) * 3f)
        
        return WindData(
            direction = baseDirection,
            speed = windSpeed,
            gust = windSpeed * 1.5f,
            temperature = 20f + (kotlin.math.sin(hour * 0.3) * 10f),
            humidity = 50f + (kotlin.math.cos(hour * 0.4) * 20f),
            pressure = 1013f + (kotlin.math.sin(hour * 0.2) * 10f),
            visibility = 8000f + (kotlin.math.cos(hour * 0.1) * 2000f),
            timestamp = currentTime
        )
    }
}
