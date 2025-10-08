package com.compasspro.utils

/**
 * Konfigurasi aplikasi CompassPro
 * Berisi konstanta dan pengaturan yang digunakan di seluruh aplikasi
 */
object Config {
    
    // API Configuration
    const val WEATHER_API_BASE_URL = "https://api.openweathermap.org/data/2.5/"
    const val WEATHER_API_KEY = "c1d29d5134907393e360ae999e85fa0f" // Ganti dengan API key yang valid
    
    // Cache Configuration
    const val CACHE_VALIDITY_DURATION_MS = 10 * 60 * 1000L // 10 menit
    const val MAX_CACHE_SIZE = 100
    
    // Sensor Configuration
    const val SENSOR_UPDATE_INTERVAL_MS = 100L // 100ms
    const val CALIBRATION_SAMPLE_SIZE = 50
    const val MIN_CALIBRATION_SAMPLES = 10
    
    // Location Configuration
    const val LOCATION_UPDATE_INTERVAL_MS = 1000L // 1 detik
    const val LOCATION_UPDATE_DISTANCE_M = 1f // 1 meter
    const val NETWORK_UPDATE_INTERVAL_MS = 2000L // 2 detik
    const val NETWORK_UPDATE_DISTANCE_M = 5f // 5 meter
    
    // UI Configuration
    const val COMPASS_ANIMATION_DURATION_MS = 500
    const val PULSE_ANIMATION_DURATION_MS = 2000
    const val ERROR_DISPLAY_DURATION_MS = 5000
    
    // Accuracy Thresholds
    const val EXCELLENT_ACCURACY_THRESHOLD = 0.1f
    const val GOOD_ACCURACY_THRESHOLD = 0.3f
    const val FAIR_ACCURACY_THRESHOLD = 0.5f
    
    // Magnetic Declination for Indonesia (approximate)
    const val INDONESIA_MAGNETIC_DECLINATION = 0f // -1° to +1° depending on location
    
    // Notification Configuration
    const val NOTIFICATION_PRIORITY = android.app.NotificationManager.IMPORTANCE_LOW
    const val NOTIFICATION_CATEGORY = "service"
    
    // Error Messages
    const val ERROR_SENSOR_UNAVAILABLE = "Sensor kompas tidak tersedia"
    const val ERROR_LOCATION_PERMISSION = "Permission lokasi diperlukan"
    const val ERROR_NETWORK_UNAVAILABLE = "Koneksi jaringan tidak tersedia"
    const val ERROR_API_LIMIT_EXCEEDED = "Batas API terlampaui"
    const val ERROR_CALIBRATION_FAILED = "Kalibrasi sensor gagal"
    
    // Success Messages
    const val SUCCESS_CALIBRATION_COMPLETE = "Kalibrasi selesai"
    const val SUCCESS_LOCATION_ACQUIRED = "Lokasi berhasil diperoleh"
    const val SUCCESS_WIND_DATA_LOADED = "Data angin berhasil dimuat"
    
    // Default Values
    const val DEFAULT_TEMPERATURE = 25f
    const val DEFAULT_HUMIDITY = 60f
    const val DEFAULT_PRESSURE = 1013f
    const val DEFAULT_VISIBILITY = 10000f
    const val DEFAULT_WIND_SPEED = 0f
    const val DEFAULT_WIND_DIRECTION = 0f
    
    // Units
    const val METERS_PER_SECOND = "m/s"
    const val KILOMETERS_PER_HOUR = "km/h"
    const val CELSIUS = "°C"
    const val FAHRENHEIT = "°F"
    const val HECTOPASCAL = "hPa"
    const val PERCENT = "%"
    const val METERS = "m"
    const val KILOMETERS = "km"
    const val DEGREES = "°"
}
