package com.compasspro.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Information card component for displaying various data
 */
@Composable
fun InfoCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    statusColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = statusColor ?: MaterialTheme.colorScheme.primary,
        animationSpec = tween(300),
        label = "card_color"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    animatedColor.copy(alpha = 0.1f),
                                    animatedColor.copy(alpha = 0.2f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = animatedColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Location information card
 */
@Composable
fun LocationInfoCard(
    coordinates: String,
    altitude: String,
    accuracy: String,
    speed: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        InfoCard(
            title = "Coordinates",
            value = coordinates,
            icon = Icons.Default.LocationOn,
            statusColor = LocationAccurate
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoCard(
                title = "Altitude",
                value = altitude,
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Height,
                statusColor = Info
            )
            
            InfoCard(
                title = "Accuracy",
                value = accuracy,
                modifier = Modifier.weight(1f),
                icon = Icons.Default.GpsFixed,
                statusColor = Success
            )
        }
        
        InfoCard(
            title = "Speed",
            value = speed,
            icon = Icons.Default.Speed,
            statusColor = Warning
        )
    }
}

/**
 * Weather information card
 */
@Composable
fun WeatherInfoCard(
    temperature: String,
    windSpeed: String,
    windDirection: String,
    humidity: String,
    pressure: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        InfoCard(
            title = "Temperature",
            value = temperature,
            icon = Icons.Default.Thermostat,
            statusColor = Info
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoCard(
                title = "Wind Speed",
                value = windSpeed,
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Air,
                statusColor = WindModerate
            )
            
            InfoCard(
                title = "Wind Direction",
                value = windDirection,
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Navigation,
                statusColor = WindModerate
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoCard(
                title = "Humidity",
                value = humidity,
                modifier = Modifier.weight(1f),
                icon = Icons.Default.WaterDrop,
                statusColor = Info
            )
            
            InfoCard(
                title = "Pressure",
                value = pressure,
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Speed,
                statusColor = Info
            )
        }
    }
}

/**
 * Status indicator card
 */
@Composable
fun StatusIndicatorCard(
    compassStatus: String,
    compassStatusColor: String,
    locationStatus: String,
    locationStatusColor: String,
    weatherStatus: String,
    weatherStatusColor: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "System Status",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            CompassStatusIndicator(
                status = "Compass: $compassStatus",
                statusColor = compassStatusColor
            )
            
            CompassStatusIndicator(
                status = "Location: $locationStatus",
                statusColor = locationStatusColor
            )
            
            CompassStatusIndicator(
                status = "Weather: $weatherStatus",
                statusColor = weatherStatusColor
            )
        }
    }
}

/**
 * Action button component
 */
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
