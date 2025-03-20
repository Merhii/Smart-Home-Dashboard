package com.example.myapplication
import android.content.Context
import android.content.Intent
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
        var logout = findViewById<TextView>(R.id.logout)

        name.text =  "$username"
        logout.setOnClickListener{
            logout()
        }
    }
    private fun logout() {
        // Clear the stored authentication data
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Redirect to the login activity
        val intent = Intent(this, MainActivity::class.java)
        // Clearing the activity stack and starting new task
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}