package com.compasspro.data.model

import android.location.Location

/**
 * Data class representing location information
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val provider: String = "unknown"
) {
    
    /**
     * Convert to Android Location object
     */
    fun toLocation(): Location {
        val location = Location(provider)
        location.latitude = latitude
        location.longitude = longitude
        location.altitude = altitude
        location.accuracy = accuracy
        location.speed = speed
        location.bearing = bearing
        location.time = timestamp
        return location
    }
    
    /**
     * Calculate distance to another location in meters
     */
    fun distanceTo(other: LocationData): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            latitude, longitude,
            other.latitude, other.longitude,
            results
        )
        return results[0]
    }
    
    /**
     * Calculate bearing to another location
     */
    fun bearingTo(other: LocationData): Float {
        val results = FloatArray(2)
        Location.distanceBetween(
            latitude, longitude,
            other.latitude, other.longitude,
            results
        )
        return results[1]
    }
    
    /**
     * Get accuracy level as string
     */
    fun getAccuracyLevel(): String {
        return when {
            accuracy <= 5f -> "Excellent"
            accuracy <= 10f -> "Good"
            accuracy <= 20f -> "Fair"
            accuracy <= 50f -> "Poor"
            else -> "Very Poor"
        }
    }
    
    /**
     * Get accuracy color based on level
     */
    fun getAccuracyColor(): String {
        return when {
            accuracy <= 5f -> "green"
            accuracy <= 10f -> "blue"
            accuracy <= 20f -> "orange"
            accuracy <= 50f -> "red"
            else -> "dark_red"
        }
    }
    
    /**
     * Format coordinates as string
     */
    fun getFormattedCoordinates(): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lonDir = if (longitude >= 0) "E" else "W"
        
        return String.format(
            "%.6f° %s, %.6f° %s",
            kotlin.math.abs(latitude), latDir,
            kotlin.math.abs(longitude), lonDir
        )
    }
    
    /**
     * Format altitude as string
     */
    fun getFormattedAltitude(): String {
        return String.format("%.1f m", altitude)
    }
    
    /**
     * Format speed as string
     */
    fun getFormattedSpeed(): String {
        return when {
            speed < 1f -> String.format("%.1f m/s", speed)
            else -> String.format("%.1f km/h", speed * 3.6f)
        }
    }
    
    companion object {
        /**
         * Create from Android Location object
         */
        fun fromLocation(location: Location): LocationData {
            return LocationData(
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                accuracy = location.accuracy,
                speed = location.speed,
                bearing = location.bearing,
                timestamp = location.time,
                provider = location.provider ?: "unknown"
            )
        }
        
        /**
         * Get magnetic declination for given coordinates
         * This is a simplified calculation - in production, use a proper magnetic model
         */
        fun getMagneticDeclination(latitude: Double, longitude: Double): Float {
            // Simplified magnetic declination calculation
            // In production, use World Magnetic Model (WMM) or IGRF
            val year = 2024
            val lat = Math.toRadians(latitude)
            val lon = Math.toRadians(longitude)
            
            // Basic approximation (not accurate for production use)
            val declination = Math.toDegrees(
                Math.atan2(
                    Math.sin(lon) * Math.cos(lat),
                    Math.cos(lat) * Math.cos(lon) + Math.sin(lat) * Math.sin(lon)
                )
            ).toFloat()
            
            return declination
        }
    }
}
