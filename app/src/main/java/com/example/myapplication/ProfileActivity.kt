package com.example.myapplication
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

private lateinit var curtains: Switch
private val client = OkHttpClient()
class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.profile)
        val username = intent.getStringExtra("Name") ?: "Default Name"  // You can replace "Default Name" with any default value you prefer
        var name = findViewById<TextView>(R.id.name)
        var logout = findViewById<TextView>(R.id.logout)

        curtains=findViewById(R.id.switchcurtains)
        name.text =  "$username"
        logout.setOnClickListener{
            logout()
        }

        curtains.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked) {
                sendRequest(IP2.LDRT)
            } else {
                sendRequest(IP2.LDRF)
            }
        }
    }

    private fun sendRequest(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Failed to send request", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    runOnUiThread {
                        Toast.makeText(this@ProfileActivity, "Request Sent Successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
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