package com.compasspro.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compasspro.data.model.CompassData
import com.compasspro.service.CompassService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for compass functionality
 * Manages compass state and user interactions
 */
class CompassViewModel(
    private val compassService: CompassService
) : ViewModel() {
    
    private val _compassData = MutableStateFlow<CompassData?>(null)
    val compassData: StateFlow<CompassData?> = _compassData.asStateFlow()
    
    private val _isCalibrated = MutableStateFlow(false)
    val isCalibrated: StateFlow<Boolean> = _isCalibrated.asStateFlow()
    
    private val _calibrationProgress = MutableStateFlow(0)
    val calibrationProgress: StateFlow<Int> = _calibrationProgress.asStateFlow()
    
    private val _isCalibrating = MutableStateFlow(false)
    val isCalibrating: StateFlow<Boolean> = _isCalibrating.asStateFlow()
    
    private val _currentHeading = MutableStateFlow(0f)
    val currentHeading: StateFlow<Float> = _currentHeading.asStateFlow()
    
    private val _currentDirection = MutableStateFlow("N")
    val currentDirection: StateFlow<String> = _currentDirection.asStateFlow()
    
    private val _sensorInfo = MutableStateFlow<com.compasspro.service.SensorInfo?>(null)
    val sensorInfo: StateFlow<com.compasspro.service.SensorInfo?> = _sensorInfo.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        observeCompassData()
        checkSensorAvailability()
    }
    
    private fun observeCompassData() {
        viewModelScope.launch {
            compassService.compassData.collect { data ->
                _compassData.value = data
                data?.let {
                    _currentHeading.value = it.getMagneticHeading()
                    _currentDirection.value = it.getDirectionString()
                }
            }
        }
        
        viewModelScope.launch {
            compassService.isCalibrated.collect { calibrated ->
                _isCalibrated.value = calibrated
            }
        }
        
        viewModelScope.launch {
            compassService.calibrationProgress.collect { progress ->
                _calibrationProgress.value = progress
            }
        }
    }
    
    private fun checkSensorAvailability() {
        if (!compassService.areSensorsAvailable()) {
            _error.value = "Required sensors are not available on this device"
        } else {
            _sensorInfo.value = compassService.getSensorInfo()
        }
    }
    
    /**
     * Start compass calibration
     */
    fun startCalibration() {
        if (!compassService.areSensorsAvailable()) {
            _error.value = "Sensors not available for calibration"
            return
        }
        
        _isCalibrating.value = true
        _error.value = null
        
        try {
            compassService.startCalibration()
        } catch (e: Exception) {
            _error.value = e.message ?: "Calibration failed"
            _isCalibrating.value = false
        }
    }
    
    /**
     * Stop calibration process
     */
    fun stopCalibration() {
        _isCalibrating.value = false
    }
    
    /**
     * Get current compass heading
     */
    fun getCurrentHeading(): Float {
        return _currentHeading.value
    }
    
    /**
     * Get current compass direction
     */
    fun getCurrentDirection(): String {
        return _currentDirection.value
    }
    
    /**
     * Get compass calibration quality
     */
    fun getCalibrationQuality(): Int {
        return _compassData.value?.getCalibrationQuality() ?: 0
    }
    
    /**
     * Check if compass is calibrated
     */
    fun isCompassCalibrated(): Boolean {
        return _isCalibrated.value
    }
    
    /**
     * Get compass status message
     */
    fun getCompassStatus(): String {
        return when {
            _error.value != null -> _error.value!!
            !compassService.areSensorsAvailable() -> "Sensors not available"
            _isCalibrating.value -> "Calibrating..."
            !_isCalibrated.value -> "Calibration required"
            else -> "Compass ready"
        }
    }
    
    /**
     * Get compass status color
     */
    fun getCompassStatusColor(): String {
        return when {
            _error.value != null -> "red"
            !compassService.areSensorsAvailable() -> "red"
            _isCalibrating.value -> "orange"
            !_isCalibrated.value -> "orange"
            else -> "green"
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Get formatted heading
     */
    fun getFormattedHeading(): String {
        val heading = _currentHeading.value
        return String.format("%.1f°", heading)
    }
    
    /**
     * Get detailed compass information
     */
    fun getCompassInfo(): CompassInfo {
        val data = _compassData.value
        return CompassInfo(
            heading = _currentHeading.value,
            direction = _currentDirection.value,
            isCalibrated = _isCalibrated.value,
            calibrationQuality = getCalibrationQuality(),
            isCalibrating = _isCalibrating.value,
            status = getCompassStatus(),
            statusColor = getCompassStatusColor(),
            sensorInfo = _sensorInfo.value
        )
    }
}

/**
 * Data class for compass information
 */
data class CompassInfo(
    val heading: Float,
    val direction: String,
    val isCalibrated: Boolean,
    val calibrationQuality: Int,
    val isCalibrating: Boolean,
    val status: String,
    val statusColor: String,
    val sensorInfo: com.compasspro.service.SensorInfo?
)
