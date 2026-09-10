package com.kusa.cockpitscope

import android.content.Context
import android.graphics.Color
import androidx.annotation.StringRes

data class DisplayItem(
    val id: String,
    @StringRes val labelResId: Int,
    val label: String,
    val maxValue: Float,
    val defaultColor: Int,
    val unit: String = "",
    val decimalPlaces: Int = 0
) {
    fun getLabel(context: Context): String {
        return if (labelResId != 0) context.getString(labelResId) else label
    }
}

class SettingsManager(context: Context) {
    private val prefs = context.getSharedPreferences("display_settings", Context.MODE_PRIVATE)

    companion object {
        val ALL_ITEMS = listOf(
            DisplayItem("rpm", R.string.metric_rpm, "Engine Speed (RPM)", 8000f, Color.parseColor("#FF4444")),
            DisplayItem("speed", R.string.metric_speed, "Vehicle Speed", 200f, Color.parseColor("#44FF44"), "km/h"),
            DisplayItem("throttle", R.string.metric_throttle, "Throttle Position", 100f, Color.parseColor("#FFFF44"), "%"),
            DisplayItem("water_temp", R.string.metric_water_temp, "Coolant Temp", 120f, Color.parseColor("#44FFFF"), "°C"),
            DisplayItem("voltage", R.string.metric_voltage, "Battery Voltage", 16f, Color.parseColor("#FF44FF"), "V", 1),
            DisplayItem("load", R.string.metric_load, "Engine Load", 100f, Color.parseColor("#AAAAAA"), "%"),
            DisplayItem("map", R.string.metric_map, "Manifold Pressure (MAP)", 255f, Color.parseColor("#4444FF"), "kPa"),
            DisplayItem("maf", R.string.metric_maf, "Mass Air Flow (MAF)", 100f, Color.parseColor("#FFFFFF"), "g/s", 1),
            DisplayItem("gforce", R.string.metric_gforce, "G-Force", 2f, Color.parseColor("#FF8800"), "G", 1),
            DisplayItem("intake_air_temp", R.string.metric_intake_air_temp, "Intake Air Temp", 100f, Color.parseColor("#0088FF"), "°C"),
            DisplayItem("timing_advance", R.string.metric_timing_advance, "Timing Advance", 60f, Color.parseColor("#FFCC00"), "°"),
            DisplayItem("ambient_air_temp", R.string.metric_ambient_air_temp, "Ambient Air Temp", 50f, Color.parseColor("#88FF00"), "°C"),
            DisplayItem("fuel_level", R.string.metric_fuel_level, "Fuel Level", 100f, Color.parseColor("#FF0088"), "%"),
            DisplayItem("baro_pressure", R.string.metric_baro_pressure, "Barometric Pressure", 110f, Color.parseColor("#00FF88"), "kPa")
        )
    }

    fun getEnabledCount(): Int {
        return ALL_ITEMS.count { isEnabled(it.id) }
    }

    fun isEnabled(id: String): Boolean {
        val default = when (id) {
            "rpm", "speed", "throttle" -> true
            else -> false
        }
        return prefs.getBoolean("enabled_$id", default)
    }

    fun setEnabled(id: String, enabled: Boolean) {
        prefs.edit().putBoolean("enabled_$id", enabled).apply()
    }

    fun getColor(id: String): Int {
        val item = ALL_ITEMS.find { it.id == id }
        val defaultColor = item?.defaultColor ?: Color.WHITE
        return prefs.getInt("color_$id", defaultColor)
    }

    fun setColor(id: String, color: Int) {
        prefs.edit().putInt("color_$id", color).apply()
    }

    var selectedDeviceAddress: String?
        get() = prefs.getString("selected_device_address", null)
        set(value) = prefs.edit().putString("selected_device_address", value).apply()
}
