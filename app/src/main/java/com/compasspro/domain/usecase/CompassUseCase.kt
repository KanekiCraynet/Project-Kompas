package com.compasspro.domain.usecase

import com.compasspro.domain.model.CompassData
import com.compasspro.domain.model.RawSensorData
import com.compasspro.domain.model.SensorQuality
import com.compasspro.utils.Config
import kotlin.math.*

/**
 * Use case untuk perhitungan dan pemrosesan data kompas
 * Mengimplementasikan algoritma kompleks untuk akurasi tinggi
 */
class CompassUseCase {

    private var lastRawData: RawSensorData? = null
    private var calibrationData = mutableListOf<RawSensorData>()
    private var isCalibrating = false

    /**
     * Memproses data sensor mentah menjadi data kompas yang akurat
     */
    fun processCompassData(
        rawData: RawSensorData,
        magneticDeclination: Float = 0f,
        locationData: com.compasspro.domain.model.LocationData? = null,
        windData: com.compasspro.domain.model.WindData? = null
    ): CompassData {
        
        // Kalibrasi sensor jika diperlukan
        if (isCalibrating) {
            calibrationData.add(rawData)
            if (calibrationData.size >= Config.CALIBRATION_SAMPLE_SIZE) {
                performCalibration()
            }
        }

        // Hitung heading magnetik dengan algoritma yang lebih akurat
        val magneticHeading = calculateMagneticHeading(rawData)
        
        // Hitung heading sebenarnya
        val trueHeading = (magneticHeading + magneticDeclination + 360f) % 360f
        
        // Evaluasi kualitas sensor
        val sensorQuality = evaluateSensorQuality(rawData)
        
        // Hitung akurasi berdasarkan variasi data
        val accuracy = calculateAccuracy(rawData)

        return CompassData(
            magneticHeading = magneticHeading,
            trueHeading = trueHeading,
            magneticDeclination = magneticDeclination,
            accuracy = accuracy,
            isCalibrated = !isCalibrating && calibrationData.isNotEmpty(),
            timestamp = rawData.timestamp,
            location = locationData,
            windData = windData,
            sensorQuality = sensorQuality
        )
    }

    /**
     * Menghitung heading magnetik dengan algoritma yang lebih presisi
     */
    private fun calculateMagneticHeading(rawData: RawSensorData): Float {
        val accelX = rawData.accelerometerX
        val accelY = rawData.accelerometerY
        val accelZ = rawData.accelerometerZ
        
        val magX = rawData.magnetometerX
        val magY = rawData.magnetometerY
        val magZ = rawData.magnetometerZ

        // Normalisasi vektor accelerometer
        val accelMagnitude = sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ)
        val normAccelX = accelX / accelMagnitude
        val normAccelY = accelY / accelMagnitude
        val normAccelZ = accelZ / accelMagnitude

        // Hitung vektor magnetik yang diproyeksikan ke bidang horizontal
        val magMagnitude = sqrt(magX * magX + magY * magY + magZ * magZ)
        val normMagX = magX / magMagnitude
        val normMagY = magY / magMagnitude
        val normMagZ = magZ / magMagnitude

        // Proyeksi vektor magnetik ke bidang horizontal
        val horizontalMagX = normMagX - normAccelX * (normMagX * normAccelX + normMagY * normAccelY + normMagZ * normAccelZ)
        val horizontalMagY = normMagY - normAccelY * (normMagX * normAccelX + normMagY * normAccelY + normMagZ * normAccelZ)

        // Hitung heading
        val heading = Math.toDegrees(atan2(-horizontalMagY, horizontalMagX)).toFloat()
        
        return (heading + 360f) % 360f
    }

    /**
     * Evaluasi kualitas sensor berdasarkan stabilitas dan konsistensi data
     */
    private fun evaluateSensorQuality(rawData: RawSensorData): SensorQuality {
        val lastData = lastRawData ?: return SensorQuality.UNKNOWN
        
        // Hitung variasi dari data sebelumnya
        val accelVariation = sqrt(
            (rawData.accelerometerX - lastData.accelerometerX).pow(2) +
            (rawData.accelerometerY - lastData.accelerometerY).pow(2) +
            (rawData.accelerometerZ - lastData.accelerometerZ).pow(2)
        )
        
        val magVariation = sqrt(
            (rawData.magnetometerX - lastData.magnetometerX).pow(2) +
            (rawData.magnetometerY - lastData.magnetometerY).pow(2) +
            (rawData.magnetometerZ - lastData.magnetometerZ).pow(2)
        )

        // Evaluasi berdasarkan variasi
        return when {
            accelVariation < Config.EXCELLENT_ACCURACY_THRESHOLD && magVariation < Config.EXCELLENT_ACCURACY_THRESHOLD -> SensorQuality.EXCELLENT
            accelVariation < Config.GOOD_ACCURACY_THRESHOLD && magVariation < Config.GOOD_ACCURACY_THRESHOLD -> SensorQuality.GOOD
            accelVariation < Config.FAIR_ACCURACY_THRESHOLD && magVariation < Config.FAIR_ACCURACY_THRESHOLD -> SensorQuality.FAIR
            else -> SensorQuality.POOR
        }
    }

    /**
     * Hitung akurasi sensor berdasarkan konsistensi data
     */
    private fun calculateAccuracy(rawData: RawSensorData): Float {
        if (calibrationData.isEmpty()) return 0f
        
        // Hitung standar deviasi dari data kalibrasi
        val magXValues = calibrationData.map { it.magnetometerX }
        val magYValues = calibrationData.map { it.magnetometerY }
        val magZValues = calibrationData.map { it.magnetometerZ }
        
        val magXStdDev = calculateStandardDeviation(magXValues)
        val magYStdDev = calculateStandardDeviation(magYValues)
        val magZStdDev = calculateStandardDeviation(magZValues)
        
        val avgStdDev = (magXStdDev + magYStdDev + magZStdDev) / 3f
        
        // Konversi ke akurasi dalam derajat (semakin kecil std dev, semakin akurat)
        return maxOf(0f, 360f - (avgStdDev * 100f))
    }

    /**
     * Hitung standar deviasi dari list nilai
     */
    private fun calculateStandardDeviation(values: List<Float>): Float {
        if (values.isEmpty()) return 0f
        
        val mean = values.average().toFloat()
        val variance = values.map { (it - mean).pow(2) }.average().toFloat()
        return sqrt(variance)
    }

    /**
     * Mulai proses kalibrasi sensor
     */
    fun startCalibration() {
        isCalibrating = true
        calibrationData.clear()
    }

    /**
     * Selesai kalibrasi dan terapkan hasil
     */
    private fun performCalibration() {
        if (calibrationData.size < 10) return
        
        // Hitung offset dan skala untuk kalibrasi
        val magXValues = calibrationData.map { it.magnetometerX }
        val magYValues = calibrationData.map { it.magnetometerY }
        val magZValues = calibrationData.map { it.magnetometerZ }
        
        // Hitung hard iron offset (bias magnetik)
        val hardIronX = (magXValues.maxOrNull()!! + magXValues.minOrNull()!!) / 2f
        val hardIronY = (magYValues.maxOrNull()!! + magYValues.minOrNull()!!) / 2f
        val hardIronZ = (magZValues.maxOrNull()!! + magZValues.minOrNull()!!) / 2f
        
        // Simpan hasil kalibrasi (bisa disimpan ke SharedPreferences atau database)
        // Untuk sekarang, kita hanya set flag kalibrasi selesai
        isCalibrating = false
    }

    /**
     * Cek apakah sensor perlu dikalibrasi
     */
    fun needsCalibration(): Boolean {
        return !isCalibrating && calibrationData.isEmpty()
    }

    /**
     * Update data sensor terakhir untuk perhitungan selanjutnya
     */
    fun updateLastRawData(rawData: RawSensorData) {
        lastRawData = rawData
    }
}
