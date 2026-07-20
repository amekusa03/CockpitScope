package com.kusa.cockpitscope

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.kusa.cockpitscope.databinding.FragmentFirstBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.sin

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private lateinit var settingsManager: SettingsManager
    private lateinit var deviceSensorManager: DeviceSensorManager
    private val obdBluetoothManager = ObdBluetoothManager()
    private var isObdConnected = false

    private lateinit var logger: TelemetryLogger
    private var isRecording = false
    private var isReplaying = false
    private var dataJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsManager = SettingsManager(requireContext())
        deviceSensorManager = DeviceSensorManager(requireContext())
        logger = TelemetryLogger(requireContext())

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // MainActivityで既にinflateされているが、
        // タイトルを動的に変更するために参照する
        updateMenuTitles(menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_record -> {
                if (isReplaying) {
                    Toast.makeText(context, "Cannot record during replay", Toast.LENGTH_SHORT).show()
                } else {
                    if (isRecording) stopRecording() else startRecording()
                    activity?.invalidateOptionsMenu()
                }
                true
            }
            R.id.action_replay -> {
                if (isRecording) {
                    Toast.makeText(context, "Cannot replay during recording", Toast.LENGTH_SHORT).show()
                } else {
                    if (isReplaying) stopReplaying() else startReplaying()
                    activity?.invalidateOptionsMenu()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateMenuTitles(menu: Menu) {
        menu.findItem(R.id.action_record)?.title = if (isRecording) "Stop Recording" else "Record (REC)"
        menu.findItem(R.id.action_replay)?.title = if (isReplaying) "Stop Replay" else "Replay"
    }

    private fun startRecording() {
        val path = logger.startLogging()
        if (path != null) {
            isRecording = true
            binding.tvStatus.text = "Recording: ${File(path).name}"
        }
    }

    private fun stopRecording() {
        logger.stopLogging()
        isRecording = false
        binding.tvStatus.text = "Saved"
    }

    private fun startDataCycle() {
        dataJob?.cancel()
        dataJob = viewLifecycleOwner.lifecycleScope.launch {
            val address = settingsManager.selectedDeviceAddress
            if (address != null) {
                isObdConnected = obdBluetoothManager.connect(address)
            }

            var time = 0f
            while (true) {
                val dataMap = mutableMapOf<String, Float>()
                
                if (isObdConnected) {
                    fetchRealData(dataMap)
                } else {
                    generateMockData(time, dataMap)
                }

                if (settingsManager.isEnabled("gforce")) {
                    dataMap["gforce"] = deviceSensorManager.currentGForce
                }

                dataMap.forEach { (id, value) ->
                    binding.combinedScope.addDataPoint(id, value)
                    if (isRecording) logger.logData(id, value)
                }

                time += 0.05f
                delay(if (isObdConnected) 100 else 30)
            }
        }
    }

    private suspend fun fetchRealData(outData: MutableMap<String, Float>) {
        val pids = mapOf("rpm" to "01 0C", "speed" to "01 0D", "throttle" to "01 11", "water_temp" to "01 05")
        pids.forEach { (id, pid) ->
            if (settingsManager.isEnabled(id)) {
                val resp = obdBluetoothManager.fetchData(pid)
                val value = when(id) {
                    "rpm" -> obdBluetoothManager.parseRpm(resp)
                    "speed" -> obdBluetoothManager.parseSpeed(resp)
                    "throttle" -> obdBluetoothManager.parseThrottle(resp)
                    else -> obdBluetoothManager.parseWaterTemp(resp)
                }
                outData[id] = value
            }
        }
    }

    private fun generateMockData(time: Float, outData: MutableMap<String, Float>) {
        val throttle = (sin(time * 0.5f) * 50f + 50f).coerceIn(0f, 100f)
        if (settingsManager.isEnabled("rpm")) outData["rpm"] = (throttle * 60f + 800f + (sin(time * 2f) * 200f)).coerceIn(800f, 7500f)
        if (settingsManager.isEnabled("speed")) outData["speed"] = ((time * 5f) % 160f).coerceIn(0f, 180f)
        if (settingsManager.isEnabled("throttle")) outData["throttle"] = throttle
        if (settingsManager.isEnabled("water_temp")) outData["water_temp"] = 85f + sin(time * 0.1f) * 5f
        if (settingsManager.isEnabled("voltage")) outData["voltage"] = 13.8f + sin(time * 0.3f) * 0.5f
        if (settingsManager.isEnabled("load")) outData["load"] = 20f + sin(time * 0.4f) * 60f
        if (settingsManager.isEnabled("map")) outData["map"] = 30f + sin(time * 0.6f) * 70f
        if (settingsManager.isEnabled("maf")) outData["maf"] = 5f + sin(time * 0.8f) * 40f
    }

    private fun startReplaying() {
        val logs = logger.getLogs()
        if (logs.isEmpty()) {
            Toast.makeText(context, "No logs", Toast.LENGTH_SHORT).show()
            return
        }
        val lastLog = logs.maxByOrNull { it.lastModified() } ?: return
        isReplaying = true
        binding.tvStatus.text = "Replay: ${lastLog.name}"
        
        dataJob?.cancel()
        dataJob = viewLifecycleOwner.lifecycleScope.launch {
            val lines = lastLog.readLines().drop(1)
            var currentLine = 0
            while (currentLine < lines.size && isReplaying) {
                val line = lines[currentLine]
                if (line.isBlank()) {
                    currentLine++
                    continue
                }
                val parts = line.split(",")
                val firstTs = parts[0].toLong()
                while (currentLine < lines.size) {
                    val p = lines[currentLine].split(",")
                    if (p.size < 3 || p[0].toLong() != firstTs) break
                    binding.combinedScope.addDataPoint(p[1], p[2].toFloat())
                    currentLine++
                }
                delay(30)
            }
            stopReplaying()
            activity?.invalidateOptionsMenu()
        }
    }

    private fun stopReplaying() {
        isReplaying = false
        binding.tvStatus.text = ""
        startDataCycle()
    }

    private fun setupScope() {
        binding.combinedScope.apply {
            clearSeries()
            SettingsManager.ALL_ITEMS.forEach { item ->
                if (settingsManager.isEnabled(item.id)) {
                    addSeries(item.id, item.label, item.maxValue, settingsManager.getColor(item.id), item.decimalPlaces)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logger.stopLogging()
        _binding = null
    }
}
