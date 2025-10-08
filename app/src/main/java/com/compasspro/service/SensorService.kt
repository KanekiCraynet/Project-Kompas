package com.compasspro.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.compasspro.CompassApplication
import com.compasspro.R
import com.compasspro.domain.model.RawSensorData
import com.compasspro.ui.MainActivity
import com.compasspro.utils.Config
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Service untuk mengelola sensor kompas (accelerometer, magnetometer, gyroscope)
 * Berjalan sebagai foreground service untuk memastikan kontinuitas data
 */
@AndroidEntryPoint
class SensorService : Service(), SensorEventListener {

    @Inject
    lateinit var sensorManager: SensorManager

    private val binder = SensorBinder()
    private var serviceJob: Job? = null
    private val sensorDataChannel = Channel<RawSensorData>(Channel.UNLIMITED)
    
    // Sensor instances
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var gyroscope: Sensor? = null
    
    // Sensor data
    private var accelerometerData = FloatArray(3)
    private var magnetometerData = FloatArray(3)
    private var gyroscopeData = FloatArray(3)
    
    // Kalibrasi data
    private var isCalibrated = false
    private var calibrationOffset = FloatArray(3)

    inner class SensorBinder : Binder() {
        fun getService(): SensorService = this@SensorService
    }

    override fun onCreate() {
        super.onCreate()
        initializeSensors()
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SENSORS -> startSensorMonitoring()
            ACTION_STOP_SENSORS -> stopSensorMonitoring()
            ACTION_CALIBRATE -> startCalibration()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        stopSensorMonitoring()
        serviceJob?.cancel()
    }

    /**
     * Inisialisasi sensor yang diperlukan
     */
    private fun initializeSensors() {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        
        // Cek ketersediaan sensor
        if (accelerometer == null || magnetometer == null) {
            // Sensor tidak tersedia, hentikan service
            stopSelf()
        }
    }

    /**
     * Mulai monitoring sensor
     */
    fun startSensorMonitoring() {
        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            // Register sensor listeners dengan delay yang optimal
            accelerometer?.let { 
                sensorManager.registerListener(
                    this@SensorService, 
                    it, 
                    SensorManager.SENSOR_DELAY_UI
                ) 
            }
            
            magnetometer?.let { 
                sensorManager.registerListener(
                    this@SensorService, 
                    it, 
                    SensorManager.SENSOR_DELAY_UI
                ) 
            }
            
            gyroscope?.let { 
                sensorManager.registerListener(
                    this@SensorService, 
                    it, 
                    SensorManager.SENSOR_DELAY_UI
                ) 
            }
        }
    }

    /**
     * Hentikan monitoring sensor
     */
    fun stopSensorMonitoring() {
        sensorManager.unregisterListener(this)
        serviceJob?.cancel()
    }

    /**
     * Mulai proses kalibrasi sensor
     */
    fun startCalibration() {
        isCalibrated = false
        // Reset kalibrasi offset
        calibrationOffset.fill(0f)
    }

    /**
     * Selesai kalibrasi dan terapkan offset
     */
    fun finishCalibration() {
        isCalibrated = true
    }

    /**
     * Get flow untuk data sensor
     */
    fun getSensorDataFlow(): Flow<RawSensorData> = sensorDataChannel.receiveAsFlow()

    /**
     * Cek apakah sensor tersedia
     */
    fun areSensorsAvailable(): Boolean {
        return accelerometer != null && magnetometer != null
    }

    /**
     * Cek status kalibrasi
     */
    fun isCalibrated(): Boolean = isCalibrated

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            when (sensorEvent.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(sensorEvent.values, 0, accelerometerData, 0, 3)
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(sensorEvent.values, 0, magnetometerData, 0, 3)
                }
                Sensor.TYPE_GYROSCOPE -> {
                    System.arraycopy(sensorEvent.values, 0, gyroscopeData, 0, 3)
                }
            }
            
            // Kirim data gabungan
            val rawData = RawSensorData(
                accelerometerX = accelerometerData[0],
                accelerometerY = accelerometerData[1],
                accelerometerZ = accelerometerData[2],
                magnetometerX = magnetometerData[0] - calibrationOffset[0],
                magnetometerY = magnetometerData[1] - calibrationOffset[1],
                magnetometerZ = magnetometerData[2] - calibrationOffset[2],
                gyroscopeX = gyroscopeData[0],
                gyroscopeY = gyroscopeData[1],
                gyroscopeZ = gyroscopeData[2],
                timestamp = System.currentTimeMillis()
            )
            
            // Kirim data ke channel
            CoroutineScope(Dispatchers.IO).launch {
                sensorDataChannel.trySend(rawData)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle perubahan akurasi sensor
        when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                // Sensor akurat
            }
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                // Sensor akurasi sedang
            }
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                // Sensor akurasi rendah, mungkin perlu kalibrasi
            }
            SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                // Sensor tidak dapat diandalkan
            }
        }
    }

    /**
     * Mulai foreground service dengan notifikasi
     */
    private fun startForegroundService() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CompassApplication.SENSOR_CHANNEL_ID)
            .setContentTitle("Sensor Kompas Aktif")
            .setContentText("Mengumpulkan data sensor untuk kompas")
            .setSmallIcon(R.drawable.ic_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        startForeground(CompassApplication.NOTIFICATION_ID_SENSOR, notification)
    }

    companion object {
        const val ACTION_START_SENSORS = "START_SENSORS"
        const val ACTION_STOP_SENSORS = "STOP_SENSORS"
        const val ACTION_CALIBRATE = "CALIBRATE"
    }
}
