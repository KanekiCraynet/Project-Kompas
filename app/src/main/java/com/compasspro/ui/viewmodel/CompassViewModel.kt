package com.compasspro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compasspro.domain.model.CompassData
import com.compasspro.domain.model.LocationData
import com.compasspro.domain.model.RawSensorData
import com.compasspro.domain.model.WindData
import com.compasspro.domain.usecase.CompassUseCase
import com.compasspro.data.repository.WeatherRepository
import com.compasspro.service.LocationService
import com.compasspro.service.SensorService
import com.compasspro.utils.Config
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel untuk mengelola state dan logika bisnis kompas
 * Mengintegrasikan data dari sensor, lokasi, dan cuaca
 */
@HiltViewModel
class CompassViewModel @Inject constructor(
    private val compassUseCase: CompassUseCase,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private var sensorService: SensorService? = null
    private var locationService: LocationService? = null
    
    // State flows
    private val _compassData = MutableStateFlow(CompassData())
    val compassData: StateFlow<CompassData> = _compassData.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Current data
    private var currentLocationData: LocationData? = null
    private var currentWindData: WindData? = null
    private var magneticDeclination: Float = 0f

    init {
        // Set magnetic declination untuk Indonesia (sekitar -1° hingga +1°)
        magneticDeclination = 0f
    }

    /**
     * Set sensor service dan mulai monitoring
     */
    fun setSensorService(service: SensorService) {
        sensorService = service
        startSensorMonitoring()
    }

    /**
     * Set location service dan mulai monitoring
     */
    fun setLocationService(service: LocationService) {
        locationService = service
        startLocationMonitoring()
    }

    /**
     * Mulai monitoring sensor
     */
    private fun startSensorMonitoring() {
        sensorService?.let { service ->
            viewModelScope.launch {
                service.getSensorDataFlow().collect { rawData ->
                    processSensorData(rawData)
                }
            }
        }
    }

    /**
     * Mulai monitoring lokasi
     */
    private fun startLocationMonitoring() {
        locationService?.let { service ->
            viewModelScope.launch {
                service.getLocationDataFlow().collect { locationData ->
                    currentLocationData = locationData
                    updateCompassData()
                    fetchWindData(locationData.latitude, locationData.longitude)
                }
            }
        }
    }

    /**
     * Proses data sensor mentah
     */
    private fun processSensorData(rawData: RawSensorData) {
        try {
            // Update use case dengan data terbaru
            compassUseCase.updateLastRawData(rawData)
            
            // Proses data kompas
            val processedData = compassUseCase.processCompassData(
                rawData = rawData,
                magneticDeclination = magneticDeclination,
                locationData = currentLocationData,
                windData = currentWindData
            )
            
            _compassData.value = processedData
            _isLoading.value = false
            _errorMessage.value = null
            
        } catch (e: Exception) {
            _errorMessage.value = "Error memproses data sensor: ${e.message}"
        }
    }

    /**
     * Update data kompas dengan informasi terbaru
     */
    private fun updateCompassData() {
        val currentData = _compassData.value
        _compassData.value = currentData.copy(
            location = currentLocationData,
            windData = currentWindData
        )
    }

    /**
     * Ambil data angin dari repository
     */
    private fun fetchWindData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                weatherRepository.getWindData(latitude, longitude).collect { windData ->
                    currentWindData = windData
                    updateCompassData()
                }
            } catch (e: Exception) {
                // Gunakan data simulasi jika API tidak tersedia
                currentWindData = weatherRepository.getSimulatedWindData()
                updateCompassData()
            }
        }
    }

    /**
     * Mulai proses kalibrasi sensor
     */
    fun startCalibration() {
        try {
            compassUseCase.startCalibration()
            sensorService?.startCalibration()
            _errorMessage.value = "Kalibrasi dimulai. Gerakkan perangkat dalam bentuk angka 8"
            
            // Selesai kalibrasi setelah 10 detik
            viewModelScope.launch {
                kotlinx.coroutines.delay(10000)
                finishCalibration()
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error memulai kalibrasi: ${e.message}"
        }
    }

    /**
     * Selesai proses kalibrasi
     */
    private fun finishCalibration() {
        try {
            sensorService?.finishCalibration()
            _errorMessage.value = "Kalibrasi selesai"
        } catch (e: Exception) {
            _errorMessage.value = "Error menyelesaikan kalibrasi: ${e.message}"
        }
    }

    /**
     * Refresh data kompas
     */
    fun refreshData() {
        _isLoading.value = true
        _errorMessage.value = null
        
        // Ambil lokasi terakhir yang diketahui
        locationService?.getLastKnownLocation()
        
        // Cek apakah sensor perlu dikalibrasi
        if (compassUseCase.needsCalibration()) {
            _errorMessage.value = "Sensor perlu dikalibrasi untuk akurasi optimal"
        }
    }

    /**
     * Set target lokasi untuk navigasi
     */
    fun setTargetLocation(latitude: Double, longitude: Double) {
        val currentData = _compassData.value
        val distance = currentData.getDistanceToTarget(latitude, longitude)
        val bearing = currentData.getBearingToTarget(latitude, longitude)
        
        // Update data dengan informasi target
        _compassData.value = currentData.copy(
            // Bisa ditambahkan field targetLocation jika diperlukan
        )
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Cek status sensor
     */
    fun isSensorAvailable(): Boolean {
        return sensorService?.areSensorsAvailable() ?: false
    }

    /**
     * Cek status kalibrasi
     */
    fun isSensorCalibrated(): Boolean {
        return sensorService?.isCalibrated() ?: false
    }

    /**
     * Cek status GPS
     */
    fun isGpsEnabled(): Boolean {
        return locationService?.isGpsEnabled() ?: false
    }

    /**
     * Get akurasi sensor saat ini
     */
    fun getCurrentAccuracy(): Float {
        return _compassData.value.accuracy
    }

    /**
     * Get kualitas sensor saat ini
     */
    fun getCurrentSensorQuality(): String {
        return when (_compassData.value.sensorQuality) {
            com.compasspro.domain.model.SensorQuality.EXCELLENT -> "Sangat Baik"
            com.compasspro.domain.model.SensorQuality.GOOD -> "Baik"
            com.compasspro.domain.model.SensorQuality.FAIR -> "Cukup"
            com.compasspro.domain.model.SensorQuality.POOR -> "Buruk"
            com.compasspro.domain.model.SensorQuality.UNKNOWN -> "Tidak Diketahui"
        }
    }
}
