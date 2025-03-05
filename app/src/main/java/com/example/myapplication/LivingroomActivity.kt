package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.DTO.RegisterUserDto
import com.example.myapplication.Entity.User
import com.example.myapplication.RetrofitInstance.apiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.Call
import java.io.IOException


private lateinit var ACswitch: Switch
private lateinit var APswitch: Switch
private lateinit var Curswitch: Switch
private lateinit var MLswitch: Switch
private lateinit var ELswitch: Switch

class LivingroomActivity : ComponentActivity() {
    private val client = OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.livingroom)
        val deviceLocation = intent.getStringExtra("loc")

        ACswitch =  findViewById(R.id.switchAC)
        APswitch = findViewById(R.id.switchpurifier)
        Curswitch = findViewById(R.id.switchcurtains)
        MLswitch = findViewById(R.id.switchlights1)
        ELswitch = findViewById(R.id.switchlights2)

        ACswitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sendRequest(" http://192.168.1.111/on")

            } else {
                sendRequest(" http://192.168.1.111/off")
            }
        }
        APswitch.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) 1 else 0
            // Call your API to update the status
            println(status)

            if (deviceLocation != null) {
                updateDeviceStatus("AC", deviceLocation, status)
            }
        }
        Curswitch.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) 1 else 0
            // Call your API to update the status
            println(status)

            if (deviceLocation != null) {
                updateDeviceStatus("Curtains", deviceLocation, status)
            }
        }
        MLswitch.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) 1 else 0
            // Call your API to update the status
            println(status)

            if (deviceLocation != null) {
                updateDeviceStatus("Middle Lights", deviceLocation, status)
            }
        }
        ELswitch.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) 1 else 0
            // Call your API to update the status
            println(status)

            if (deviceLocation != null) {
                updateDeviceStatus("Edge Lights", deviceLocation, status)
            }
        }

    }

    private fun updateDeviceStatus(deviceName: String, deviceLocation: String, status: Int) {
        // Launch a coroutine to make the network call asynchronously
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = apiService.updateDeviceStatus(deviceLocation, deviceName, status)

                if (response.isExecuted) {
                    Toast.makeText(this@LivingroomActivity, "$deviceName status updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LivingroomActivity, "Failed to update device status", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
//                Toast.makeText(this@LivingroomActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Toast.makeText(this@LivingroomActivity, "$deviceName status updated successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendRequest(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@LivingroomActivity, "Failed", Toast.LENGTH_SHORT).show()

                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    runOnUiThread {
                        Toast.makeText(this@LivingroomActivity, " WEEEE", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })}
}

