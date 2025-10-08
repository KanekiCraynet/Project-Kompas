package com.compasspro.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.compasspro.ui.component.*

/**
 * Main compass screen with all components
 */
@Composable
fun CompassScreen(
    compassViewModel: CompassViewModel,
    locationViewModel: LocationViewModel,
    weatherViewModel: WeatherViewModel,
    onRequestPermissions: () -> Unit,
    onStartServices: () -> Unit
) {
    val compassData by compassViewModel.compassData.collectAsStateWithLifecycle()
    val locationData by locationViewModel.locationData.collectAsStateWithLifecycle()
    val weatherData by weatherViewModel.weatherData.collectAsStateWithLifecycle()
    
    val compassInfo by compassViewModel.compassInfo.collectAsStateWithLifecycle()
    val locationInfo by locationViewModel.locationInfo.collectAsStateWithLifecycle()
    val weatherInfo by weatherViewModel.weatherInfo.collectAsStateWithLifecycle()
    
    // Animated background gradient
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background
                    ),
                    center = androidx.compose.ui.geometry.Offset(
                        x = 0.5f + 0.3f * kotlin.math.sin(gradientOffset * 2 * kotlin.math.PI).toFloat(),
                        y = 0.5f + 0.3f * kotlin.math.cos(gradientOffset * 2 * kotlin.math.PI).toFloat()
                    ),
                    radius = 1000f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            CompassHeader(
                compassInfo = compassInfo,
                locationInfo = locationInfo,
                weatherInfo = weatherInfo
            )
            
            // Main compass
            CompassComponent(
                heading = compassInfo.heading,
                isCalibrated = compassInfo.isCalibrated,
                calibrationProgress = compassInfo.calibrationQuality,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // Compass controls
            CompassControls(
                compassViewModel = compassViewModel,
                onRequestPermissions = onRequestPermissions
            )
            
            // Location information
            if (locationData != null) {
                LocationInfoCard(
                    coordinates = locationViewModel.getFormattedCoordinates(),
                    altitude = locationViewModel.getFormattedAltitude(),
                    accuracy = locationInfo.accuracyLevel,
                    speed = locationViewModel.getFormattedSpeed()
                )
            }
            
            // Weather information
            if (weatherData != null) {
                WeatherInfoCard(
                    temperature = weatherViewModel.getFormattedTemperature(),
                    windSpeed = weatherViewModel.getFormattedWindSpeed(),
                    windDirection = weatherViewModel.getWindDirectionString(),
                    humidity = weatherViewModel.getFormattedHumidity(),
                    pressure = weatherViewModel.getFormattedPressure()
                )
            }
            
            // System status
            StatusIndicatorCard(
                compassStatus = compassInfo.status,
                compassStatusColor = compassInfo.statusColor,
                locationStatus = locationInfo.status,
                locationStatusColor = locationInfo.statusColor,
                weatherStatus = weatherInfo.status,
                weatherStatusColor = weatherInfo.statusColor
            )
            
            // Action buttons
            ActionButtons(
                compassViewModel = compassViewModel,
                locationViewModel = locationViewModel,
                weatherViewModel = weatherViewModel,
                locationData = locationData
            )
        }
    }
}

/**
 * Compass header with title and status
 */
@Composable
private fun CompassHeader(
    compassInfo: CompassInfo,
    locationInfo: LocationInfo,
    weatherInfo: WeatherInfo
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Compass Pro",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Professional Navigation & Weather",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Quick status indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CompassStatusIndicator(
                status = "Compass",
                statusColor = compassInfo.statusColor
            )
            
            CompassStatusIndicator(
                status = "Location",
                statusColor = locationInfo.statusColor
            )
            
            CompassStatusIndicator(
                status = "Weather",
                statusColor = weatherInfo.statusColor
            )
        }
    }
}

/**
 * Compass controls
 */
@Composable
private fun CompassControls(
    compassViewModel: CompassViewModel,
    onRequestPermissions: () -> Unit
) {
    val isCalibrated by compassViewModel.isCalibrated.collectAsStateWithLifecycle()
    val isCalibrating by compassViewModel.isCalibrating.collectAsStateWithLifecycle()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!isCalibrated && !isCalibrating) {
            ActionButton(
                text = "Calibrate",
                onClick = { compassViewModel.startCalibration() },
                icon = Icons.Default.Tune,
                modifier = Modifier.weight(1f)
            )
        }
        
        if (isCalibrating) {
            ActionButton(
                text = "Calibrating...",
                onClick = { },
                icon = Icons.Default.Refresh,
                enabled = false,
                modifier = Modifier.weight(1f)
            )
        }
        
        ActionButton(
            text = "Permissions",
            onClick = onRequestPermissions,
            icon = Icons.Default.Security,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Action buttons
 */
@Composable
private fun ActionButtons(
    compassViewModel: CompassViewModel,
    locationViewModel: LocationViewModel,
    weatherViewModel: WeatherViewModel,
    locationData: com.compasspro.data.model.LocationData?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                text = "Refresh Location",
                onClick = { locationViewModel.startLocationUpdates() },
                icon = Icons.Default.Refresh,
                modifier = Modifier.weight(1f)
            )
            
            ActionButton(
                text = "Refresh Weather",
                onClick = {
                    locationData?.let { data ->
                        weatherViewModel.refreshWeatherData(data.latitude, data.longitude)
                    }
                },
                icon = Icons.Default.CloudSync,
                enabled = locationData != null,
                modifier = Modifier.weight(1f)
            )
        }
        
        ActionButton(
            text = "Settings",
            onClick = { /* TODO: Navigate to settings */ },
            icon = Icons.Default.Settings,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
