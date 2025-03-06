package com.example.myapplication

import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.myapplication.RetrofitInstance.apiService
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

private lateinit var ACswitch: Switch
private lateinit var Curswitch: Switch
private lateinit var MLswitch: Switch
private lateinit var ELswitch: Switch

private lateinit var btnIncreaseTemp: Button
private lateinit var btnDecreaseTemp: Button
private lateinit var seekBarACTemp: SeekBar
private lateinit var acModeIcon: ImageView
private lateinit var tvSetACTemperature: TextView
private lateinit var tvRoomTemperature: TextView

class LivingroomActivity : ComponentActivity() {
    private val client = OkHttpClient()
    private var isAcOn: Boolean = false
    private var setTemperature: Int = 22
    private var roomTemperature: Int = 22

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.livingroom)

        val deviceLocation = intent.getStringExtra("loc")

        ACswitch = findViewById(R.id.switchAC)
        Curswitch = findViewById(R.id.switchcurtains)
        MLswitch = findViewById(R.id.switchlights1)
        ELswitch = findViewById(R.id.switchlights2)

        btnIncreaseTemp = findViewById(R.id.btnIncreaseTemp)
        btnDecreaseTemp = findViewById(R.id.btnDecreaseTemp)
        seekBarACTemp = findViewById(R.id.seekBarACTemp)
        acModeIcon = findViewById(R.id.acModeIcon)
        tvSetACTemperature = findViewById(R.id.tvSetACTemperature)
        tvRoomTemperature = findViewById(R.id.tvRoomTemperature)

        // ✅ AC Switch controls ON/OFF logic
        ACswitch.setOnCheckedChangeListener { _, isChecked ->
            isAcOn = isChecked
            updateAcUI()
            updateDeviceStatus("AC", deviceLocation ?: "Living Room", if (isAcOn) 1 else 0)
        }

        // ✅ MLswitch controls a request (previously missing)
        MLswitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sendRequest("http://192.168.1.111/on")
            } else {
                sendRequest("http://192.168.1.111/off")
            }
        }

        Curswitch.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) 1 else 0
            if (deviceLocation != null) {
                updateDeviceStatus("Curtains", deviceLocation, status)
            }
        }

        ELswitch.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) 1 else 0
            if (deviceLocation != null) {
                updateDeviceStatus("Edge Lights", deviceLocation, status)
            }
        }

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

    private fun updateDeviceStatus(deviceName: String, deviceLocation: String, status: Int) {
        lifecycleScope.launch {
            try {
                val response = apiService.updateDeviceStatus(deviceLocation, deviceName, status)
                if (response.isExecuted) {
                    Toast.makeText(this@LivingroomActivity, "$deviceName updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LivingroomActivity, "Failed to update $deviceName", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LivingroomActivity, "Error updating $deviceName", Toast.LENGTH_SHORT).show()
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
}
