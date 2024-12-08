package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.DTO.RegisterUserDto
import com.example.myapplication.Entity.User
import com.example.myapplication.RetrofitInstance.apiService
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET



class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home_status)
        var living = findViewById<Button>(R.id.living)
        var bed= findViewById<Button>(R.id.bed)
        var bath= findViewById<Button>(R.id.bath)
        var kitchen= findViewById<Button>(R.id.kitchen)
        living.setOnClickListener{
            val intent = Intent(this@HomeActivity, LivingroomActivity::class.java)
            startActivity(intent)
        }
        bed.setOnClickListener{
            val intent = Intent(this@HomeActivity, BedroomActivity::class.java)
            startActivity(intent)
        }
        bath.setOnClickListener{
            val intent = Intent(this@HomeActivity, BathroomActivity::class.java)
            startActivity(intent)
        }
        kitchen.setOnClickListener{
            val intent = Intent(this@HomeActivity, KitchenActivity::class.java)
            startActivity(intent)
        }
    }
}

