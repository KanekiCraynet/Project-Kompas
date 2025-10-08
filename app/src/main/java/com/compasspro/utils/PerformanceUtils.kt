package com.compasspro.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.conflate

/**
 * Utility class untuk optimasi performa aplikasi
 */
object PerformanceUtils {
    
    /**
     * Debounce flow untuk mengurangi frekuensi update yang tidak perlu
     */
    fun <T> Flow<T>.debounceUpdates(timeoutMs: Long = 100L): Flow<T> {
        return this.debounce(timeoutMs)
    }
    
    /**
     * Filter flow untuk menghindari nilai duplikat
     */
    fun <T> Flow<T>.filterDuplicates(): Flow<T> {
        return this.distinctUntilChanged()
    }
    
    /**
     * Buffer flow untuk meningkatkan throughput
     */
    fun <T> Flow<T>.bufferUpdates(capacity: Int = 64): Flow<T> {
        return this.buffer(capacity)
    }
    
    /**
     * Conflate flow untuk mengambil nilai terbaru saja
     */
    fun <T> Flow<T>.conflateUpdates(): Flow<T> {
        return this.conflate()
    }
    
    /**
     * Retry flow dengan exponential backoff
     */
    fun <T> Flow<T>.retryWithBackoff(
        maxRetries: Int = 3,
        initialDelay: Long = 1000L,
        maxDelay: Long = 10000L
    ): Flow<T> {
        return this.retry(maxRetries) { cause ->
            delay(initialDelay.coerceAtMost(maxDelay))
            true
        }
    }
    
    /**
     * Flow dengan error handling yang robust
     */
    fun <T> Flow<T>.withErrorHandling(
        onError: (Throwable) -> Unit = {},
        onStart: () -> Unit = {},
        onCompletion: () -> Unit = {}
    ): Flow<T> {
        return this
            .onStart { onStart() }
            .onCompletion { onCompletion() }
            .catch { error -> onError(error) }
    }
    
    /**
     * Flow yang dijalankan di background thread
     */
    fun <T> Flow<T>.onBackground(): Flow<T> {
        return this.flowOn(Dispatchers.IO)
    }
    
    /**
     * Flow yang dijalankan di main thread
     */
    fun <T> Flow<T>.onMain(): Flow<T> {
        return this.flowOn(Dispatchers.Main)
    }
    
    /**
     * Throttle flow untuk membatasi frekuensi update
     */
    fun <T> Flow<T>.throttle(timeoutMs: Long): Flow<T> {
        return flow {
            var lastEmission = 0L
            collect { value ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastEmission >= timeoutMs) {
                    emit(value)
                    lastEmission = currentTime
                }
            }
        }
    }
    
    /**
     * Sample flow untuk mengambil sample pada interval tertentu
     */
    fun <T> Flow<T>.sample(intervalMs: Long): Flow<T> {
        return flow {
            var lastSample = 0L
            collect { value ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastSample >= intervalMs) {
                    emit(value)
                    lastSample = currentTime
                }
            }
        }
    }
    
    /**
     * Cek apakah device memiliki performa tinggi
     */
    fun isHighPerformanceDevice(): Boolean {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        
        // Device dengan RAM > 4GB dianggap high performance
        return maxMemory > 4L * 1024 * 1024 * 1024
    }
    
    /**
     * Optimasi berdasarkan performa device
     */
    fun getOptimalSensorDelay(): Int {
        return if (isHighPerformanceDevice()) {
            android.hardware.SensorManager.SENSOR_DELAY_FASTEST
        } else {
            android.hardware.SensorManager.SENSOR_DELAY_UI
        }
    }
    
    /**
     * Optimasi cache size berdasarkan performa device
     */
    fun getOptimalCacheSize(): Int {
        return if (isHighPerformanceDevice()) {
            Config.MAX_CACHE_SIZE
        } else {
            Config.MAX_CACHE_SIZE / 2
        }
    }
}
