package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.log_in_layout)
        var signup1=findViewById<Button>(R.id.btnSignup)
        var signup2=findViewById<Button>(R.id.Signup)
        signup1.setOnClickListener{
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
        }
        signup2.setOnClickListener{
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
        }
        }

    }
