package com.kusa.cockpitscope

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kusa.cockpitscope.databinding.FragmentSecondBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var settingsManager: SettingsManager
    private val obdBluetoothManager = ObdBluetoothManager()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsManager = SettingsManager(requireContext())

        val adapter = SettingsAdapter(settingsManager, SettingsManager.ALL_ITEMS)
        binding.recyclerSettings.adapter = adapter

        updateDeviceLabel()

        binding.textBtDevice.setOnClickListener {
            showDevicePicker()
        }

        binding.buttonSave.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun updateDeviceLabel() {
        val address = settingsManager.selectedDeviceAddress
        if (address != null) {
            binding.textBtDevice.text = getString(R.string.connected_device, address)
        } else {
            binding.textBtDevice.text = getString(R.string.select_bt_device)
        }
    }

    @SuppressLint("MissingPermission")
    private fun showDevicePicker() {
        val devices = obdBluetoothManager.getPairedDevices()
        if (devices.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.no_paired_devices), Toast.LENGTH_SHORT).show()
            return
        }

        val names = devices.map { "${it.name ?: "Unknown"}\n${it.address}" }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_device_title))
            .setItems(names) { _, which ->
                val selected = devices[which]
                settingsManager.selectedDeviceAddress = selected.address
                updateDeviceLabel()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
