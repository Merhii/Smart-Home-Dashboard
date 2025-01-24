package com.example.myapplication

import android.view.LayoutInflater
import android.app.AlertDialog
import android.widget.ImageView
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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



class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home_status)
        var profile = findViewById<ImageView>(R.id.ivProfile)
        var notification = findViewById<ImageView>(R.id.ivNotification)
        var living = findViewById<Button>(R.id.living)
        var name = findViewById<TextView>(R.id.tvHeader)
        var bed= findViewById<Button>(R.id.bed)
        var bath= findViewById<Button>(R.id.bath)
        var kitchen= findViewById<Button>(R.id.kitchen)
var username = intent.getStringExtra("username")
        name.text = "$username's Home"
        notification.setOnClickListener {
            showNotificationDialog()
        }
        profile.setOnClickListener{
            val intent = Intent(this@HomeActivity, ProfileActivity::class.java)
            startActivity(intent)
        }
        living.setOnClickListener{
            val intent = Intent(this@HomeActivity, LivingroomActivity::class.java)
            intent.putExtra("loc", "Living Room")
            startActivity(intent)
        }
        bed.setOnClickListener{
            val intent = Intent(this@HomeActivity, BedroomActivity::class.java)
            intent.putExtra("loc", "Bed Room")
            startActivity(intent)
        }
        bath.setOnClickListener{
            val intent = Intent(this@HomeActivity, BathroomActivity::class.java)
            intent.putExtra("loc", "Bath Room")
            startActivity(intent)
        }
        kitchen.setOnClickListener{
            val intent = Intent(this@HomeActivity, KitchenActivity::class.java)
            intent.putExtra("loc", "Kitchen")
            startActivity(intent)
        }
    }
    private fun showNotificationDialog() {
        // Inflate the custom dialog layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_notifications, null)
        // Create the AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        // Close button action
        val btnCloseDialog: Button = dialogView.findViewById(R.id.btnCloseDialog)
        btnCloseDialog.setOnClickListener {
            dialog.dismiss()
        }
    }
}

