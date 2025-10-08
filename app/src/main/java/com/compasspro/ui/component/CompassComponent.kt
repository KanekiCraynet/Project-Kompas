package com.compasspro.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

/**
 * Modern compass component with smooth animations
 */
@Composable
fun CompassComponent(
    heading: Float,
    isCalibrated: Boolean,
    calibrationProgress: Int,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    // Animation for smooth rotation
    val animatedHeading by animateFloatAsState(
        targetValue = heading,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "compass_rotation"
    )
    
    // Pulsing animation for calibration
    val pulseAnimation by animateFloatAsState(
        targetValue = if (isCalibrated) 1f else 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "compass_pulse"
    )
    
    Box(
        modifier = modifier
            .size(300.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(-animatedHeading)
        ) {
            drawCompass(
                size = size,
                isCalibrated = isCalibrated,
                calibrationProgress = calibrationProgress,
                pulseAnimation = pulseAnimation
            )
        }
        
        // Center dot
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        
        // Calibration indicator
        if (!isCalibrated) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "Calibration: $calibrationProgress%",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/**
 * Draw compass elements
 */
private fun DrawScope.drawCompass(
    size: androidx.compose.ui.geometry.Size,
    isCalibrated: Boolean,
    calibrationProgress: Int,
    pulseAnimation: Float
) {
    val center = size.center
    val radius = minOf(size.width, size.height) / 2f
    val strokeWidth = 4.dp.toPx()
    
    // Outer ring
    drawCircle(
        color = if (isCalibrated) CompassRing else CompassRing.copy(alpha = 0.5f),
        radius = radius - strokeWidth / 2,
        style = Stroke(width = strokeWidth)
    )
    
    // Direction markers
    drawDirectionMarkers(center, radius, isCalibrated, pulseAnimation)
    
    // Compass needle
    drawCompassNeedle(center, radius, isCalibrated, pulseAnimation)
    
    // Degree markers
    drawDegreeMarkers(center, radius)
}

/**
 * Draw direction markers (N, S, E, W)
 */
private fun DrawScope.drawDirectionMarkers(
    center: Offset,
    radius: Float,
    isCalibrated: Boolean,
    pulseAnimation: Float
) {
    val markerRadius = radius * 0.85f
    val directions = listOf("N", "E", "S", "W")
    val colors = listOf(CompassNorth, CompassEast, CompassSouth, CompassWest)
    
    directions.forEachIndexed { index, direction ->
        val angle = index * 90f
        val x = center.x + markerRadius * cos(Math.toRadians(angle.toDouble())).toFloat()
        val y = center.y + markerRadius * sin(Math.toRadians(angle.toDouble())).toFloat()
        
        val color = if (isCalibrated) {
            colors[index]
        } else {
            colors[index].copy(alpha = 0.5f * pulseAnimation)
        }
        
        // Direction marker circle
        drawCircle(
            color = color,
            radius = 20.dp.toPx(),
            center = Offset(x, y)
        )
        
        // Direction text
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 16.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            
            val textBounds = android.graphics.Rect()
            paint.getTextBounds(direction, 0, direction.length, textBounds)
            
            drawText(
                direction,
                x,
                y + textBounds.height() / 2f,
                paint
            )
        }
    }
}

/**
 * Draw compass needle
 */
private fun DrawScope.drawCompassNeedle(
    center: Offset,
    radius: Float,
    isCalibrated: Boolean,
    pulseAnimation: Float
) {
    val needleLength = radius * 0.7f
    val needleWidth = 8.dp.toPx()
    
    val color = if (isCalibrated) {
        CompassNeedle
    } else {
        CompassNeedle.copy(alpha = 0.7f * pulseAnimation)
    }
    
    // Needle shadow
    drawLine(
        color = Color.Black.copy(alpha = 0.3f),
        start = Offset(center.x - needleWidth / 2, center.y),
        end = Offset(center.x + needleWidth / 2, center.y - needleLength),
        strokeWidth = needleWidth,
        cap = StrokeCap.Round
    )
    
    // Main needle
    drawLine(
        color = color,
        start = Offset(center.x, center.y),
        end = Offset(center.x, center.y - needleLength),
        strokeWidth = needleWidth,
        cap = StrokeCap.Round
    )
    
    // Needle tip
    drawCircle(
        color = color,
        radius = needleWidth / 2,
        center = Offset(center.x, center.y - needleLength)
    )
}

/**
 * Draw degree markers
 */
private fun DrawScope.drawDegreeMarkers(
    center: Offset,
    radius: Float
) {
    val markerRadius = radius * 0.9f
    
    for (degree in 0..359 step 30) {
        val angle = Math.toRadians(degree.toDouble())
        val x = center.x + markerRadius * cos(angle).toFloat()
        val y = center.y + markerRadius * sin(angle).toFloat()
        
        val markerLength = if (degree % 90 == 0) 12.dp.toPx() else 6.dp.toPx()
        val markerWidth = if (degree % 90 == 0) 3.dp.toPx() else 2.dp.toPx()
        
        drawLine(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            start = Offset(x, y - markerLength / 2),
            end = Offset(x, y + markerLength / 2),
            strokeWidth = markerWidth,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Compass status indicator
 */
@Composable
fun CompassStatusIndicator(
    status: String,
    statusColor: String,
    modifier: Modifier = Modifier
) {
    val color = when (statusColor) {
        "green" -> Success
        "orange" -> Warning
        "red" -> Error
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        
        Text(
            text = status,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}
