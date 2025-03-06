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

private lateinit var switchLight: Switch



class EntranceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.entrance)
        switchLight = findViewById(R.id.switchLight)

        switchLight.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Switch turned ON -> Call LED ON API
                apiService.turnOnLed().enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        Toast.makeText(applicationContext, "LED ON", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Toast.makeText(applicationContext, "Failed to connect", Toast.LENGTH_SHORT)
                            .show()
                    }
                })
            } else {
                // Switch turned OFF -> Call LED OFF API
                apiService.turnOffLed().enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        Toast.makeText(applicationContext, "LED OFF", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Toast.makeText(applicationContext, "Failed to connect", Toast.LENGTH_SHORT)
                            .show()
                    }
                })
            }

        }
    }
}