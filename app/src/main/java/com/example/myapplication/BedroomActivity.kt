package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.DTO.RegisterUserDto
import com.example.myapplication.Entity.User
import com.example.myapplication.RetrofitInstance.apiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import android.content.SharedPreferences

private lateinit var Lswitch: Switch
private lateinit var Cswitch: Switch

class BedroomActivity : ComponentActivity() {
    private val roomStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Refresh UI based on updated RoomState
            Lswitch.isChecked = sharedPreferences.getBoolean("Bedroom_Lights", false)
            Cswitch.isChecked = sharedPreferences.getBoolean("Bedroom_Curtains", false)
        }
    }

    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.bedroom)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("RoomState", Context.MODE_PRIVATE)

        // Register the BroadcastReceiver
        registerReceiver(roomStateReceiver, IntentFilter("com.example.myapplication.ROOM_STATE_UPDATED"),
            RECEIVER_NOT_EXPORTED
        )

        val deviceLocation = intent.getStringExtra("loc")

        Lswitch = findViewById(R.id.switchlights)
        Cswitch = findViewById(R.id.switchcurtains)

        // Restore switch states from SharedPreferences
        Lswitch.isChecked = sharedPreferences.getBoolean("Bedroom_Lights", false)
        Cswitch.isChecked = sharedPreferences.getBoolean("Bedroom_Curtains", false)

        Cswitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Bedroom_Curtains", isChecked).apply()
            val status = if (isChecked) 1 else 0
            if (deviceLocation != null) {
                updateDeviceStatus("Curtains", deviceLocation, status)
            }
        }

        Lswitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Bedroom_Lights", isChecked).apply()
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
        // Launch a coroutine to make the network call asynchronously
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = apiService.updateDeviceStatus(deviceLocation, deviceName, status)

                if (response.isExecuted) {
                    Toast.makeText(this@BedroomActivity, "$deviceName status updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@BedroomActivity, "Failed to update device status", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BedroomActivity, "$deviceName status updated successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }
}