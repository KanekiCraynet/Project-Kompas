package com.compasspro.domain.model

import kotlin.math.*

/**
 * Data class untuk menyimpan data kompas yang komprehensif
 * Menggabungkan informasi sensor, lokasi, dan arah angin
 */
data class CompassData(
    val magneticHeading: Float = 0f,           // Arah magnetik dalam derajat
    val trueHeading: Float = 0f,               // Arah sebenarnya (true north)
    val magneticDeclination: Float = 0f,       // Deklinasi magnetik
    val accuracy: Float = 0f,                  // Akurasi sensor (0-360 derajat)
    val isCalibrated: Boolean = false,         // Status kalibrasi sensor
    val timestamp: Long = System.currentTimeMillis(),
    val location: LocationData? = null,        // Data lokasi pengguna
    val windData: WindData? = null,            // Data arah angin
    val sensorQuality: SensorQuality = SensorQuality.UNKNOWN
)

/**
 * Data class untuk informasi lokasi pengguna
 */
data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val address: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data class untuk informasi arah angin
 */
data class WindData(
    val direction: Float = 0f,                 // Arah angin dalam derajat
    val speed: Float = 0f,                     // Kecepatan angin dalam m/s
    val gust: Float = 0f,                      // Kecepatan hembusan maksimal
    val temperature: Float = 0f,               // Suhu udara
    val humidity: Float = 0f,                  // Kelembaban udara
    val pressure: Float = 0f,                  // Tekanan udara
    val visibility: Float = 0f,                // Jarak pandang
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Enum untuk kualitas sensor
 */
enum class SensorQuality {
    EXCELLENT,      // Akurasi tinggi, sensor terkalibrasi dengan baik
    GOOD,           // Akurasi sedang, sensor berfungsi normal
    FAIR,           // Akurasi rendah, sensor memerlukan kalibrasi
    POOR,           // Akurasi sangat rendah, sensor bermasalah
    UNKNOWN         // Status tidak diketahui
}

/**
 * Data class untuk data sensor mentah
 */
data class RawSensorData(
    val accelerometerX: Float = 0f,
    val accelerometerY: Float = 0f,
    val accelerometerZ: Float = 0f,
    val magnetometerX: Float = 0f,
    val magnetometerY: Float = 0f,
    val magnetometerZ: Float = 0f,
    val gyroscopeX: Float = 0f,
    val gyroscopeY: Float = 0f,
    val gyroscopeZ: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Extension functions untuk perhitungan kompas
 */
fun CompassData.calculateTrueHeading(): Float {
    return (magneticHeading + magneticDeclination + 360f) % 360f
}

fun CompassData.getCardinalDirection(): String {
    val heading = trueHeading
    return when {
        heading >= 337.5f || heading < 22.5f -> "Utara"
        heading >= 22.5f && heading < 67.5f -> "Timur Laut"
        heading >= 67.5f && heading < 112.5f -> "Timur"
        heading >= 112.5f && heading < 157.5f -> "Tenggara"
        heading >= 157.5f && heading < 202.5f -> "Selatan"
        heading >= 202.5f && heading < 247.5f -> "Barat Daya"
        heading >= 247.5f && heading < 292.5f -> "Barat"
        heading >= 292.5f && heading < 337.5f -> "Barat Laut"
        else -> "Tidak Diketahui"
    }
}

fun CompassData.getWindDirectionRelative(): Float {
    return windData?.let { wind ->
        (wind.direction - trueHeading + 360f) % 360f
    } ?: 0f
}

fun CompassData.getDistanceToTarget(targetLat: Double, targetLon: Double): Double {
    return location?.let { loc ->
        calculateDistance(
            loc.latitude, loc.longitude,
            targetLat, targetLon
        )
    } ?: 0.0
}

fun CompassData.getBearingToTarget(targetLat: Double, targetLon: Double): Float {
    return location?.let { loc ->
        calculateBearing(
            loc.latitude, loc.longitude,
            targetLat, targetLon
        )
    } ?: 0f
}

/**
 * Fungsi utilitas untuk perhitungan geografis
 */
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371000.0 // Radius bumi dalam meter
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c
}

private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val dLon = Math.toRadians(lon2 - lon1)
    val lat1Rad = Math.toRadians(lat1)
    val lat2Rad = Math.toRadians(lat2)
    
    val y = sin(dLon) * cos(lat2Rad)
    val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)
    
    val bearing = Math.toDegrees(atan2(y, x))
    return ((bearing + 360) % 360).toFloat()
}
