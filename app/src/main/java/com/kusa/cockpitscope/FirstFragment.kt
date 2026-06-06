package com.kusa.cockpitscope

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.kusa.cockpitscope.databinding.FragmentFirstBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var settingsManager: SettingsManager
    private lateinit var deviceSensorManager: DeviceSensorManager
    private val obdBluetoothManager = ObdBluetoothManager()
    private var isObdConnected = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsManager = SettingsManager(requireContext())
        deviceSensorManager = DeviceSensorManager(requireContext())

        setupScope()
        startDataCycle()
    }

    override fun onResume() {
        super.onResume()
        deviceSensorManager.start()
    }

    override fun onPause() {
        super.onPause()
        deviceSensorManager.stop()
    }

    private fun startDataCycle() {
        viewLifecycleOwner.lifecycleScope.launch {
            val address = settingsManager.selectedDeviceAddress
            if (address != null) {
                isObdConnected = obdBluetoothManager.connect(address)
            }

            var time = 0f
            while (true) {
                // OBDデータ取得
                if (isObdConnected) {
                    fetchRealData()
                } else {
                    generateMockData(time)
                }

                // デバイスセンサー（加速度G）は常に取得可能
                if (settingsManager.isEnabled("gforce")) {
                    binding.combinedScope.addDataPoint("gforce", deviceSensorManager.currentGForce)
                }

                time += 0.05f
                delay(if (isObdConnected) 100 else 30)
            }
        }
    }

    private suspend fun fetchRealData() {
        if (settingsManager.isEnabled("rpm")) {
            val resp = obdBluetoothManager.fetchData("01 0C")
            binding.combinedScope.addDataPoint("rpm", obdBluetoothManager.parseRpm(resp))
        }
        if (settingsManager.isEnabled("speed")) {
            val resp = obdBluetoothManager.fetchData("01 0D")
            binding.combinedScope.addDataPoint("speed", obdBluetoothManager.parseSpeed(resp))
        }
        if (settingsManager.isEnabled("throttle")) {
            val resp = obdBluetoothManager.fetchData("01 11")
            binding.combinedScope.addDataPoint("throttle", obdBluetoothManager.parseThrottle(resp))
        }
        if (settingsManager.isEnabled("water_temp")) {
            val resp = obdBluetoothManager.fetchData("01 05")
            binding.combinedScope.addDataPoint("water_temp", obdBluetoothManager.parseWaterTemp(resp))
        }
    }

    private fun generateMockData(time: Float) {
        if (settingsManager.isEnabled("rpm")) {
            val throttle = (sin(time * 0.5f) * 50f + 50f).coerceIn(0f, 100f)
            val rpm = (throttle * 60f + 800f + (sin(time * 2f) * 200f)).coerceIn(800f, 7500f)
            binding.combinedScope.addDataPoint("rpm", rpm)
        }
        if (settingsManager.isEnabled("speed")) {
            val speed = ((time * 5f) % 160f).coerceIn(0f, 180f)
            binding.combinedScope.addDataPoint("speed", speed)
        }
        if (settingsManager.isEnabled("throttle")) {
            val throttle = (sin(time * 0.5f) * 50f + 50f).coerceIn(0f, 100f)
            binding.combinedScope.addDataPoint("throttle", throttle)
        }
        if (settingsManager.isEnabled("water_temp")) {
            val temp = 85f + sin(time * 0.1f) * 5f
            binding.combinedScope.addDataPoint("water_temp", temp)
        }
        if (settingsManager.isEnabled("voltage")) {
            val voltage = 13.8f + sin(time * 0.3f) * 0.5f
            binding.combinedScope.addDataPoint("voltage", voltage)
        }
        if (settingsManager.isEnabled("load")) {
            val load = 20f + sin(time * 0.4f) * 60f
            binding.combinedScope.addDataPoint("load", load)
        }
        if (settingsManager.isEnabled("map")) {
            val map = 30f + sin(time * 0.6f) * 70f
            binding.combinedScope.addDataPoint("map", map)
        }
        if (settingsManager.isEnabled("maf")) {
            val maf = 5f + sin(time * 0.8f) * 40f
            binding.combinedScope.addDataPoint("maf", maf)
        }
    }

    private fun setupScope() {
        binding.combinedScope.apply {
            clearSeries()
            SettingsManager.ALL_ITEMS.forEach { item ->
                if (settingsManager.isEnabled(item.id)) {
                    addSeries(
                        item.id,
                        item.label,
                        item.maxValue,
                        settingsManager.getColor(item.id),
                        item.decimalPlaces
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
