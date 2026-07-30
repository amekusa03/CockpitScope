package com.kusa.cockpitscope

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class ObdBluetoothManager {
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }

    @SuppressLint("MissingPermission")
    suspend fun connect(address: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val device = bluetoothAdapter?.getRemoteDevice(address) ?: return@withContext false
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket?.connect()
            inputStream = socket?.inputStream
            outputStream = socket?.outputStream
            
            // Initialize ELM327
            initializeElm327()
            true
        } catch (e: IOException) {
            Log.e("ObdBluetooth", "Connection failed", e)
            close()
            false
        }
    }

    private fun sendCommand(command: String) {
        try {
            outputStream?.write((command + "\r").toByteArray())
            outputStream?.flush()
        } catch (e: IOException) {
            Log.e("ObdBluetooth", "Send failed", e)
        }
    }

    private fun readResponse(): String {
        val buffer = StringBuilder()
        try {
            var char: Int
            while (true) {
                char = inputStream?.read() ?: -1
                if (char == -1) break
                val c = char.toChar()
                if (c == '>') break
                buffer.append(c)
            }
        } catch (e: IOException) {
            Log.e("ObdBluetooth", "Read failed", e)
        }
        return buffer.toString().trim()
    }

    private fun initializeElm327() {
        sendCommand("AT Z") // Reset
        Thread.sleep(1000)
        readResponse()
        sendCommand("AT E0") // Echo off
        readResponse()
        sendCommand("AT L0") // Linefeeds off
        readResponse()
        sendCommand("AT SP 0") // Protocol auto
        readResponse()
    }

    suspend fun fetchData(pid: String): String = withContext(Dispatchers.IO) {
        sendCommand(pid)
        readResponse()
    }

    fun parseRpm(response: String): Float {
        // Expected: "41 0C AA BB" -> (AA*256 + BB) / 4
        return parseHex(response, "0C", 2) / 4f
    }

    fun parseSpeed(response: String): Float {
        // Expected: "41 0D AA" -> AA
        return parseHex(response, "0D", 1)
    }

    fun parseThrottle(response: String): Float {
        // Expected: "41 11 AA" -> AA * 100 / 255
        return parseHex(response, "11", 1) * 100f / 255f
    }

    fun parseWaterTemp(response: String): Float {
        // Expected: "41 05 AA" -> AA - 40
        return parseHex(response, "05", 1) - 40f
    }

    fun parseIntakeAirTemp(response: String): Float {
        // Expected: "41 0F AA" -> AA - 40
        return parseHex(response, "0F", 1) - 40f
    }

    fun parseTimingAdvance(response: String): Float {
        // Expected: "41 0E AA" -> AA / 2 - 64
        return parseHex(response, "0E", 1) / 2f - 64f
    }

    fun parseAmbientAirTemp(response: String): Float {
        // Expected: "41 46 AA" -> AA - 40
        return parseHex(response, "46", 1) - 40f
    }

    fun parseFuelLevel(response: String): Float {
        // Expected: "41 2F AA" -> AA * 100 / 255
        return parseHex(response, "2F", 1) * 100f / 255f
    }

    fun parseBaroPressure(response: String): Float {
        // Expected: "41 33 AA" -> AA
        return parseHex(response, "33", 1)
    }

    private fun parseHex(response: String, pid: String, bytes: Int): Float {
        try {
            val clean = response.replace(" ", "")
            val header = "41$pid"
            val index = clean.indexOf(header)
            if (index != -1) {
                val data = clean.substring(index + header.length, index + header.length + bytes * 2)
                return data.toInt(16).toFloat()
            }
        } catch (e: Exception) {
            Log.e("ObdBluetooth", "Parse error for PID $pid: $response", e)
        }
        return 0f
    }

    fun isConnected(): Boolean {
        return socket?.isConnected ?: false
    }

    fun close() {
        try {
            socket?.close()
        } catch (e: IOException) {}
        socket = null
        inputStream = null
        outputStream = null
    }
}
