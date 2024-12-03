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

private lateinit var userName: EditText
private lateinit var password: EditText
private lateinit var email: EditText

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.sign_up_layout)

        var btn = findViewById<Button>(R.id.button)

        userName = findViewById(R.id.nametext)
        email = findViewById(R.id.emailtxt)
        password = findViewById(R.id.Passwordtxt)
        btn.setOnClickListener {
            val email = email.text.toString()
            val password = password.text.toString()
            val username = userName.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty()) {
                registerUser(email, password, username)
                val intent = Intent(this@MainActivity, OtpVerificationActivity::class.java)
                intent.putExtra("email", email) // Add more data if needed
                startActivity(intent)
            } else {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun registerUser(email: String, password: String, username: String) {
        val registerUserDto = RegisterUserDto(email, password, username)

        apiService.registerUser(registerUserDto).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    Toast.makeText(this@MainActivity, "User registered: $user", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to register: ${response.errorBody()?.string()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

