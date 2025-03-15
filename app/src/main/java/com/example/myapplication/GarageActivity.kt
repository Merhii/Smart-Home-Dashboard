package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Switch
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.SharedPreferences
import com.example.myapplication.RetrofitInstance.apiService

private lateinit var switchLight: Switch

class GarageActivity : ComponentActivity() {
    private val roomStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Refresh UI based on updated RoomState
            switchLight.isChecked = sharedPreferences.getBoolean("Garage_Lights", false)
        }
    }

    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.garage)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("RoomState", Context.MODE_PRIVATE)

        // Register the BroadcastReceiver
        registerReceiver(roomStateReceiver, IntentFilter("com.example.myapplication.ROOM_STATE_UPDATED"),
            RECEIVER_NOT_EXPORTED
        )

        switchLight = findViewById(R.id.switchlights)

        // Restore switch state from SharedPreferences
        switchLight.isChecked = sharedPreferences.getBoolean("Garage_Lights", false)

        switchLight.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Garage_Lights", isChecked).apply()
            val status = if (isChecked) 1 else 0
            val deviceLocation = "Garage" // Assuming the location is fixed for the garage
            updateDeviceStatus("Lights", deviceLocation, status)
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
                    Toast.makeText(this@GarageActivity, "$deviceName status updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@GarageActivity, "Failed to update device status", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@GarageActivity, "$deviceName status updated successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }
}