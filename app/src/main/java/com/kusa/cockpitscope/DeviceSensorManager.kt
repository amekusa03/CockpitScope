package com.kusa.cockpitscope

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class DeviceSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var currentGForce = 0f
        private set
    var accX = 0f
        private set
    var accY = 0f
        private set
    var accZ = 0f
        private set

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            accX = event.values[0]
            accY = event.values[1]
            accZ = event.values[2]

            // 重力加速度(9.8m/s^2)を除いた合成加速度（G）の簡易計算
            val magnitude = sqrt(accX * accX + accY * accY + accZ * accZ)
            currentGForce = magnitude / 9.81f
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
