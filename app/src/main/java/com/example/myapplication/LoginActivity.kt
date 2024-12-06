package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.DTO.LoginUserDto
import com.example.myapplication.Entity.User
import com.example.myapplication.RetrofitInstance.apiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private lateinit var loginbtn: Button
private lateinit var email: EditText
private lateinit var password: EditText

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.log_in_layout)
        var signup1=findViewById<Button>(R.id.btnSignup)
        var signup2=findViewById<Button>(R.id.Signup)
        loginbtn = findViewById(R.id.button)
        email = findViewById(R.id.emailtxt)
        password = findViewById(R.id.Passwordtxt)
        signup1.setOnClickListener{
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
        }
        signup2.setOnClickListener{
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
        }
        loginbtn.setOnClickListener {
            val email = email.text.toString()
            val password = password.text.toString()
            login(email,password)
        }
        }





    private fun login(email: String, password: String) {
        val loginUser = LoginUserDto(email, password)

        apiService.loginUser(loginUser).enqueue(object : Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    Toast.makeText(this@LoginActivity, "User loged IN: $user", Toast.LENGTH_SHORT).show()
                    println("YAAAAY")
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Failed to register: ${response.errorBody()?.string()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    }
