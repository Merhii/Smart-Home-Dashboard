package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.DTO.LoginUserDto
import com.example.myapplication.Entity.User
import com.example.myapplication.RetrofitInstance.apiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private lateinit var loginbtn: Button
private lateinit var email: EditText
private lateinit var password: EditText
private var isPasswordVisible = false

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.log_in_layout)

        val signup1 = findViewById<Button>(R.id.btnSignup)
        val signup2 = findViewById<Button>(R.id.Signup)
        loginbtn = findViewById(R.id.button)
        email = findViewById(R.id.emailtxt)
        password = findViewById(R.id.Passwordtxt)

        signup1.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
        }

        signup2.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
        }

        loginbtn.setOnClickListener {
            val email = email.text.toString()
            val password = password.text.toString()
            login(email, password)
        }

        // Handle password visibility toggle when the eye icon is clicked
        password.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = password.compoundDrawablesRelative[2] // Get drawableEnd (right icon)
                if (drawableEnd != null) {
                    val iconStartX = password.width - password.paddingEnd - drawableEnd.bounds.width()
                    if (event.rawX >= iconStartX) {
                        togglePasswordVisibility()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    private fun login(email: String, password: String) {
        val loginUser = LoginUserDto(email, password)

        apiService.loginUser(loginUser).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    Toast.makeText(this@LoginActivity, "User logged IN: $user", Toast.LENGTH_SHORT).show()
                    CoroutineScope(Dispatchers.IO).launch {
                        val username = getUsername(email)

                        withContext(Dispatchers.Main) {
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            intent.putExtra("id", user?.userid)
                            intent.putExtra("username", username)
                            startActivity(intent)
                        }
                    }
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

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            password.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0)
        } else {
            password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            password.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_eye_closed, 0)
        }
        password.setSelection(password.text.length) // Keep cursor at the end
    }

    suspend fun getUsername(email: String): String? {
        return apiService.getUsername(email)
    }
}
