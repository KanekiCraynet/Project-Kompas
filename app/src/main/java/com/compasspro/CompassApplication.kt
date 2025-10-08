package com.compasspro

import android.app.Application
import com.compasspro.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Application class for Compass Pro
 * Initializes dependency injection and global configurations
 */
class CompassApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin dependency injection
        startKoin {
            androidContext(this@CompassApplication)
            modules(appModule)
        }
    }
}
