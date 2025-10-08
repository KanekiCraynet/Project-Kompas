package com.compasspro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compasspro.service.CompassService
import com.compasspro.service.LocationService
import com.compasspro.ui.screen.CompassScreen
import com.compasspro.ui.theme.CompassProTheme
import org.koin.android.ext.android.inject

/**
 * Main activity for Compass Pro application
 */
class MainActivity : ComponentActivity() {
    
    private val compassViewModel: com.compasspro.ui.screen.CompassViewModel by viewModels()
    private val locationViewModel: com.compasspro.ui.screen.LocationViewModel by viewModels()
    private val weatherViewModel: com.compasspro.ui.screen.WeatherViewModel by viewModels()
    
    private val compassService: CompassService by inject()
    private val locationService: LocationService by inject()
    
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted
                startServices()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted
                startServices()
            }
            else -> {
                // No location access granted
                // Handle permission denied
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            CompassProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompassScreen(
                        compassViewModel = compassViewModel,
                        locationViewModel = locationViewModel,
                        weatherViewModel = weatherViewModel,
                        onRequestPermissions = { requestLocationPermissions() },
                        onStartServices = { startServices() }
                    )
                }
            }
        }
        
        // Check permissions and start services
        checkPermissionsAndStartServices()
    }
    
    private fun checkPermissionsAndStartServices() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                startServices()
            }
            else -> {
                // Request permission
                requestLocationPermissions()
            }
        }
    }
    
    private fun requestLocationPermissions() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    
    private fun startServices() {
        // Start compass service
        val compassIntent = Intent(this, CompassService::class.java)
        startService(compassIntent)
        
        // Start location service
        val locationIntent = Intent(this, LocationService::class.java)
        startService(locationIntent)
        
        // Start location updates
        locationViewModel.startLocationUpdates()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Stop services
        stopService(Intent(this, CompassService::class.java))
        stopService(Intent(this, LocationService::class.java))
    }
}
