package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.log_in_layout)

        var btn = findViewById<Button>(R.id.button)

        btn.setOnClickListener {
            print("Hello")
            println("Hello000")
       fetchUserName()
        }
    }

    private fun fetchUserName() {
        lifecycleScope.launch {

             val userAdapter = UserAdapter()

            val users = userAdapter.getUsers()
            if (users != null && users.isNotEmpty()) {
                val userName = users[0].name // Assuming the 'name' property exists in your User data class
                // Show a Toast with the user's name
                Toast.makeText(this@MainActivity, "User Name: $userName", Toast.LENGTH_SHORT).show()
            } else {
                // Handle failure, e.g., show a message if no users found
                Toast.makeText(this@MainActivity, "Failed to fetch users", Toast.LENGTH_SHORT).show()
            }
        }}
}

