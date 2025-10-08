package com.compasspro.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.ActivityCompat
import com.compasspro.data.model.LocationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Service for managing location data
 * Provides high-accuracy GPS location updates
 */
class LocationService : Service(), LocationListener {
    
    private lateinit var locationManager: LocationManager
    private val binder = LocationBinder()
    
    private val _locationData = MutableStateFlow<LocationData?>(null)
    val locationData: StateFlow<LocationData?> = _locationData.asStateFlow()
    
    private val _isLocationEnabled = MutableStateFlow(false)
    val isLocationEnabled: StateFlow<Boolean> = _isLocationEnabled.asStateFlow()
    
    private val _locationAccuracy = MutableStateFlow(0f)
    val locationAccuracy: StateFlow<Float> = _locationAccuracy.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    // Location update parameters
    private val minTime: Long = 1000 // 1 second
    private val minDistance: Float = 1f // 1 meter
    
    inner class LocationBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }
    
    override fun onCreate() {
        super.onCreate()
        initializeLocationManager()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    private fun initializeLocationManager() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        checkLocationAvailability()
    }
    
    private fun checkLocationAvailability() {
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        
        _isLocationEnabled.value = isGpsEnabled || isNetworkEnabled
    }
    
    /**
     * Start location updates
     */
    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        
        _isSearching.value = true
        
        try {
            // Try GPS first for highest accuracy
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime,
                    minDistance,
                    this
                )
            }
            
            // Also use network provider as backup
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTime * 2, // Less frequent for network
                    minDistance * 5, // Less precise for network
                    this
                )
            }
            
            // Get last known location immediately
            getLastKnownLocation()
            
        } catch (e: SecurityException) {
            // Permission not granted
            _isSearching.value = false
        }
    }
    
    /**
     * Stop location updates
     */
    fun stopLocationUpdates() {
        locationManager.removeUpdates(this)
        _isSearching.value = false
    }
    
    /**
     * Get last known location
     */
    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        
        try {
            // Try GPS first
            val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (gpsLocation != null && isLocationRecent(gpsLocation)) {
                updateLocation(gpsLocation)
                return
            }
            
            // Fallback to network
            val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (networkLocation != null && isLocationRecent(networkLocation)) {
                updateLocation(networkLocation)
            }
            
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
    
    /**
     * Check if location is recent (within 5 minutes)
     */
    private fun isLocationRecent(location: Location): Boolean {
        val currentTime = System.currentTimeMillis()
        val locationTime = location.time
        val timeDifference = currentTime - locationTime
        return timeDifference < 5 * 60 * 1000 // 5 minutes
    }
    
    /**
     * Update location data
     */
    private fun updateLocation(location: Location) {
        val locationData = LocationData.fromLocation(location)
        _locationData.value = locationData
        _locationAccuracy.value = location.accuracy
        _isSearching.value = false
    }
    
    override fun onLocationChanged(location: Location) {
        updateLocation(location)
    }
    
    override fun onProviderEnabled(provider: String) {
        checkLocationAvailability()
    }
    
    override fun onProviderDisabled(provider: String) {
        checkLocationAvailability()
    }
    
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // Handle status changes if needed
    }
    
    /**
     * Get current location
     */
    fun getCurrentLocation(): LocationData? {
        return _locationData.value
    }
    
    /**
     * Check if location permission is granted
     */
    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Get location accuracy level
     */
    fun getLocationAccuracyLevel(): String {
        val accuracy = _locationAccuracy.value
        return when {
            accuracy <= 5f -> "Excellent"
            accuracy <= 10f -> "Good"
            accuracy <= 20f -> "Fair"
            accuracy <= 50f -> "Poor"
            else -> "Very Poor"
        }
    }
    
    /**
     * Get location accuracy color
     */
    fun getLocationAccuracyColor(): String {
        val accuracy = _locationAccuracy.value
        return when {
            accuracy <= 5f -> "green"
            accuracy <= 10f -> "blue"
            accuracy <= 20f -> "orange"
            accuracy <= 50f -> "red"
            else -> "dark_red"
        }
    }
    
    /**
     * Calculate distance to target location
     */
    fun distanceTo(latitude: Double, longitude: Double): Float? {
        val currentLocation = _locationData.value ?: return null
        
        val results = FloatArray(1)
        Location.distanceBetween(
            currentLocation.latitude,
            currentLocation.longitude,
            latitude,
            longitude,
            results
        )
        
        return results[0]
    }
    
    /**
     * Calculate bearing to target location
     */
    fun bearingTo(latitude: Double, longitude: Double): Float? {
        val currentLocation = _locationData.value ?: return null
        
        val results = FloatArray(2)
        Location.distanceBetween(
            currentLocation.latitude,
            currentLocation.longitude,
            latitude,
            longitude,
            results
        )
        
        return results[1]
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }
}
