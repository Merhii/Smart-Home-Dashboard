// --- GarageActivity.kt ---
package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Switch
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import android.content.SharedPreferences

private lateinit var switchLight: Switch
private lateinit var sharedPreferences: SharedPreferences
private lateinit var electricityPrefs: SharedPreferences
private var garageHandler: Handler? = null
private const val LIGHT_CONSUMPTION_KWH_PER_5MIN = 0.00083f
private const val TOTAL_CONSUMPTION_KEY = "TotalConsumption"

class GarageActivity : ComponentActivity() {
    private val roomStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            switchLight.isChecked = sharedPreferences.getBoolean("Garage_Lights", false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.garage)

        sharedPreferences = getSharedPreferences("RoomState", Context.MODE_PRIVATE)
        electricityPrefs = getSharedPreferences("ElectricityData", Context.MODE_PRIVATE)

        registerReceiver(roomStateReceiver, IntentFilter("com.example.myapplication.ROOM_STATE_UPDATED"), RECEIVER_NOT_EXPORTED)

        switchLight = findViewById(R.id.switchlights)
        switchLight.isChecked = sharedPreferences.getBoolean("Garage_Lights", false)

        handleTracking()

        switchLight.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Garage_Lights", isChecked).apply()
            handleTracking()
        }
    }

    override fun onResume() {
        super.onResume()
        handleTracking()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(roomStateReceiver)
    }

    private fun handleTracking() {
        if (switchLight.isChecked) {
            if (garageHandler == null) garageHandler = Handler(Looper.getMainLooper())
            garageHandler?.removeCallbacksAndMessages(null)

            val updateTask = object : Runnable {
                override fun run() {
                    if (!switchLight.isChecked) {
                        garageHandler?.removeCallbacksAndMessages(null)
                        garageHandler = null
                        return
                    }
                    var current = electricityPrefs.getFloat(TOTAL_CONSUMPTION_KEY, 0f)
                    current += LIGHT_CONSUMPTION_KWH_PER_5MIN
                    electricityPrefs.edit().putFloat(TOTAL_CONSUMPTION_KEY, current).apply()
                    garageHandler?.postDelayed(this, 300000)
                }
            }
            garageHandler?.post(updateTask)
        } else {
            garageHandler?.removeCallbacksAndMessages(null)
            garageHandler = null
        }
    }

}
