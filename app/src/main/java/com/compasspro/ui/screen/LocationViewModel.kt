package com.compasspro.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compasspro.data.model.LocationData
import com.compasspro.service.LocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for location functionality
 * Manages location state and user interactions
 */
class LocationViewModel(
    private val locationService: LocationService
) : ViewModel() {
    
    private val _locationData = MutableStateFlow<LocationData?>(null)
    val locationData: StateFlow<LocationData?> = _locationData.asStateFlow()
    
    private val _isLocationEnabled = MutableStateFlow(false)
    val isLocationEnabled: StateFlow<Boolean> = _isLocationEnabled.asStateFlow()
    
    private val _locationAccuracy = MutableStateFlow(0f)
    val locationAccuracy: StateFlow<Float> = _locationAccuracy.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        observeLocationData()
    }
    
    private fun observeLocationData() {
        viewModelScope.launch {
            locationService.locationData.collect { data ->
                _locationData.value = data
            }
        }
        
        viewModelScope.launch {
            locationService.isLocationEnabled.collect { enabled ->
                _isLocationEnabled.value = enabled
            }
        }
        
        viewModelScope.launch {
            locationService.locationAccuracy.collect { accuracy ->
                _locationAccuracy.value = accuracy
            }
        }
        
        viewModelScope.launch {
            locationService.isSearching.collect { searching ->
                _isSearching.value = searching
            }
        }
    }
    
    /**
     * Start location updates
     */
    fun startLocationUpdates() {
        if (!locationService.hasLocationPermission()) {
            _error.value = "Location permission is required"
            return
        }
        
        if (!_isLocationEnabled.value) {
            _error.value = "Location services are disabled"
            return
        }
        
        _error.value = null
        locationService.startLocationUpdates()
    }
    
    /**
     * Stop location updates
     */
    fun stopLocationUpdates() {
        locationService.stopLocationUpdates()
    }
    
    /**
     * Get current location
     */
    fun getCurrentLocation(): LocationData? {
        return _locationData.value
    }
    
    /**
     * Get location accuracy level
     */
    fun getLocationAccuracyLevel(): String {
        return locationService.getLocationAccuracyLevel()
    }
    
    /**
     * Get location accuracy color
     */
    fun getLocationAccuracyColor(): String {
        return locationService.getLocationAccuracyColor()
    }
    
    /**
     * Calculate distance to target location
     */
    fun distanceTo(latitude: Double, longitude: Double): Float? {
        return locationService.distanceTo(latitude, longitude)
    }
    
    /**
     * Calculate bearing to target location
     */
    fun bearingTo(latitude: Double, longitude: Double): Float? {
        return locationService.bearingTo(latitude, longitude)
    }
    
    /**
     * Get location status message
     */
    fun getLocationStatus(): String {
        return when {
            _error.value != null -> _error.value!!
            !locationService.hasLocationPermission() -> "Permission required"
            !_isLocationEnabled.value -> "Location disabled"
            _isSearching.value -> "Searching for location..."
            _locationData.value == null -> "No location data"
            else -> "Location found"
        }
    }
    
    /**
     * Get location status color
     */
    fun getLocationStatusColor(): String {
        return when {
            _error.value != null -> "red"
            !locationService.hasLocationPermission() -> "red"
            !_isLocationEnabled.value -> "red"
            _isSearching.value -> "orange"
            _locationData.value == null -> "orange"
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
     * Get formatted coordinates
     */
    fun getFormattedCoordinates(): String {
        return _locationData.value?.getFormattedCoordinates() ?: "No location"
    }
    
    /**
     * Get formatted altitude
     */
    fun getFormattedAltitude(): String {
        return _locationData.value?.getFormattedAltitude() ?: "N/A"
    }
    
    /**
     * Get formatted speed
     */
    fun getFormattedSpeed(): String {
        return _locationData.value?.getFormattedSpeed() ?: "0.0 m/s"
    }
    
    /**
     * Get magnetic declination for current location
     */
    fun getMagneticDeclination(): Float {
        val location = _locationData.value ?: return 0f
        return LocationData.getMagneticDeclination(location.latitude, location.longitude)
    }
    
    /**
     * Get detailed location information
     */
    fun getLocationInfo(): LocationInfo {
        val data = _locationData.value
        return LocationInfo(
            locationData = data,
            isEnabled = _isLocationEnabled.value,
            accuracy = _locationAccuracy.value,
            accuracyLevel = getLocationAccuracyLevel(),
            accuracyColor = getLocationAccuracyColor(),
            isSearching = _isSearching.value,
            status = getLocationStatus(),
            statusColor = getLocationStatusColor(),
            hasPermission = locationService.hasLocationPermission(),
            magneticDeclination = getMagneticDeclination()
        )
    }
}

/**
 * Data class for location information
 */
data class LocationInfo(
    val locationData: LocationData?,
    val isEnabled: Boolean,
    val accuracy: Float,
    val accuracyLevel: String,
    val accuracyColor: String,
    val isSearching: Boolean,
    val status: String,
    val statusColor: String,
    val hasPermission: Boolean,
    val magneticDeclination: Float
)
