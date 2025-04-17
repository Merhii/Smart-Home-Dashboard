package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
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
import android.os.Handler

private var lightOn = false
private var handler: Handler? = null
private lateinit var electricityPrefs: SharedPreferences

private val LAMP_CONSUMPTION_KWH_PER_5MIN = 0.00083f
private val BILL_RATE = 0.1475f
private val TOTAL_CONSUMPTION_KEY = "TotalConsumption"

private lateinit var switchLight: Switch



class EntranceActivity : ComponentActivity() {
    private val roomStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Refresh UI based on updated RoomState
            switchLight.isChecked = sharedPreferences.getBoolean("Entrance_Lights", false)
        }
    }
    companion object {
        const val ENTRANCE_LIGHT_CONSUMPTION_PER_HOUR = 0.06f // 60W = 0.06 kWh
        const val SHARED_PREF_ELECTRICITY = "ElectricityData"
        const val TOTAL_CONSUMPTION_KEY = "TotalConsumption"
    }

    private lateinit var sharedPreferences: SharedPreferences
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        electricityPrefs = getSharedPreferences("ElectricityData", Context.MODE_PRIVATE)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.entrance)
        electricityPrefs = getSharedPreferences(SHARED_PREF_ELECTRICITY, Context.MODE_PRIVATE)


        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("RoomState", Context.MODE_PRIVATE)

        // Register the BroadcastReceiver
        registerReceiver(roomStateReceiver, IntentFilter("com.example.myapplication.ROOM_STATE_UPDATED"),
            RECEIVER_NOT_EXPORTED
        )
        switchLight = findViewById(R.id.switchLight)
        switchLight.isChecked = sharedPreferences.getBoolean("Entrance_Lights", false)

        val currentConsumption = electricityPrefs.getFloat(TOTAL_CONSUMPTION_KEY, 0f)
        updateElectricityUI(currentConsumption)

        switchLight.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Entrance_Lights", isChecked).apply()
            lightOn = isChecked

            if (isChecked) {
                startElectricityTracking()

                apiService.turnOnLed().enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        Toast.makeText(applicationContext, "LED ON", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Toast.makeText(applicationContext, "Failed to connect", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                stopElectricityTracking()

                apiService.turnOffLed().enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        Toast.makeText(applicationContext, "LED OFF", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Toast.makeText(applicationContext, "Failed to connect", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

    }

    private fun startElectricityTracking() {
        handler = android.os.Handler(Looper.getMainLooper())
        val updateTask = object : Runnable {
            override fun run() {
                if (lightOn) {
                    val current = electricityPrefs.getFloat(TOTAL_CONSUMPTION_KEY, 0f)
                    val updated = current + LAMP_CONSUMPTION_KWH_PER_5MIN
                    electricityPrefs.edit().putFloat(TOTAL_CONSUMPTION_KEY, updated).apply()
                    updateElectricityUI(updated)

                    handler?.postDelayed(this, 300000) // every 5 minutes add consumption

                }
            }
        }
        handler?.post(updateTask)
    }

    private fun stopElectricityTracking() {
        handler?.removeCallbacksAndMessages(null)
    }
    private fun updateElectricityUI(kwh: Float) {
        val tvElectricity = findViewById<TextView?>(R.id.tvElectricityValue)
        val tvBill = findViewById<TextView?>(R.id.tvApproxBillValue)

        if (tvElectricity != null && tvBill != null) {
            val bill = kwh * BILL_RATE
            tvElectricity.text = String.format("%.2f kWh", kwh)
            tvBill.text = String.format("%.2f $", bill)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the BroadcastReceiver
        unregisterReceiver(roomStateReceiver)
    }
}