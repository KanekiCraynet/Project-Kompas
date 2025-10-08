package com.compasspro.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compasspro.domain.model.CompassData
import com.compasspro.domain.model.SensorQuality
import com.compasspro.ui.theme.*
import com.compasspro.utils.Config
import kotlin.math.*

/**
 * Screen utama untuk menampilkan kompas dengan UI yang modern dan responsif
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassScreen(
    compassData: CompassData,
    isLoading: Boolean,
    errorMessage: String?,
    onCalibrate: () -> Unit,
    onRefresh: () -> Unit
) {
    val density = LocalDensity.current
    
    // Animasi untuk rotasi kompas
    val rotationAnimation by animateFloatAsState(
        targetValue = -compassData.trueHeading,
        animationSpec = tween(
            durationMillis = Config.COMPASS_ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing
        ),
        label = "compass_rotation"
    )
    
    // Animasi untuk pulsing effect
    val pulseAnimation by animateFloatAsState(
        targetValue = if (compassData.sensorQuality == SensorQuality.EXCELLENT) 1f else 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(Config.PULSE_ANIMATION_DURATION_MS),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_animation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(16.dp)
    ) {
        // Header dengan informasi status
        CompassHeader(
            compassData = compassData,
            isLoading = isLoading,
            onRefresh = onRefresh
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Kompas utama
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .align(Alignment.CenterHorizontally)
        ) {
            CompassDial(
                rotation = rotationAnimation,
                pulseAlpha = pulseAnimation,
                compassData = compassData
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Informasi detail
        CompassInfo(
            compassData = compassData,
            onCalibrate = onCalibrate
        )
        
        // Error message
        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = ErrorColor.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ErrorColor
                )
            }
        }
    }
}

@Composable
private fun CompassHeader(
    compassData: CompassData,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "CompassPro",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = compassData.getCardinalDirection(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status indicator
            SensorQualityIndicator(compassData.sensorQuality)
            
            // Refresh button
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SensorQualityIndicator(quality: SensorQuality) {
    val (color, icon) = when (quality) {
        SensorQuality.EXCELLENT -> SuccessColor to Icons.Default.CheckCircle
        SensorQuality.GOOD -> InfoColor to Icons.Default.Info
        SensorQuality.FAIR -> WarningColor to Icons.Default.Warning
        SensorQuality.POOR -> ErrorColor to Icons.Default.Error
        SensorQuality.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant to Icons.Default.Help
    }
    
    Icon(
        imageVector = icon,
        contentDescription = "Sensor Quality",
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun CompassDial(
    rotation: Float,
    pulseAlpha: Float,
    compassData: CompassData
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .rotate(rotation)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = minOf(size.width, size.height) / 2 - 20.dp.toPx()
        
        // Background circle
        drawCircle(
            color = MaterialTheme.colorScheme.surface,
            radius = radius,
            center = center,
            style = Stroke(width = 4.dp.toPx())
        )
        
        // Compass markings
        drawCompassMarkings(center, radius)
        
        // Cardinal directions
        drawCardinalDirections(center, radius)
        
        // Needle
        drawCompassNeedle(center, radius, pulseAlpha)
        
        // Center dot
        drawCircle(
            color = CompassRed,
            radius = 8.dp.toPx(),
            center = center
        )
    }
}

private fun DrawScope.drawCompassMarkings(
    center: Offset,
    radius: Float
) {
    val strokeWidth = 2.dp.toPx()
    val markingLength = 8.dp.toPx()
    
    // Draw degree markings
    for (i in 0 until 360 step 5) {
        val angle = Math.toRadians(i.toDouble())
        val startRadius = if (i % 30 == 0) radius - 15.dp.toPx() else radius - 8.dp.toPx()
        val endRadius = radius
        
        val startX = center.x + (startRadius * cos(angle)).toFloat()
        val startY = center.y + (startRadius * sin(angle)).toFloat()
        val endX = center.x + (endRadius * cos(angle)).toFloat()
        val endY = center.y + (endRadius * sin(angle)).toFloat()
        
        val color = if (i % 30 == 0) CompassRed else MaterialTheme.colorScheme.onSurfaceVariant
        val width = if (i % 30 == 0) strokeWidth * 2 else strokeWidth
        
        drawLine(
            color = color,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = width
        )
    }
}

private fun DrawScope.drawCardinalDirections(
    center: Offset,
    radius: Float
) {
    val directions = listOf("N", "E", "S", "W")
    val angles = listOf(0f, 90f, 180f, 270f)
    
    directions.forEachIndexed { index, direction ->
        val angle = Math.toRadians(angles[index].toDouble())
        val textRadius = radius - 30.dp.toPx()
        
        val x = center.x + (textRadius * cos(angle)).toFloat()
        val y = center.y + (textRadius * sin(angle)).toFloat()
        
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = CompassRed.toArgb()
                textSize = 24.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }
            drawText(direction, x, y + 8.dp.toPx(), paint)
        }
    }
}

private fun DrawScope.drawCompassNeedle(
    center: Offset,
    radius: Float,
    pulseAlpha: Float
) {
    val needleLength = radius * 0.8f
    val needleWidth = 4.dp.toPx()
    
    // North needle (red)
    val northPath = Path().apply {
        moveTo(center.x, center.y - needleLength)
        lineTo(center.x - needleWidth, center.y)
        lineTo(center.x + needleWidth, center.y)
        close()
    }
    
    drawPath(
        path = northPath,
        color = CompassRed.copy(alpha = pulseAlpha),
        style = Fill
    )
    
    // South needle (white/light)
    val southPath = Path().apply {
        moveTo(center.x, center.y + needleLength)
        lineTo(center.x - needleWidth, center.y)
        lineTo(center.x + needleWidth, center.y)
        close()
    }
    
    drawPath(
        path = southPath,
        color = MaterialTheme.colorScheme.surface.copy(alpha = pulseAlpha),
        style = Fill
    )
}

@Composable
private fun CompassInfo(
    compassData: CompassData,
    onCalibrate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Heading information
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Arah",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${compassData.trueHeading.toInt()}°",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = compassData.getCardinalDirection(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // Location and wind info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Location card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Lokasi",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    compassData.location?.let { location ->
                        Text(
                            text = "${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = location.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2
                        )
                    } ?: Text(
                        text = "Mencari lokasi...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Wind card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Angin",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    compassData.windData?.let { wind ->
                        Text(
                            text = "${wind.direction.toInt()}°",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${String.format("%.1f", wind.speed)} m/s",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } ?: Text(
                        text = "Mencari data angin...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Calibration button
        if (!compassData.isCalibrated) {
            Button(
                onClick = onCalibrate,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarningColor
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kalibrasi Sensor")
            }
        }
    }
}
