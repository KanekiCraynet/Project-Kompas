package com.compasspro.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compasspro.service.LocationService
import com.compasspro.service.SensorService
import com.compasspro.ui.screen.CompassScreen
import com.compasspro.ui.theme.CompassProTheme
import com.compasspro.ui.viewmodel.CompassViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity untuk aplikasi CompassPro
 * Mengelola permission, service binding, dan UI utama
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: CompassViewModel by viewModels()
    
    // Service connections
    private var sensorService: SensorService? = null
    private var locationService: LocationService? = null
    private var isSensorServiceBound = false
    private var isLocationServiceBound = false

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startServices()
        } else {
            Toast.makeText(this, "Permission diperlukan untuk menggunakan kompas", Toast.LENGTH_LONG).show()
        }
    }

    // Service connections
    private val sensorServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SensorService.SensorBinder
            sensorService = binder.getService()
            isSensorServiceBound = true
            viewModel.setSensorService(sensorService!!)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isSensorServiceBound = false
            sensorService = null
        }
    }

    private val locationServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationService.LocationBinder
            locationService = binder.getService()
            isLocationServiceBound = true
            viewModel.setLocationService(locationService!!)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isLocationServiceBound = false
            locationService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkPermissions()
        
        setContent {
            CompassProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val compassData by viewModel.compassData.collectAsStateWithLifecycle()
                    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
                    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
                    
                    CompassScreen(
                        compassData = compassData,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onCalibrate = { viewModel.startCalibration() },
                        onRefresh = { viewModel.refreshData() }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        bindServices()
    }

    override fun onStop() {
        super.onStop()
        unbindServices()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopServices()
    }

    /**
     * Cek permission yang diperlukan
     */
    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            startServices()
        }
    }

    /**
     * Mulai service yang diperlukan
     */
    private fun startServices() {
        // Start Sensor Service
        val sensorIntent = Intent(this, SensorService::class.java).apply {
            action = SensorService.ACTION_START_SENSORS
        }
        startForegroundService(sensorIntent)
        
        // Start Location Service
        val locationIntent = Intent(this, LocationService::class.java).apply {
            action = LocationService.ACTION_START_LOCATION
        }
        startForegroundService(locationIntent)
    }

    /**
     * Bind ke service
     */
    private fun bindServices() {
        // Bind Sensor Service
        val sensorIntent = Intent(this, SensorService::class.java)
        bindService(sensorIntent, sensorServiceConnection, Context.BIND_AUTO_CREATE)
        
        // Bind Location Service
        val locationIntent = Intent(this, LocationService::class.java)
        bindService(locationIntent, locationServiceConnection, Context.BIND_AUTO_CREATE)
    }

    /**
     * Unbind dari service
     */
    private fun unbindServices() {
        if (isSensorServiceBound) {
            unbindService(sensorServiceConnection)
            isSensorServiceBound = false
        }
        
        if (isLocationServiceBound) {
            unbindService(locationServiceConnection)
            isLocationServiceBound = false
        }
    }

    /**
     * Hentikan service
     */
    private fun stopServices() {
        // Stop Sensor Service
        val sensorIntent = Intent(this, SensorService::class.java).apply {
            action = SensorService.ACTION_STOP_SENSORS
        }
        startService(sensorIntent)
        
        // Stop Location Service
        val locationIntent = Intent(this, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP_LOCATION
        }
        startService(locationIntent)
    }
}
