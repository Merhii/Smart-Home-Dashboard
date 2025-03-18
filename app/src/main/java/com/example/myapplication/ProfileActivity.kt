package com.example.myapplication
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.profile)
        val username = intent.getStringExtra("Name") ?: "Default Name"  // You can replace "Default Name" with any default value you prefer
        var name = findViewById<TextView>(R.id.name)
        name.text =  "$username"
    }
}