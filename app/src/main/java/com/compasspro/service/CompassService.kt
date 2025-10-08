package com.compasspro.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import com.compasspro.data.model.CompassData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

/**
 * Service for managing compass sensor data
 * Provides real-time compass readings with calibration support
 */
class CompassService : Service(), SensorEventListener {
    
    private lateinit var sensorManager: SensorManager
    private var magnetometer: Sensor? = null
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    
    private val _compassData = MutableStateFlow<CompassData?>(null)
    val compassData: StateFlow<CompassData?> = _compassData.asStateFlow()
    
    private val _isCalibrated = MutableStateFlow(false)
    val isCalibrated: StateFlow<Boolean> = _isCalibrated.asStateFlow()
    
    private val _calibrationProgress = MutableStateFlow(0)
    val calibrationProgress: StateFlow<Int> = _calibrationProgress.asStateFlow()
    
    private var magneticField = FloatArray(3)
    private var acceleration = FloatArray(3)
    private var rotation = FloatArray(3)
    
    private var lastUpdateTime = 0L
    private val updateInterval = 100L // 100ms
    
    private val binder = CompassBinder()
    
    inner class CompassBinder : Binder() {
        fun getService(): CompassService = this@CompassService
    }
    
    override fun onCreate() {
        super.onCreate()
        initializeSensors()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    private fun initializeSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        
        if (magnetometer == null || accelerometer == null) {
            // Sensors not available
            return
        }
        
        startSensorUpdates()
    }
    
    private fun startSensorUpdates() {
        magnetometer?.let { sensor ->
            sensorManager.registerListener(
                this, sensor, SensorManager.SENSOR_DELAY_UI
            )
        }
        
        accelerometer?.let { sensor ->
            sensorManager.registerListener(
                this, sensor, SensorManager.SENSOR_DELAY_UI
            )
        }
        
        gyroscope?.let { sensor ->
            sensorManager.registerListener(
                this, sensor, SensorManager.SENSOR_DELAY_UI
            )
        }
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            val currentTime = System.currentTimeMillis()
            
            if (currentTime - lastUpdateTime < updateInterval) {
                return
            }
            
            lastUpdateTime = currentTime
            
            when (sensorEvent.sensor.type) {
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(sensorEvent.values, 0, magneticField, 0, 3)
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(sensorEvent.values, 0, acceleration, 0, 3)
                }
                Sensor.TYPE_GYROSCOPE -> {
                    System.arraycopy(sensorEvent.values, 0, rotation, 0, 3)
                }
            }
            
            // Create compass data and update state
            val compassData = CompassData(
                magneticField = magneticField.clone(),
                acceleration = acceleration.clone(),
                rotation = rotation.clone()
            )
            
            _compassData.value = compassData
            _isCalibrated.value = compassData.isCalibrated()
            _calibrationProgress.value = compassData.getCalibrationQuality()
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }
    
    /**
     * Start calibration process
     */
    fun startCalibration() {
        _calibrationProgress.value = 0
        _isCalibrated.value = false
        
        // In a real implementation, you would start a calibration routine
        // that requires the user to move the device in a figure-8 pattern
        // For now, we'll simulate calibration progress
        simulateCalibration()
    }
    
    private fun simulateCalibration() {
        // This is a simplified calibration simulation
        // In production, implement proper magnetometer calibration
        Thread {
            for (i in 0..100 step 10) {
                Thread.sleep(200)
                _calibrationProgress.value = i
            }
            _isCalibrated.value = true
        }.start()
    }
    
    /**
     * Get current compass heading
     */
    fun getCurrentHeading(): Float? {
        return _compassData.value?.getMagneticHeading()
    }
    
    /**
     * Get current compass direction as string
     */
    fun getCurrentDirection(): String? {
        return _compassData.value?.getDirectionString()
    }
    
    /**
     * Check if sensors are available
     */
    fun areSensorsAvailable(): Boolean {
        return magnetometer != null && accelerometer != null
    }
    
    /**
     * Get sensor information
     */
    fun getSensorInfo(): SensorInfo {
        return SensorInfo(
            hasMagnetometer = magnetometer != null,
            hasAccelerometer = accelerometer != null,
            hasGyroscope = gyroscope != null,
            magnetometerName = magnetometer?.name ?: "Not Available",
            accelerometerName = accelerometer?.name ?: "Not Available",
            gyroscopeName = gyroscope?.name ?: "Not Available"
        )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}

/**
 * Data class for sensor information
 */
data class SensorInfo(
    val hasMagnetometer: Boolean,
    val hasAccelerometer: Boolean,
    val hasGyroscope: Boolean,
    val magnetometerName: String,
    val accelerometerName: String,
    val gyroscopeName: String
)
