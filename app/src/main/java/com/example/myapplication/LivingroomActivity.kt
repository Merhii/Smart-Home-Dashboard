package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper

private lateinit var ACswitch: Switch
private lateinit var Curswitch: Switch
private lateinit var MLswitch: Switch
private lateinit var btnIncreaseTemp: Button
private lateinit var btnDecreaseTemp: Button
private lateinit var seekBarACTemp: SeekBar
private lateinit var acModeIcon: ImageView
private lateinit var tvSetACTemperature: TextView
private lateinit var tvRoomTemperature: TextView
class LivingroomActivity : ComponentActivity() {
    private val roomStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Refresh UI based on updated RoomState
            ACswitch.isChecked = sharedPreferences.getBoolean("Livingroom_AC", false)
            Curswitch.isChecked = sharedPreferences.getBoolean("Livingroom_Curtains", false)
            MLswitch.isChecked = sharedPreferences.getBoolean("Livingroom_MainLights", false)
        }
    }

    private lateinit var sharedPreferences: SharedPreferences
    private val client = OkHttpClient()
    private var isAcOn: Boolean = false
    private var setTemperature: Int = 22
    private var roomTemperature: Int = 22
    private lateinit var electricityPrefs: SharedPreferences
    private var handler: Handler? = null
    private var lightsOn = false
    private var edgeLightsOn = false
    private var acOn = false

    private val MAIN_LIGHTS_KWH_PER_5MIN = 0.00083f
    private val AC_KWH_PER_5MIN = 0.166f // assuming 2kW AC

    private val TOTAL_CONSUMPTION_KEY = "TotalConsumption"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.livingroom)
        electricityPrefs = getSharedPreferences("ElectricityData", Context.MODE_PRIVATE)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("RoomState", Context.MODE_PRIVATE)

        // Register the BroadcastReceiver
        registerReceiver(roomStateReceiver, IntentFilter("com.example.myapplication.ROOM_STATE_UPDATED"),
            RECEIVER_NOT_EXPORTED
        )

        val deviceLocation = intent.getStringExtra("loc")

        ACswitch = findViewById(R.id.switchAC)
        Curswitch = findViewById(R.id.switchcurtains)
        MLswitch = findViewById(R.id.switchlights1)


        btnIncreaseTemp = findViewById(R.id.btnIncreaseTemp)
        btnDecreaseTemp = findViewById(R.id.btnDecreaseTemp)
        seekBarACTemp = findViewById(R.id.seekBarACTemp)
        acModeIcon = findViewById(R.id.acModeIcon)
        tvSetACTemperature = findViewById(R.id.tvSetACTemperature)
        tvRoomTemperature = findViewById(R.id.tvRoomTemperature)

        // Restore switch states from SharedPreferences
        ACswitch.isChecked = sharedPreferences.getBoolean("Livingroom_AC", false)
        Curswitch.isChecked = sharedPreferences.getBoolean("Livingroom_Curtains", false)
        MLswitch.isChecked = sharedPreferences.getBoolean("Livingroom_Lights", false)



        // ✅ AC Switch controls ON/OFF logic
        ACswitch.setOnCheckedChangeListener { _, isChecked ->
            isAcOn = isChecked
            acOn = isChecked
            sharedPreferences.edit().putBoolean("Livingroom_AC", isChecked).apply()
            updateAcUI()
            handleLivingroomTracking()
        }

        // ✅ MLswitch controls a request (previously missing)
        MLswitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Livingroom_Lights", isChecked).apply()
            if (isChecked) {
                sendRequest(IP.ledon)
            } else {
                sendRequest(IP.ledoff)
            }
            lightsOn = isChecked
            handleLivingroomTracking()
        }



        Curswitch.setOnCheckedChangeListener{ _, isChecked ->
            sharedPreferences.edit().putBoolean("Livingroom_Curtains", isChecked).apply()
            if (isChecked) {
                sendRequest(IP2.openCurtains)
                println(IP.curtson)
            } else {
                sendRequest(IP2.closeCurtains)
            }
        }
        lightsOn = MLswitch.isChecked
        acOn = ACswitch.isChecked
        handleLivingroomTracking()

        // ✅ Temperature adjustment buttons
        btnIncreaseTemp.setOnClickListener { adjustTemperature(1) }
        btnDecreaseTemp.setOnClickListener { adjustTemperature(-1) }

        // ✅ SeekBar for setting temperature
        seekBarACTemp.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (isAcOn) {
                    setTemperature = progress
                    updateAcMode()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // ✅ Simulate room temperature updates
        simulateTemperatureUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the BroadcastReceiver
        unregisterReceiver(roomStateReceiver)
    }

    private fun updateAcUI() {
        btnIncreaseTemp.isEnabled = isAcOn
        btnDecreaseTemp.isEnabled = isAcOn
        seekBarACTemp.isEnabled = isAcOn

        if (!isAcOn) {
            acModeIcon.setImageResource(R.drawable.ic_ac_off)
            tvSetACTemperature.text = "Set Temp: --"
        } else {
            updateAcMode()
        }
    }

    private fun updateAcMode() {
        tvSetACTemperature.text = "Set Temp: $setTemperature°C"
        when {
            setTemperature > roomTemperature -> {
                acModeIcon.setImageResource(R.drawable.ic_ac_heat)
            }
            setTemperature < roomTemperature -> {
                acModeIcon.setImageResource(R.drawable.ic_ac_cool)
            }
            else -> {
                acModeIcon.setImageResource(R.drawable.ic_ac_off)
            }
        }
    }

    private fun adjustTemperature(change: Int) {
        if (isAcOn) {
            setTemperature = (setTemperature + change).coerceIn(16, 30)
            seekBarACTemp.progress = setTemperature
            updateAcMode()
        }
    }

    private fun simulateTemperatureUpdates() {
        lifecycleScope.launch {
            while (true) {
                tvRoomTemperature.text = "Room Temp: $roomTemperature°C"
                delay(5000)
                roomTemperature = (18..28).random()
                updateAcMode()
            }
        }
    }



    private fun sendRequest(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LivingroomActivity, "Failed to send request", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    runOnUiThread {
                        Toast.makeText(this@LivingroomActivity, "Request Sent Successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun handleLivingroomTracking() {
        if (lightsOn || acOn) {
            if (handler == null) handler = Handler(Looper.getMainLooper())
            handler?.removeCallbacksAndMessages(null)

            val updateTask = object : Runnable {
                override fun run() {
                    // Refresh the switch states safely
                    lightsOn = MLswitch.isChecked
                    acOn = ACswitch.isChecked

                    // Stop tracking if all are off
                    if (!lightsOn && !acOn) {
                        handler?.removeCallbacksAndMessages(null)
                        handler = null
                        return
                    }

                    var current = electricityPrefs.getFloat(TOTAL_CONSUMPTION_KEY, 0f)
                    if (lightsOn) current += MAIN_LIGHTS_KWH_PER_5MIN
                    if (acOn) current += AC_KWH_PER_5MIN
                    electricityPrefs.edit().putFloat(TOTAL_CONSUMPTION_KEY, current).apply()

                    handler?.postDelayed(this, 300000)
                }
            }
            handler?.post(updateTask)
        } else {
            handler?.removeCallbacksAndMessages(null)
            handler = null
        }
    }



}