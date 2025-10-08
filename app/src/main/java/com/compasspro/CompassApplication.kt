package com.compasspro

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class untuk CompassPro
 * Mengatur dependency injection dan konfigurasi global aplikasi
 */
@HiltAndroidApp
class CompassApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerConfiguration: Configuration

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        initializeWorkManager()
    }

    /**
     * Membuat notification channels untuk Android 8.0+
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Channel untuk location service
            val locationChannel = NotificationChannel(
                LOCATION_CHANNEL_ID,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Menampilkan notifikasi untuk layanan lokasi kompas"
                setShowBadge(false)
            }

            // Channel untuk sensor service
            val sensorChannel = NotificationChannel(
                SENSOR_CHANNEL_ID,
                "Sensor Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Menampilkan notifikasi untuk layanan sensor kompas"
                setShowBadge(false)
            }

            notificationManager.createNotificationChannels(listOf(locationChannel, sensorChannel))
        }
    }

    /**
     * Inisialisasi WorkManager dengan konfigurasi custom
     */
    private fun initializeWorkManager() {
        WorkManager.initialize(this, workerConfiguration)
    }

    override val workManagerConfiguration: Configuration
        get() = workerConfiguration

    companion object {
        const val LOCATION_CHANNEL_ID = "location_service_channel"
        const val SENSOR_CHANNEL_ID = "sensor_service_channel"
        const val NOTIFICATION_ID_LOCATION = 1001
        const val NOTIFICATION_ID_SENSOR = 1002
    }
}
