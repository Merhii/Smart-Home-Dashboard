// ✅ KitchenActivity (Updated with electricity tracking)
package com.example.myapplication

import android.content.*
import android.os.*
import android.widget.Switch
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi

private lateinit var kitchenSwitch: Switch
private lateinit var kitchenPrefs: SharedPreferences
private var kitchenHandler: Handler? = null
private const val KITCHEN_LIGHT_KWH_PER_5MIN = 0.00083f
private const val KWH_KEY = "TotalConsumption"

class KitchenActivity : ComponentActivity() {
    private val roomStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            kitchenSwitch.isChecked = kitchenPrefs.getBoolean("Kitchen_Lights", false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kitchen)
        kitchenPrefs = getSharedPreferences("RoomState", Context.MODE_PRIVATE)
        val electricityPrefs = getSharedPreferences("ElectricityData", Context.MODE_PRIVATE)

        registerReceiver(roomStateReceiver, IntentFilter("com.example.myapplication.ROOM_STATE_UPDATED"), RECEIVER_NOT_EXPORTED)

        val loc = intent.getStringExtra("loc") ?: "Kitchen"
        kitchenSwitch = findViewById(R.id.switchlights)
        kitchenSwitch.isChecked = kitchenPrefs.getBoolean("Kitchen_Lights", false)

        kitchenSwitch.setOnCheckedChangeListener { _, isChecked ->
            kitchenPrefs.edit().putBoolean("Kitchen_Lights", isChecked).apply()
            handleKitchenTracking(electricityPrefs)
        }

        handleKitchenTracking(electricityPrefs)
    }

    private fun handleKitchenTracking(electricityPrefs: SharedPreferences) {
        val isLightOn = kitchenPrefs.getBoolean("Kitchen_Lights", false)
        if (isLightOn) {
            if (kitchenHandler == null) kitchenHandler = Handler(Looper.getMainLooper())
            kitchenHandler?.removeCallbacksAndMessages(null)
            val task = object : Runnable {
                override fun run() {
                    if (!kitchenPrefs.getBoolean("Kitchen_Lights", false)) {
                        kitchenHandler?.removeCallbacksAndMessages(null)
                        kitchenHandler = null
                        return
                    }
                    var kwh = electricityPrefs.getFloat(KWH_KEY, 0f)
                    kwh += KITCHEN_LIGHT_KWH_PER_5MIN
                    electricityPrefs.edit().putFloat(KWH_KEY, kwh).apply()
                    kitchenHandler?.postDelayed(this, 300000)
                }
            }
            kitchenHandler?.post(task)
        } else {
            kitchenHandler?.removeCallbacksAndMessages(null)
            kitchenHandler = null
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(roomStateReceiver)
    }
}
