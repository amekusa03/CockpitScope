package com.kusa.cockpitscope

import android.content.Context
import android.graphics.Color

data class DisplayItem(
    val id: String,
    val label: String,
    val maxValue: Float,
    val defaultColor: Int,
    val unit: String = "",
    val decimalPlaces: Int = 0
)

class SettingsManager(context: Context) {
    private val prefs = context.getSharedPreferences("display_settings", Context.MODE_PRIVATE)

    companion object {
        val ALL_ITEMS = listOf(
            DisplayItem("rpm", "エンジン回転数 (RPM)", 8000f, Color.parseColor("#FF4444")),
            DisplayItem("speed", "車速 (Speed)", 200f, Color.parseColor("#44FF44"), "km/h"),
            DisplayItem("throttle", "アクセル開度 (Throttle)", 100f, Color.parseColor("#FFFF44"), "%"),
            DisplayItem("water_temp", "水温 (Water Temp)", 120f, Color.parseColor("#44FFFF"), "°C"),
            DisplayItem("voltage", "電圧 (Voltage)", 16f, Color.parseColor("#FF44FF"), "V", 1),
            DisplayItem("load", "エンジン負荷 (Load)", 100f, Color.parseColor("#AAAAAA"), "%"),
            DisplayItem("map", "インマニ圧 (MAP)", 255f, Color.parseColor("#4444FF"), "kPa"),
            DisplayItem("maf", "吸入空気量 (MAF)", 100f, Color.parseColor("#FFFFFF"), "g/s", 1),
            DisplayItem("gforce", "加速度 (G-Force)", 2f, Color.parseColor("#FF8800"), "G", 1)
        )
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
