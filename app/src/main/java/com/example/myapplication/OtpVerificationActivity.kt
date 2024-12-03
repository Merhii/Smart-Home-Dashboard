package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.example.myapplication.DTO.RegisterUserDto
import com.example.myapplication.DTO.VerifyUserDto
import com.example.myapplication.Entity.User
import com.example.myapplication.R
import com.example.myapplication.RetrofitInstance.apiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OtpVerificationActivity : ComponentActivity() {
    private lateinit var tvCountdown: TextView
    private lateinit var btnResend: Button
    private var countDownTimer: CountDownTimer? = null
    private lateinit var btn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.otp)

        tvCountdown = findViewById(R.id.tvCountdown)
        btnResend = findViewById(R.id.btnResend)
        btn = findViewById(R.id.btnEnter)
        startCountdown()

        btnResend.setOnClickListener {
            btnResend.visibility = View.GONE
            startCountdown()
        }
        setUpOTPFieldListeners()

        btn.setOnClickListener {
            var otp1: EditText=findViewById<EditText>(R.id.otp1)
            var otp2: EditText=findViewById<EditText>(R.id.otp2)
            var otp3: EditText=findViewById<EditText>(R.id.otp3)
            var otp4: EditText=findViewById<EditText>(R.id.otp4)
            var otp5: EditText=findViewById<EditText>(R.id.otp5)
            var otp6: EditText=findViewById<EditText>(R.id.otp6)
            val otpString = otp1.text.toString() + otp2.text.toString() +
                    otp3.text.toString() + otp4.text.toString() +
                    otp5.text.toString() + otp6.text.toString()
            println("String is" + otpString)
            println("Hello World")
            val email = intent.getStringExtra("email")
            if (email != null) {
                verifiy(email,otpString)
            }
            //TODO: Add intent to start home page + verification call api
        }

    }

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvCountdown.text = "Resend 0:${if (seconds < 10) "0" else ""}$seconds"
            }

            override fun onFinish() {
                tvCountdown.text = ""
                btnResend.visibility = View.VISIBLE
            }
        }.start()
    }
    private fun setUpOTPFieldListeners() {
        val otpFields = listOf(
            findViewById<EditText>(R.id.otp1),
            findViewById<EditText>(R.id.otp2),
            findViewById<EditText>(R.id.otp3),
            findViewById<EditText>(R.id.otp4),
            findViewById<EditText>(R.id.otp5),
            findViewById<EditText>(R.id.otp6)
        )

        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s != null && s.length == 1) {
                        if (i < otpFields.size - 1) {
                            otpFields[i + 1].requestFocus()
                        }
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun verifiy(email: String, verificationCode: String) {
        val verifyUserDto = VerifyUserDto(email, verificationCode)

        apiService.verifiyUser(verifyUserDto).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    Toast.makeText(this@OtpVerificationActivity, "User verified: $user", Toast.LENGTH_SHORT).show()
//                    val intent = Intent(this@MainActivity, OtpVerificationActivity::class.java)
//                    intent.putExtra("email", user?.email) // Add more data if needed
//                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@OtpVerificationActivity,
                        "Failed to register: ${response.errorBody()?.string()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@OtpVerificationActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
