package com.compasspro.data.model

/**
 * Data class representing weather information
 */
data class WeatherData(
    val temperature: Float,
    val humidity: Float,
    val pressure: Float,
    val windSpeed: Float,
    val windDirection: Float,
    val windGust: Float = 0f,
    val visibility: Float = 0f,
    val uvIndex: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val location: String = "",
    val description: String = "",
    val icon: String = ""
) {
    
    /**
     * Get wind direction as string
     */
    fun getWindDirectionString(): String {
        return when {
            windDirection >= 337.5f || windDirection < 22.5f -> "N"
            windDirection >= 22.5f && windDirection < 67.5f -> "NE"
            windDirection >= 67.5f && windDirection < 112.5f -> "E"
            windDirection >= 112.5f && windDirection < 157.5f -> "SE"
            windDirection >= 157.5f && windDirection < 202.5f -> "S"
            windDirection >= 202.5f && windDirection < 247.5f -> "SW"
            windDirection >= 247.5f && windDirection < 292.5f -> "W"
            windDirection >= 292.5f && windDirection < 337.5f -> "NW"
            else -> "N"
        }
    }
    
    /**
     * Get wind speed category
     */
    fun getWindSpeedCategory(): WindSpeedCategory {
        return when {
            windSpeed < 0.5f -> WindSpeedCategory.CALM
            windSpeed < 3.3f -> WindSpeedCategory.LIGHT_AIR
            windSpeed < 5.5f -> WindSpeedCategory.LIGHT_BREEZE
            windSpeed < 7.9f -> WindSpeedCategory.GENTLE_BREEZE
            windSpeed < 10.7f -> WindSpeedCategory.MODERATE_BREEZE
            windSpeed < 13.8f -> WindSpeedCategory.FRESH_BREEZE
            windSpeed < 17.1f -> WindSpeedCategory.STRONG_BREEZE
            windSpeed < 20.7f -> WindSpeedCategory.NEAR_GALE
            windSpeed < 24.4f -> WindSpeedCategory.GALE
            windSpeed < 28.4f -> WindSpeedCategory.STRONG_GALE
            windSpeed < 32.6f -> WindSpeedCategory.STORM
            else -> WindSpeedCategory.VIOLENT_STORM
        }
    }
    
    /**
     * Get wind speed color based on category
     */
    fun getWindSpeedColor(): String {
        return when (getWindSpeedCategory()) {
            WindSpeedCategory.CALM, WindSpeedCategory.LIGHT_AIR -> "green"
            WindSpeedCategory.LIGHT_BREEZE, WindSpeedCategory.GENTLE_BREEZE -> "light_green"
            WindSpeedCategory.MODERATE_BREEZE, WindSpeedCategory.FRESH_BREEZE -> "yellow"
            WindSpeedCategory.STRONG_BREEZE, WindSpeedCategory.NEAR_GALE -> "orange"
            WindSpeedCategory.GALE, WindSpeedCategory.STRONG_GALE -> "red"
            WindSpeedCategory.STORM, WindSpeedCategory.VIOLENT_STORM -> "dark_red"
        }
    }
    
    /**
     * Format temperature as string
     */
    fun getFormattedTemperature(unit: TemperatureUnit = TemperatureUnit.CELSIUS): String {
        val temp = when (unit) {
            TemperatureUnit.CELSIUS -> temperature
            TemperatureUnit.FAHRENHEIT -> temperature * 9f / 5f + 32f
            TemperatureUnit.KELVIN -> temperature + 273.15f
        }
        
        val unitSymbol = when (unit) {
            TemperatureUnit.CELSIUS -> "°C"
            TemperatureUnit.FAHRENHEIT -> "°F"
            TemperatureUnit.KELVIN -> "K"
        }
        
        return String.format("%.1f%s", temp, unitSymbol)
    }
    
    /**
     * Format wind speed as string
     */
    fun getFormattedWindSpeed(unit: WindSpeedUnit = WindSpeedUnit.METERS_PER_SECOND): String {
        val speed = when (unit) {
            WindSpeedUnit.METERS_PER_SECOND -> windSpeed
            WindSpeedUnit.KILOMETERS_PER_HOUR -> windSpeed * 3.6f
            WindSpeedUnit.MILES_PER_HOUR -> windSpeed * 2.237f
            WindSpeedUnit.KNOTS -> windSpeed * 1.944f
        }
        
        val unitSymbol = when (unit) {
            WindSpeedUnit.METERS_PER_SECOND -> "m/s"
            WindSpeedUnit.KILOMETERS_PER_HOUR -> "km/h"
            WindSpeedUnit.MILES_PER_HOUR -> "mph"
            WindSpeedUnit.KNOTS -> "kt"
        }
        
        return String.format("%.1f %s", speed, unitSymbol)
    }
    
    /**
     * Format pressure as string
     */
    fun getFormattedPressure(unit: PressureUnit = PressureUnit.HECTOPASCAL): String {
        val pressure = when (unit) {
            PressureUnit.HECTOPASCAL -> this.pressure
            PressureUnit.MILLIBAR -> this.pressure
            PressureUnit.INCHES_OF_MERCURY -> this.pressure * 0.02953f
            PressureUnit.PASCAL -> this.pressure * 100f
        }
        
        val unitSymbol = when (unit) {
            PressureUnit.HECTOPASCAL -> "hPa"
            PressureUnit.MILLIBAR -> "mb"
            PressureUnit.INCHES_OF_MERCURY -> "inHg"
            PressureUnit.PASCAL -> "Pa"
        }
        
        return String.format("%.1f %s", pressure, unitSymbol)
    }
    
    /**
     * Format humidity as string
     */
    fun getFormattedHumidity(): String {
        return String.format("%.0f%%", humidity)
    }
    
    /**
     * Format visibility as string
     */
    fun getFormattedVisibility(): String {
        return when {
            visibility < 1000f -> String.format("%.0f m", visibility)
            else -> String.format("%.1f km", visibility / 1000f)
        }
    }
}

/**
 * Wind speed categories based on Beaufort scale
 */
enum class WindSpeedCategory {
    CALM,           // 0-0.5 m/s
    LIGHT_AIR,      // 0.5-3.3 m/s
    LIGHT_BREEZE,   // 3.3-5.5 m/s
    GENTLE_BREEZE,  // 5.5-7.9 m/s
    MODERATE_BREEZE, // 7.9-10.7 m/s
    FRESH_BREEZE,   // 10.7-13.8 m/s
    STRONG_BREEZE,  // 13.8-17.1 m/s
    NEAR_GALE,      // 17.1-20.7 m/s
    GALE,           // 20.7-24.4 m/s
    STRONG_GALE,    // 24.4-28.4 m/s
    STORM,          // 28.4-32.6 m/s
    VIOLENT_STORM   // >32.6 m/s
}

/**
 * Temperature units
 */
enum class TemperatureUnit {
    CELSIUS, FAHRENHEIT, KELVIN
}

/**
 * Wind speed units
 */
enum class WindSpeedUnit {
    METERS_PER_SECOND, KILOMETERS_PER_HOUR, MILES_PER_HOUR, KNOTS
}

/**
 * Pressure units
 */
enum class PressureUnit {
    HECTOPASCAL, MILLIBAR, INCHES_OF_MERCURY, PASCAL
}
