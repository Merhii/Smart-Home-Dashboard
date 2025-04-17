// ✅ KitchenActivity (Updated with electricity tracking)
package com.example.myapplication

import android.content.*
import android.os.*
import android.widget.Switch
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import com.example.myapplication.RetrofitInstance.apiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
            updateDeviceStatus("Lights", loc, if (isChecked) 1 else 0)
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

    private fun updateDeviceStatus(name: String, location: String, status: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = apiService.updateDeviceStatus(location, name, status)
                val msg = if (response.isExecuted) "$name updated successfully" else "Failed to update $name"
                Toast.makeText(this@KitchenActivity, msg, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@KitchenActivity, "$name updated (offline)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(roomStateReceiver)
    }
}
