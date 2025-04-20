package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge


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
