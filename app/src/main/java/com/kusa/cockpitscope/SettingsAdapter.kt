package com.kusa.cockpitscope

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.kusa.cockpitscope.databinding.ItemSettingBinding

class SettingsAdapter(
    private val settingsManager: SettingsManager,
    private val items: List<DisplayItem>
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    private val colors = listOf(
        Color.parseColor("#FF4444"), // Red
        Color.parseColor("#44FF44"), // Green
        Color.parseColor("#4444FF"), // Blue
        Color.parseColor("#FFFF44"), // Yellow
        Color.parseColor("#44FFFF"), // Cyan
        Color.parseColor("#FF44FF"), // Magenta
        Color.parseColor("#FFFFFF"), // White
        Color.parseColor("#AAAAAA")  // Gray
    )
    private val colorNames = listOf("赤 (Red)", "緑 (Green)", "青 (Blue)", "黄 (Yellow)", "水色 (Cyan)", "紫 (Magenta)", "白 (White)", "灰色 (Gray)")

    class ViewHolder(val binding: ItemSettingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.textLabel.text = item.label
        holder.binding.checkboxEnabled.isChecked = settingsManager.isEnabled(item.id)
        holder.binding.viewColor.setBackgroundColor(settingsManager.getColor(item.id))

        holder.binding.checkboxEnabled.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && settingsManager.getEnabledCount() >= 9) {
                holder.binding.checkboxEnabled.isChecked = false
                Toast.makeText(holder.itemView.context, "最大9個までしか選択できません", Toast.LENGTH_SHORT).show()
                return@setOnCheckedChangeListener
            }
            settingsManager.setEnabled(item.id, isChecked)
        }

        holder.binding.viewColor.setOnClickListener {
            AlertDialog.Builder(it.context)
                .setTitle("Select Color")
                .setItems(colorNames.toTypedArray()) { _, which ->
                    val selectedColor = colors[which]
                    settingsManager.setColor(item.id, selectedColor)
                    holder.binding.viewColor.setBackgroundColor(selectedColor)
                }
                .show()
        }
    }

    override fun getItemCount() = items.size
}
