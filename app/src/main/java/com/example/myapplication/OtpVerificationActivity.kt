package com.example.myapplication

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.example.myapplication.R

class OtpVerificationActivity : ComponentActivity() {
    private lateinit var tvCountdown: TextView
    private lateinit var btnResend: Button
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.otp)

        tvCountdown = findViewById(R.id.tvCountdown)
        btnResend = findViewById(R.id.btnResend)

        startCountdown()

        btnResend.setOnClickListener {
            btnResend.visibility = View.GONE
            startCountdown()
        }
        setUpOTPFieldListeners()
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
}
