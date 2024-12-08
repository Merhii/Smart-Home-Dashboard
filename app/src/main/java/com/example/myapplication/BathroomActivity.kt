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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET


private lateinit var Eswitch: Switch
private lateinit var Lswitch: Switch
private lateinit var Sswitch: Switch
private lateinit var Pswitch: Switch


class BathroomActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.bathroom)
        val deviceLocation = intent.getStringExtra("loc")

       Eswitch = findViewById(R.id.switchexhaust)
        Lswitch = findViewById(R.id.switchlights)
        Sswitch = findViewById(R.id.switchspeaker)
        Pswitch = findViewById(R.id.switchpump)

        Eswitch.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) 1 else 0
            // Call your API to update the status
            println(status)

            if (deviceLocation != null) {
                updateDeviceStatus("Exhaust Fan", deviceLocation, status)
            }
        }


        Pswitch.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) 1 else 0
            // Call your API to update the status
            println(status)

            if (deviceLocation != null) {
                updateDeviceStatus("Water pump", deviceLocation, status)
            }
        }

        Sswitch.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) 1 else 0
            // Call your API to update the status
            println(status)

            if (deviceLocation != null) {
                updateDeviceStatus("Speaker", deviceLocation, status)
            }
        }

        Lswitch.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) 1 else 0
            // Call your API to update the status
            println(status)

            if (deviceLocation != null) {
                updateDeviceStatus("Lights", deviceLocation, status)
            }
        }
    }
    private fun updateDeviceStatus(deviceName: String, deviceLocation: String, status: Int) {
        // Launch a coroutine to make the network call asynchronously
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = apiService.updateDeviceStatus(deviceLocation, deviceName, status)

                if (response.isExecuted) {
                    Toast.makeText(this@BathroomActivity, "$deviceName status updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@BathroomActivity, "Failed to update device status", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
//                Toast.makeText(this@LivingroomActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Toast.makeText(this@BathroomActivity, "$deviceName status updated successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

