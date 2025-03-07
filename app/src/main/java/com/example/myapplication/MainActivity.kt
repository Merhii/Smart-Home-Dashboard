package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.example.myapplication.RetrofitInstance.apiService
import com.example.myapplication.Entity.User
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import android.media.MediaPlayer
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.homepage)



        // Button for navigation
        val btn = findViewById<Button>(R.id.homeButton)
        btn.setOnClickListener {
            val intent = Intent(this@MainActivity, SignupActivity::class.java)
            startActivity(intent)
        }

    }



}
