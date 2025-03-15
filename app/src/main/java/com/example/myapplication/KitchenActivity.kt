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

private lateinit var Lswitch: Switch

class KitchenActivity : ComponentActivity() {
    private val roomStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Refresh UI based on updated RoomState
            Lswitch.isChecked = sharedPreferences.getBoolean("Kitchen_Lights", false)
        }
    }

    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.kitchen)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("RoomState", Context.MODE_PRIVATE)

        // Register the BroadcastReceiver
        registerReceiver(roomStateReceiver, IntentFilter("com.example.myapplication.ROOM_STATE_UPDATED"),
            RECEIVER_NOT_EXPORTED
        )

        val deviceLocation = intent.getStringExtra("loc")
        Lswitch = findViewById(R.id.switchlights)

        // Restore switch state from SharedPreferences
        Lswitch.isChecked = sharedPreferences.getBoolean("Kitchen_Lights", false)

        Lswitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Kitchen_Lights", isChecked).apply()
            val status = if (isChecked) 1 else 0
            if (deviceLocation != null) {
                updateDeviceStatus("Lights", deviceLocation, status)
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
                    Toast.makeText(this@KitchenActivity, "$deviceName status updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@KitchenActivity, "Failed to update device status", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@KitchenActivity, "$deviceName status updated successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }
}