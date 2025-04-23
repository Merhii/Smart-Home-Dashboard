
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
import android.widget.Toast
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

private lateinit var switchLight: Switch
private lateinit var sharedPreferences: SharedPreferences
private lateinit var electricityPrefs: SharedPreferences
private var electricHandler: Handler? = null
private val client = OkHttpClient()
private const val LIGHT_CONSUMPTION_KWH_PER_5MIN = 0.166f
private const val TOTAL_CONSUMPTION_KEY = "TotalConsumption"

class ElectricActivity : ComponentActivity() {
    private val roomStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            switchLight.isChecked = sharedPreferences.getBoolean("Electric_Lights", false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.electric)

        sharedPreferences = getSharedPreferences("RoomState", Context.MODE_PRIVATE)
        electricityPrefs = getSharedPreferences("ElectricityData", Context.MODE_PRIVATE)

        registerReceiver(roomStateReceiver, IntentFilter("com.example.myapplication.ROOM_STATE_UPDATED"), RECEIVER_NOT_EXPORTED)

        switchLight = findViewById(R.id.switchlights)
        switchLight.isChecked = sharedPreferences.getBoolean("Electric_Lights", false)

        handleTracking()

        switchLight.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Electric_Lights", isChecked).apply() // ✅ Add this!

            if (isChecked) {
                sendRequest(IP2.acOn)
                println(IP2.acOn)
            } else {
                sendRequest(IP2.acOff)
            }

            handleTracking() // Optional: to restart handler immediately
        }

    }

    private fun sendRequest(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ElectricActivity, "Failed to send request", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    runOnUiThread {
                        Toast.makeText(this@ElectricActivity, "Request Sent Successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
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
            if (electricHandler == null) electricHandler = Handler(Looper.getMainLooper())
            electricHandler?.removeCallbacksAndMessages(null)

            val updateTask = object : Runnable {
                override fun run() {
                    if (!switchLight.isChecked) {
                        electricHandler?.removeCallbacksAndMessages(null)
                        electricHandler = null
                        return
                    }
                    var current = electricityPrefs.getFloat(TOTAL_CONSUMPTION_KEY, 0f)
                    current += LIGHT_CONSUMPTION_KWH_PER_5MIN
                    electricityPrefs.edit().putFloat(TOTAL_CONSUMPTION_KEY, current).apply()
                    electricHandler?.postDelayed(this, 300000)
                }
            }
            electricHandler?.post(updateTask)
        } else {
            electricHandler?.removeCallbacksAndMessages(null)
            electricHandler = null
        }
    }


}
