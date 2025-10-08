package com.compasspro.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.compasspro.CompassApplication
import com.compasspro.R
import com.compasspro.domain.model.LocationData
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
import java.io.IOException
import java.util.*
import javax.inject.Inject

/**
 * Service untuk mengelola lokasi pengguna dengan GPS dan Network Provider
 * Menyediakan data lokasi real-time dengan akurasi tinggi
 */
@AndroidEntryPoint
class LocationService : Service(), LocationListener {

    @Inject
    lateinit var locationManager: LocationManager

    @Inject
    lateinit var geocoder: Geocoder

    private val binder = LocationBinder()
    private var serviceJob: Job? = null
    private val locationDataChannel = Channel<LocationData>(Channel.UNLIMITED)
    
    // Location providers
    private var gpsProvider: String? = null
    private var networkProvider: String? = null
    
    // Current location data
    private var currentLocation: Location? = null
    private var lastKnownLocation: Location? = null
    
    // Configuration
    private val minTimeMs = 1000L // Update setiap 1 detik
    private val minDistanceM = 1f // Update setiap 1 meter

    inner class LocationBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    override fun onCreate() {
        super.onCreate()
        initializeLocationProviders()
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_LOCATION -> startLocationUpdates()
            ACTION_STOP_LOCATION -> stopLocationUpdates()
            ACTION_GET_LAST_KNOWN -> getLastKnownLocation()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        serviceJob?.cancel()
    }

    /**
     * Inisialisasi location providers
     */
    private fun initializeLocationProviders() {
        gpsProvider = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            LocationManager.GPS_PROVIDER
        } else null
        
        networkProvider = if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            LocationManager.NETWORK_PROVIDER
        } else null
    }

    /**
     * Mulai update lokasi
     */
    fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            return
        }

        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            // Request location updates dari GPS provider (prioritas tertinggi)
            gpsProvider?.let { provider ->
                if (ActivityCompat.checkSelfPermission(
                        this@LocationService,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.requestLocationUpdates(
                        provider,
                        minTimeMs,
                        minDistanceM,
                        this@LocationService
                    )
                }
            }
            
            // Request location updates dari Network provider (backup)
            networkProvider?.let { provider ->
                if (ActivityCompat.checkSelfPermission(
                        this@LocationService,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.requestLocationUpdates(
                        provider,
                        minTimeMs * 2, // Update lebih jarang untuk network
                        minDistanceM * 5, // Jarak lebih besar untuk network
                        this@LocationService
                    )
                }
            }
        }
    }

    /**
     * Hentikan update lokasi
     */
    fun stopLocationUpdates() {
        locationManager.removeUpdates(this)
        serviceJob?.cancel()
    }

    /**
     * Ambil lokasi terakhir yang diketahui
     */
    fun getLastKnownLocation() {
        if (!hasLocationPermission()) return

        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            // Coba ambil dari GPS terlebih dahulu
            gpsProvider?.let { provider ->
                if (ActivityCompat.checkSelfPermission(
                        this@LocationService,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val gpsLocation = locationManager.getLastKnownLocation(provider)
                    if (gpsLocation != null) {
                        processLocation(gpsLocation)
                        return@launch
                    }
                }
            }
            
            // Jika GPS tidak ada, coba network
            networkProvider?.let { provider ->
                if (ActivityCompat.checkSelfPermission(
                        this@LocationService,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val networkLocation = locationManager.getLastKnownLocation(provider)
                    if (networkLocation != null) {
                        processLocation(networkLocation)
                    }
                }
            }
        }
    }

    /**
     * Get flow untuk data lokasi
     */
    fun getLocationDataFlow(): Flow<LocationData> = locationDataChannel.receiveAsFlow()

    /**
     * Cek apakah permission lokasi tersedia
     */
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Cek apakah GPS aktif
     */
    fun isGpsEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * Cek apakah Network location aktif
     */
    fun isNetworkLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Get lokasi saat ini
     */
    fun getCurrentLocation(): Location? = currentLocation

    override fun onLocationChanged(location: Location) {
        processLocation(location)
    }

    override fun onProviderEnabled(provider: String) {
        // Provider diaktifkan, restart location updates
        startLocationUpdates()
    }

    override fun onProviderDisabled(provider: String) {
        // Provider dinonaktifkan, cek apakah masih ada provider lain
        if (!isGpsEnabled() && !isNetworkLocationEnabled()) {
            // Tidak ada provider yang aktif
            stopLocationUpdates()
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // Handle perubahan status provider
    }

    /**
     * Proses data lokasi dan kirim ke channel
     */
    private fun processLocation(location: Location) {
        currentLocation = location
        lastKnownLocation = location

        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                // Dapatkan alamat dari koordinat
                val address = getAddressFromLocation(location.latitude, location.longitude)
                
                val locationData = LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = location.altitude,
                    accuracy = location.accuracy,
                    speed = location.speed,
                    bearing = location.bearing,
                    address = address,
                    timestamp = location.time
                )
                
                locationDataChannel.trySend(locationData)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Dapatkan alamat dari koordinat latitude dan longitude
     */
    private suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                buildString {
                    address.getAddressLine(0)?.let { append(it) }
                    if (address.locality != null) {
                        if (isNotEmpty()) append(", ")
                        append(address.locality)
                    }
                    if (address.adminArea != null) {
                        if (isNotEmpty()) append(", ")
                        append(address.adminArea)
                    }
                    if (address.countryName != null) {
                        if (isNotEmpty()) append(", ")
                        append(address.countryName)
                    }
                }
            } else {
                "Lokasi tidak diketahui"
            }
        } catch (e: IOException) {
            "Tidak dapat mendapatkan alamat"
        } catch (e: Exception) {
            "Error mendapatkan alamat"
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

        val notification = NotificationCompat.Builder(this, CompassApplication.LOCATION_CHANNEL_ID)
            .setContentTitle("Lokasi Kompas Aktif")
            .setContentText("Mengumpulkan data lokasi untuk kompas")
            .setSmallIcon(R.drawable.ic_location)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        startForeground(CompassApplication.NOTIFICATION_ID_LOCATION, notification)
    }

    companion object {
        const val ACTION_START_LOCATION = "START_LOCATION"
        const val ACTION_STOP_LOCATION = "STOP_LOCATION"
        const val ACTION_GET_LAST_KNOWN = "GET_LAST_KNOWN"
    }
}
