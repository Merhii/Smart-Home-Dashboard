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



private lateinit var Lswitch: Switch

class KitchenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.kitchen)
        val deviceLocation = intent.getStringExtra("loc")
        Lswitch = findViewById(R.id.switchlights)

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
                Toast.makeText(this@KitchenActivity, "$deviceName status updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@KitchenActivity, "Failed to update device status", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
//                Toast.makeText(this@LivingroomActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            Toast.makeText(this@KitchenActivity, "$deviceName status updated successfully", Toast.LENGTH_SHORT).show()
        }
    }
}
}