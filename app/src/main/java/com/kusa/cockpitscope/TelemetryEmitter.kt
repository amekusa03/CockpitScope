package com.kusa.cockpitscope

import android.os.Handler
import android.os.Looper

/**
 * LocalWrapper Template for Telemetry Emission.
 * Handles data emission at a specific frequency (e.g., 10Hz).
 */
class TelemetryEmitter(private val onUpdate: (TelemetryData) -> Unit) {
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    
    // Frequency: 10Hz = 100ms interval
    private val intervalMs = 100L

    private val runnable = object : Runnable {
        private var rpm = 0f
        private var speed = 0f
        private var throttle = 0f

        override fun run() {
            if (!isRunning) return
            
            // Generate mock data for 10Hz emission
            rpm = (rpm + 50) % 8000
            speed = (speed + 1) % 180
            throttle = (throttle + 2) % 100
            
            onUpdate(TelemetryData(rpm, speed, throttle))
            handler.postDelayed(this, intervalMs)
        }
    }

    data class TelemetryData(
        val rpm: Float,
        val speed: Float,
        val throttle: Float
    )

    fun start() {
        if (isRunning) return
        isRunning = true
        handler.post(runnable)
    }

    fun stop() {
        isRunning = false
        handler.removeCallbacks(runnable)
    }
}
