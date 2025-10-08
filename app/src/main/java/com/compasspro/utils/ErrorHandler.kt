package com.compasspro.utils

import android.content.Context
import android.widget.Toast
import com.compasspro.R

/**
 * Utility class untuk menangani error dan menampilkan pesan yang sesuai
 */
object ErrorHandler {
    
    /**
     * Menampilkan error message berdasarkan jenis error
     */
    fun showError(context: Context, error: Throwable) {
        val message = when (error) {
            is SecurityException -> Config.ERROR_LOCATION_PERMISSION
            is IllegalStateException -> Config.ERROR_SENSOR_UNAVAILABLE
            is java.net.UnknownHostException -> Config.ERROR_NETWORK_UNAVAILABLE
            is java.net.SocketTimeoutException -> Config.ERROR_NETWORK_UNAVAILABLE
            is retrofit2.HttpException -> {
                when (error.code()) {
                    401 -> "API key tidak valid"
                    403 -> Config.ERROR_API_LIMIT_EXCEEDED
                    404 -> "Data tidak ditemukan"
                    429 -> Config.ERROR_API_LIMIT_EXCEEDED
                    else -> "Error server: ${error.code()}"
                }
            }
            else -> error.message ?: "Terjadi kesalahan yang tidak diketahui"
        }
        
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Mendapatkan pesan error berdasarkan jenis error
     */
    fun getErrorMessage(error: Throwable): String {
        return when (error) {
            is SecurityException -> Config.ERROR_LOCATION_PERMISSION
            is IllegalStateException -> Config.ERROR_SENSOR_UNAVAILABLE
            is java.net.UnknownHostException -> Config.ERROR_NETWORK_UNAVAILABLE
            is java.net.SocketTimeoutException -> Config.ERROR_NETWORK_UNAVAILABLE
            is retrofit2.HttpException -> {
                when (error.code()) {
                    401 -> "API key tidak valid"
                    403 -> Config.ERROR_API_LIMIT_EXCEEDED
                    404 -> "Data tidak ditemukan"
                    429 -> Config.ERROR_API_LIMIT_EXCEEDED
                    else -> "Error server: ${error.code()}"
                }
            }
            else -> error.message ?: "Terjadi kesalahan yang tidak diketahui"
        }
    }
    
    /**
     * Log error untuk debugging
     */
    fun logError(tag: String, error: Throwable, message: String = "") {
        android.util.Log.e(tag, message, error)
    }
    
    /**
     * Cek apakah error dapat di-recover
     */
    fun isRecoverableError(error: Throwable): Boolean {
        return when (error) {
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException,
            is retrofit2.HttpException -> true
            else -> false
        }
    }
}
