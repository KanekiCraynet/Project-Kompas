package com.compasspro.data.model

import kotlin.math.*

/**
 * Data class representing compass sensor readings
 */
data class CompassData(
    val magneticField: FloatArray,
    val acceleration: FloatArray,
    val rotation: FloatArray,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Calculate magnetic heading from sensor data
     */
    fun getMagneticHeading(): Float {
        val x = magneticField[0]
        val y = magneticField[1]
        val z = magneticField[2]
        
        // Calculate heading using magnetometer data
        var heading = Math.toDegrees(atan2(y, x).toDouble()).toFloat()
        
        // Normalize to 0-360 degrees
        if (heading < 0) heading += 360f
        
        return heading
    }
    
    /**
     * Calculate true heading by applying magnetic declination
     */
    fun getTrueHeading(magneticDeclination: Float): Float {
        val magneticHeading = getMagneticHeading()
        var trueHeading = magneticHeading + magneticDeclination
        
        // Normalize to 0-360 degrees
        if (trueHeading < 0) trueHeading += 360f
        if (trueHeading >= 360) trueHeading -= 360f
        
        return trueHeading
    }
    
    /**
     * Get compass direction as string
     */
    fun getDirectionString(): String {
        val heading = getMagneticHeading()
        return when {
            heading >= 337.5f || heading < 22.5f -> "N"
            heading >= 22.5f && heading < 67.5f -> "NE"
            heading >= 67.5f && heading < 112.5f -> "E"
            heading >= 112.5f && heading < 157.5f -> "SE"
            heading >= 157.5f && heading < 202.5f -> "S"
            heading >= 202.5f && heading < 247.5f -> "SW"
            heading >= 247.5f && heading < 292.5f -> "W"
            heading >= 292.5f && heading < 337.5f -> "NW"
            else -> "N"
        }
    }
    
    /**
     * Check if compass is calibrated
     */
    fun isCalibrated(): Boolean {
        val magnitude = sqrt(
            magneticField[0] * magneticField[0] +
            magneticField[1] * magneticField[1] +
            magneticField[2] * magneticField[2]
        )
        
        // Typical Earth's magnetic field strength is around 25-65 microtesla
        return magnitude in 20f..70f
    }
    
    /**
     * Get calibration quality (0-100)
     */
    fun getCalibrationQuality(): Int {
        val magnitude = sqrt(
            magneticField[0] * magneticField[0] +
            magneticField[1] * magneticField[1] +
            magneticField[2] * magneticField[2]
        )
        
        return when {
            magnitude < 20f -> 0
            magnitude > 70f -> 0
            magnitude in 25f..65f -> 100
            else -> ((65f - abs(magnitude - 45f)) / 20f * 100).toInt().coerceIn(0, 100)
        }
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as CompassData
        
        if (!magneticField.contentEquals(other.magneticField)) return false
        if (!acceleration.contentEquals(other.acceleration)) return false
        if (!rotation.contentEquals(other.rotation)) return false
        if (timestamp != other.timestamp) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = magneticField.contentHashCode()
        result = 31 * result + acceleration.contentHashCode()
        result = 31 * result + rotation.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}
