package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

private lateinit var Lswitch: Switch
private lateinit var Hswitch: Switch
private lateinit var tvTemperature: TextView
private lateinit var tempProgressBar: ProgressBar
private lateinit var sharedPreferences: SharedPreferences
private lateinit var electricityPrefs: SharedPreferences
private var lightOn = false
private var heaterOn = false
private var bathroomHandler: Handler? = null
private const val LIGHT_CONSUMPTION_KWH_PER_5MIN = 0.00083f
private const val HEATER_CONSUMPTION_KWH_PER_5MIN = 0.083f
private const val TOTAL_CONSUMPTION_KEY = "TotalConsumption"





class BathroomActivity : ComponentActivity() {
    private val roomStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Lswitch.isChecked = sharedPreferences.getBoolean("Bathroom_Lights", false)
            Hswitch.isChecked = sharedPreferences.getBoolean("Bathroom_Heater", false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.bathroom)

        sharedPreferences = getSharedPreferences("RoomState", Context.MODE_PRIVATE)
        electricityPrefs = getSharedPreferences("ElectricityData", Context.MODE_PRIVATE)

        registerReceiver(
            roomStateReceiver,
            IntentFilter("com.example.myapplication.ROOM_STATE_UPDATED"),
            RECEIVER_NOT_EXPORTED
        )

        val deviceLocation = intent.getStringExtra("loc")
        Lswitch = findViewById(R.id.switchlights)
        Hswitch = findViewById(R.id.switchHeater)
        tvTemperature = findViewById(R.id.tvTemperature)
        tempProgressBar = findViewById(R.id.tempProgressBar)

        lightOn = sharedPreferences.getBoolean("Bathroom_Lights", false)
        heaterOn = sharedPreferences.getBoolean("Bathroom_Heater", false)
        handleTracking()


        Lswitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Bathroom_Lights", isChecked).apply()
            updateDeviceStatus("Lights", deviceLocation ?: "Bathroom", if (isChecked) 1 else 0)
            handleTracking()
        }

        Hswitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Bathroom_Heater", isChecked).apply()
            updateDeviceStatus("Heater", deviceLocation ?: "Bathroom", if (isChecked) 1 else 0)

            if (isChecked) startHeatingSimulation() else resetTemperature()
            handleTracking()
        }
    }
    override fun onResume() {
        super.onResume()
        Lswitch.isChecked = sharedPreferences.getBoolean("Bathroom_Lights", false)
        Hswitch.isChecked = sharedPreferences.getBoolean("Bathroom_Heater", false)
        handleTracking()
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(roomStateReceiver)
    }

    private fun handleTracking() {
        // Read actual switch state every time for safety
        val lightState = sharedPreferences.getBoolean("Bathroom_Lights", false)
        val heaterState = sharedPreferences.getBoolean("Bathroom_Heater", false)

        if (lightState || heaterState) {
            if (bathroomHandler == null) bathroomHandler = Handler(Looper.getMainLooper())
            bathroomHandler?.removeCallbacksAndMessages(null)

            val updateTask = object : Runnable {
                override fun run() {
                    val lightOn = sharedPreferences.getBoolean("Bathroom_Lights", false)
                    val heaterOn = sharedPreferences.getBoolean("Bathroom_Heater", false)

                    if (!lightOn && !heaterOn) {
                        bathroomHandler?.removeCallbacksAndMessages(null)
                        bathroomHandler = null
                        return
                    }

                    var current = electricityPrefs.getFloat(TOTAL_CONSUMPTION_KEY, 0f)
                    if (lightOn) current += LIGHT_CONSUMPTION_KWH_PER_5MIN
                    if (heaterOn) current += HEATER_CONSUMPTION_KWH_PER_5MIN
                    electricityPrefs.edit().putFloat(TOTAL_CONSUMPTION_KEY, current).apply()

                    bathroomHandler?.postDelayed(this, 300000)
                }
            }
            bathroomHandler?.post(updateTask)
        } else {
            bathroomHandler?.removeCallbacksAndMessages(null)
            bathroomHandler = null
        }
    }




    private fun updateDeviceStatus(deviceName: String, deviceLocation: String, status: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = apiService.updateDeviceStatus(deviceLocation, deviceName, status)
                val msg = if (response.isExecuted) "$deviceName updated successfully" else "Failed to update $deviceName"
                Toast.makeText(this@BathroomActivity, msg, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@BathroomActivity, "$deviceName updated (offline)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startHeatingSimulation() {
        lifecycleScope.launch {
            var temperature = 25
            while (Hswitch.isChecked && temperature <= 50) {
                tvTemperature.text = "$temperature °C"
                tempProgressBar.progress = temperature
                delay(1000)
                temperature += 1
            }
        }
    }

    private fun resetTemperature() {
        tvTemperature.text = "-- °C"
        tempProgressBar.progress = 0
    }
}
