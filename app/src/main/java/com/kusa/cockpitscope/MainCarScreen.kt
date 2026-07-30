package com.kusa.cockpitscope

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainCarScreen(carContext: CarContext) : Screen(carContext), DefaultLifecycleObserver {

    private val settingsManager = SettingsManager(carContext)
    private val obdBluetoothManager = ObdBluetoothManager()
    
    // データ保持用
    private val dataMap = mutableMapOf<String, Float>()
    private var isObdConnected = false
    private var dataJob: Job? = null

    init {
        lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        startDataCycle()
    }

    override fun onPause(owner: LifecycleOwner) {
        stopDataCycle()
    }

    private fun startDataCycle() {
        dataJob?.cancel()
        dataJob = lifecycleScope.launch {
            val address = settingsManager.selectedDeviceAddress
            if (address != null) {
                isObdConnected = obdBluetoothManager.connect(address)
            }

            var time = 0f
            while (true) {
                if (isObdConnected) {
                    fetchRealData()
                } else {
                    generateMockData(time)
                }

                // UIの更新を要求
                invalidate()

                time += 0.1f
                // ELM327のレスポンスに合わせて間隔を調整
                delay(if (isObdConnected) 100 else 200)
            }
        }
    }

    private fun stopDataCycle() {
        dataJob?.cancel()
        dataJob = null
        obdBluetoothManager.close()
        isObdConnected = false
    }

    private suspend fun fetchRealData() {
        val pids = mapOf(
            "rpm" to "01 0C",
            "speed" to "01 0D",
            "throttle" to "01 11",
            "water_temp" to "01 05",
            "intake_air_temp" to "01 0F",
            "timing_advance" to "01 0E",
            "ambient_air_temp" to "01 46",
            "fuel_level" to "01 2F",
            "baro_pressure" to "01 33"
        )
        
        pids.forEach { (id, pid) ->
            if (settingsManager.isEnabled(id)) {
                val resp = obdBluetoothManager.fetchData(pid)
                val value = when(id) {
                    "rpm" -> obdBluetoothManager.parseRpm(resp)
                    "speed" -> obdBluetoothManager.parseSpeed(resp)
                    "throttle" -> obdBluetoothManager.parseThrottle(resp)
                    "water_temp" -> obdBluetoothManager.parseWaterTemp(resp)
                    "intake_air_temp" -> obdBluetoothManager.parseIntakeAirTemp(resp)
                    "timing_advance" -> obdBluetoothManager.parseTimingAdvance(resp)
                    "ambient_air_temp" -> obdBluetoothManager.parseAmbientAirTemp(resp)
                    "fuel_level" -> obdBluetoothManager.parseFuelLevel(resp)
                    "baro_pressure" -> obdBluetoothManager.parseBaroPressure(resp)
                    else -> 0f
                }
                dataMap[id] = value
            }
        }
    }

    private fun generateMockData(time: Float) {
        val throttle = (kotlin.math.sin(time * 0.5f) * 50f + 50f).coerceIn(0f, 100f)
        dataMap["rpm"] = (throttle * 60f + 800f + (kotlin.math.sin(time * 2f) * 200f)).coerceIn(800f, 7500f)
        dataMap["speed"] = ((time * 5f) % 160f).coerceIn(0f, 180f)
        dataMap["throttle"] = throttle
        dataMap["water_temp"] = 85f + kotlin.math.sin(time * 0.1f).toFloat() * 5f
        dataMap["intake_air_temp"] = 40f + kotlin.math.sin(time * 0.2f).toFloat() * 10f
        dataMap["timing_advance"] = 10f + kotlin.math.sin(time * 1f).toFloat() * 20f
        dataMap["ambient_air_temp"] = 25f + kotlin.math.sin(time * 0.05f).toFloat() * 2f
        dataMap["fuel_level"] = (100f - time * 0.1f).coerceAtLeast(0f)
        dataMap["baro_pressure"] = 101f + kotlin.math.sin(time * 0.01f).toFloat() * 2f
    }

    override fun onGetTemplate(): Template {
        val paneBuilder = Pane.Builder()

        SettingsManager.ALL_ITEMS.forEach { item ->
            if (settingsManager.isEnabled(item.id)) {
                val value = dataMap[item.id] ?: 0f
                val formattedValue = "%.${item.decimalPlaces}f ${item.unit}".format(value)
                
                paneBuilder.addRow(
                    Row.Builder()
                        .setTitle(item.label)
                        .addText(formattedValue)
                        .build()
                )
            }
        }

        // OBD接続状態の表示
        val statusText = if (isObdConnected) "Connected to OBD" else "OBD Disconnected (Demo Mode)"
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("System Status")
                .addText(statusText)
                .build()
        )

        return PaneTemplate.Builder(paneBuilder.build())
            .setHeader(
                Header.Builder()
                    .setStartHeaderAction(Action.APP_ICON)
                    .setTitle("Cockpit Scope Telemetry")
                    .build()
            )
            .build()
    }
}
