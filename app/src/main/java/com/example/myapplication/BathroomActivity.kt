package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.RetrofitInstance.apiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private lateinit var Lswitch: Switch
private lateinit var Hswitch: Switch
private lateinit var tvTemperature: TextView
private lateinit var tempProgressBar: ProgressBar
private var isHeaterOn = false

class BathroomActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.bathroom)

        val deviceLocation = intent.getStringExtra("loc")
        Lswitch = findViewById(R.id.switchlights)
        Hswitch = findViewById(R.id.switchHeater)
        tvTemperature = findViewById(R.id.tvTemperature)
        tempProgressBar = findViewById(R.id.tempProgressBar)

        // Lights Switch Listener
        Lswitch.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) 1 else 0
            println(status)
            if (deviceLocation != null) {
                updateDeviceStatus("Lights", deviceLocation, status)
            }
        }

        // Heater Switch Listener
        Hswitch.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) 1 else 0
            println(status)
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
