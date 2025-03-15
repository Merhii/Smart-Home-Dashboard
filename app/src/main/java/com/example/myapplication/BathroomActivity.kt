package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.RetrofitInstance.apiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.SharedPreferences

private lateinit var Lswitch: Switch
private lateinit var Hswitch: Switch
private lateinit var tvTemperature: TextView
private lateinit var tempProgressBar: ProgressBar
private var isHeaterOn = false

class BathroomActivity : ComponentActivity() {
    private val roomStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Refresh UI based on updated RoomState
            Lswitch.isChecked = sharedPreferences.getBoolean("Bathroom_Lights", false)
            Hswitch.isChecked = sharedPreferences.getBoolean("Bathroom_Heater", false)
        }
    }

    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.bathroom)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("RoomState", Context.MODE_PRIVATE)

        // Register the BroadcastReceiver
        registerReceiver(roomStateReceiver, IntentFilter("com.example.myapplication.ROOM_STATE_UPDATED"),
            RECEIVER_NOT_EXPORTED
        )

        val deviceLocation = intent.getStringExtra("loc")
        Lswitch = findViewById(R.id.switchlights)
        Hswitch = findViewById(R.id.switchHeater)
        tvTemperature = findViewById(R.id.tvTemperature)
        tempProgressBar = findViewById(R.id.tempProgressBar)

        // Restore switch states from SharedPreferences
        Lswitch.isChecked = sharedPreferences.getBoolean("Bathroom_Lights", false)
        Hswitch.isChecked = sharedPreferences.getBoolean("Bathroom_Heater", false)

        // Lights Switch Listener
        Lswitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Bathroom_Lights", isChecked).apply()
            val status = if (isChecked) 1 else 0
            if (deviceLocation != null) {
                updateDeviceStatus("Lights", deviceLocation, status)
            }
        }

        // Heater Switch Listener
        Hswitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Bathroom_Heater", isChecked).apply()
            val status = if (isChecked) 1 else 0
            if (deviceLocation != null) {
                updateDeviceStatus("Heater", deviceLocation, status)
            }

            // Start/Stop temperature monitoring based on heater status
            isHeaterOn = isChecked
            if (isChecked) {
                startHeatingSimulation()
            } else {
                resetTemperature()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the BroadcastReceiver
        unregisterReceiver(roomStateReceiver)
    }

    private fun updateDeviceStatus(deviceName: String, deviceLocation: String, status: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = apiService.updateDeviceStatus(deviceLocation, deviceName, status)
                if (response.isExecuted) {
                    Toast.makeText(this@BathroomActivity, "$deviceName status updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@BathroomActivity, "Failed to update device status", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BathroomActivity, "$deviceName status updated successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Simulate temperature rise when heater is ON
    private fun startHeatingSimulation() {
        lifecycleScope.launch {
            var temperature = 25  // Starting temperature
            while (isHeaterOn && temperature <= 50) {  // Simulate heating up to 50°C
                tvTemperature.text = "$temperature °C"
                tempProgressBar.progress = temperature  // Update progress bar
                delay(1000) // Update every second
                temperature += 1
            }
        }
    }

    private fun resetTemperature() {
        tvTemperature.text = "-- °C"
        tempProgressBar.progress = 0
    }
}