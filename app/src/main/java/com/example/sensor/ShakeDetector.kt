package com.example.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(
    var threshold: Float = 13.0f, // m/s² magnitude threshold
    var minIntervalMs: Long = 800L, // Debounce interval in ms
    private val onShake: () -> Unit
) : SensorEventListener {

    private var lastShakeTimestamp: Long = 0L

    fun register(sensorManager: SensorManager?): Boolean {
        if (sensorManager == null) return false
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return false
        return sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    fun unregister(sensorManager: SensorManager?) {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate total magnitude of acceleration
        val magnitude = sqrt(x * x + y * y + z * z)

        val currentTime = System.currentTimeMillis()
        if (magnitude > threshold) {
            if (currentTime - lastShakeTimestamp >= minIntervalMs) {
                lastShakeTimestamp = currentTime
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
