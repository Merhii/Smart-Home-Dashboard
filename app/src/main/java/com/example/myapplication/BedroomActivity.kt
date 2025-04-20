package com.example.myapplication

import android.content.*
import android.os.*
import android.widget.Switch
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi

private lateinit var Lswitch: Switch
private lateinit var Cswitch: Switch
private lateinit var sharedPreferences: SharedPreferences
private lateinit var electricityPrefs: SharedPreferences
private var bedroomHandler: Handler? = null
private const val LIGHT_CONSUMPTION_KWH_PER_5MIN = 0.00083f
private const val TOTAL_CONSUMPTION_KEY = "TotalConsumption"

class BedroomActivity : ComponentActivity() {
    private val roomStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Lswitch.isChecked = sharedPreferences.getBoolean("Bedroom_Lights", false)
            Cswitch.isChecked = sharedPreferences.getBoolean("Bedroom_Curtains", false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bedroom)

        sharedPreferences = getSharedPreferences("RoomState", Context.MODE_PRIVATE)
        electricityPrefs = getSharedPreferences("ElectricityData", Context.MODE_PRIVATE)

        registerReceiver(roomStateReceiver, IntentFilter("com.example.myapplication.ROOM_STATE_UPDATED"), RECEIVER_NOT_EXPORTED)

        val loc = intent.getStringExtra("loc") ?: "Bedroom"
        Lswitch = findViewById(R.id.switchlights)
        Cswitch = findViewById(R.id.switchcurtains)

        Lswitch.isChecked = sharedPreferences.getBoolean("Bedroom_Lights", false)
        Cswitch.isChecked = sharedPreferences.getBoolean("Bedroom_Curtains", false)

        Lswitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Bedroom_Lights", isChecked).apply()
            handleBedroomTracking()
        }

        Cswitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Bedroom_Curtains", isChecked).apply()
        }

        handleBedroomTracking()
    }

    private fun handleBedroomTracking() {
        val isLightOn = sharedPreferences.getBoolean("Bedroom_Lights", false)
        if (isLightOn) {
            if (bedroomHandler == null) bedroomHandler = Handler(Looper.getMainLooper())
            bedroomHandler?.removeCallbacksAndMessages(null)

            val task = object : Runnable {
                override fun run() {
                    if (!sharedPreferences.getBoolean("Bedroom_Lights", false)) {
                        bedroomHandler?.removeCallbacksAndMessages(null)
                        bedroomHandler = null
                        return
                    }
                    var current = electricityPrefs.getFloat(TOTAL_CONSUMPTION_KEY, 0f)
                    current += LIGHT_CONSUMPTION_KWH_PER_5MIN
                    electricityPrefs.edit().putFloat(TOTAL_CONSUMPTION_KEY, current).apply()
                    bedroomHandler?.postDelayed(this, 300000)
                }
            }

            bedroomHandler?.post(task)
        } else {
            bedroomHandler?.removeCallbacksAndMessages(null)
            bedroomHandler = null
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(roomStateReceiver)
    }
}
