package com.example.myapplication

import android.view.LayoutInflater
import android.app.AlertDialog
import android.content.Context
import android.widget.ImageView
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.KeyEvent
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
import com.example.myapplication.api.PrayerTimesResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Switch
import android.content.SharedPreferences

class HomeActivity : ComponentActivity() {
    private var prayerTimings: PrayerTimesResponse? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home_status)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("RoomState", Context.MODE_PRIVATE)

        // Fetch Prayer Times when app starts
        fetchPrayerTimes()

        // Start checking Adhan time in a loop when app is open
        startAdhanCheckLoop()

        var profile = findViewById<ImageView>(R.id.ivProfile)
        var notification = findViewById<ImageView>(R.id.ivNotification)
        var living = findViewById<Button>(R.id.living)
        var name = findViewById<TextView>(R.id.tvHeader)
        var bed = findViewById<Button>(R.id.bed)
        var bath = findViewById<Button>(R.id.bath)
        var kitchen = findViewById<Button>(R.id.kitchen)
        var entrance = findViewById<Button>(R.id.entrance)
        var garage = findViewById<Button>(R.id.garage)
        var username = intent.getStringExtra("username")
        name.text = "$username's Home"

        notification.setOnClickListener {
            showNotificationDialog()
        }
        profile.setOnClickListener {
            val intent = Intent(this@HomeActivity, ProfileActivity::class.java)
            intent.putExtra("Name",username)
            startActivity(intent)
        }
        living.setOnClickListener {
            val intent = Intent(this@HomeActivity, LivingroomActivity::class.java)
            intent.putExtra("loc", "Living Room")
            startActivity(intent)
        }
        bed.setOnClickListener {
            val intent = Intent(this@HomeActivity, BedroomActivity::class.java)
            intent.putExtra("loc", "Bed Room")
            startActivity(intent)
        }
        bath.setOnClickListener {
            val intent = Intent(this@HomeActivity, BathroomActivity::class.java)
            intent.putExtra("loc", "Bath Room")
            startActivity(intent)
        }
        kitchen.setOnClickListener {
            val intent = Intent(this@HomeActivity, KitchenActivity::class.java)
            intent.putExtra("loc", "Kitchen")
            startActivity(intent)
        }
        entrance.setOnClickListener {
            val intent = Intent(this@HomeActivity, EntranceActivity::class.java)
            intent.putExtra("loc", "Entrance")
            startActivity(intent)
        }
        garage.setOnClickListener {
            val intent = Intent(this@HomeActivity, GarageActivity::class.java)
            intent.putExtra("loc", "Garage")
            startActivity(intent)
        }

        // Set up listeners for Quick Profiles
        val switchBed = findViewById<Switch>(R.id.switchControl1)
        val switchEnergy = findViewById<Switch>(R.id.switchControl2)
        val switchAway = findViewById<Switch>(R.id.switchControl3)

        switchBed.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                activateBedProfile()
            }
        }

        switchEnergy.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                activateEnergyProfile()
            }
        }

        switchAway.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                activateAwayProfile()
            }
        }
    }

    private fun activateBedProfile() {
        // Turn off all lights and close curtains
        RoomState.roomStates.forEach { (room, devices) ->
            devices.forEach { (device, _) ->
                when (device) {
                    "Lights" -> {
                        RoomState.roomStates[room]?.put(device, false)
                        sharedPreferences.edit().putBoolean("${room}_Lights", false).apply()
                    }
                    "Curtains" -> {
                        RoomState.roomStates[room]?.put(device, true)
                        sharedPreferences.edit().putBoolean("${room}_Curtains", true).apply()
                    }
                }
            }
        }
        notifyRooms()
    }



    private fun activateEnergyProfile() {
        RoomState.roomStates.forEach { (room, devices) ->
            devices.forEach { (device, _) ->
                //to be thought of
            }
        }
        notifyRooms()
    }

    private fun activateAwayProfile() {
        // Turn off all lights and close curtains
        RoomState.roomStates.forEach { (room, devices) ->
            devices.forEach { (device, _) ->
                RoomState.roomStates[room]?.put(device, false)
                sharedPreferences.edit().putBoolean("${room}_$device", false).apply()
            }
        }
        notifyRooms()
    }

    private fun notifyRooms() {
        // Broadcast an intent to notify room activities to refresh their UI
        val intent = Intent("com.example.myapplication.ROOM_STATE_UPDATED")
        sendBroadcast(intent)
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

    /** Fetch Adhan prayer times from API */
    private fun fetchPrayerTimes() {
        RetrofitInstance.prayerApi.getPrayerTimes().enqueue(object : Callback<PrayerTimesResponse> {
            override fun onResponse(call: Call<PrayerTimesResponse>, response: Response<PrayerTimesResponse>) {
                if (response.isSuccessful) {
                    prayerTimings = response.body()

                    // ✅ Print API response to debug
                    println("✅ API Response Received: ${prayerTimings}")

                    if (prayerTimings == null || prayerTimings?.data?.timings == null) {
                        println("❌ API returned NULL prayer times!")
                    } else {
                        println("✅ Prayer times: ${prayerTimings?.data?.timings}")
                    }
                } else {
                    println("❌ API Request Failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<PrayerTimesResponse>, t: Throwable) {
                println("❌ API Error: ${t.message}")
            }
        })
    }

    /** ✅ Loop that checks Adhan time every 60 seconds (only when app is open) */
    private fun startAdhanCheckLoop() {
        lifecycleScope.launch {
            while (true) {
                checkAdhanTime()
                delay(60000) // Check every 60 seconds
            }
        }
    }

    /** ✅ Check if it's time for Adhan */
    private fun checkAdhanTime() {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = formatter.format(Date())

        println("🕰️ Checking Adhan time... Current time: $currentTime")

        if (prayerTimings == null || prayerTimings?.data?.timings == null) {
            println("❌ No prayer times available yet")
            return
        }

        val timings = prayerTimings!!.data.timings
        val prayerTimesList = mapOf(
            "Fajr" to timings.Fajr,
            "Dhuhr" to timings.Dhuhr,
            "Asr" to timings.Asr,
            "Maghrib" to timings.Maghrib,
            "Isha" to timings.Isha
        )

        for ((prayerName, prayerTime) in prayerTimesList) {
            val normalizedPrayerTime = normalizeTime(prayerTime)

            println("🔎 Checking $prayerName time: $normalizedPrayerTime vs Current Time: $currentTime")

            if (normalizedPrayerTime == currentTime) {
                println("✅ Adhan time matched! Playing Adhan for $prayerName")
                stopMusicOnBluetooth()
                playAdhanAudio()
                return // Exit loop after finding a match
            }
        }
    }

    /** ✅ Normalize Adhan time format */
    private fun normalizeTime(time: String): String {
        return time.substring(0, 5) // Ensure only HH:mm is compared
    }

    /** ✅ Stop any playing music on Bluetooth speaker */
    private fun stopMusicOnBluetooth() {
        try {
            // ✅ Send Pause Command (For Most Music Apps)
            val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
            intent.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE))
            sendBroadcast(intent)
            println("✅ Sent Bluetooth pause command")

            // ✅ Request Audio Focus (Force Stop Music)
            val audioManager = getSystemService(AUDIO_SERVICE) as android.media.AudioManager
            val result = audioManager.requestAudioFocus(
                null,
                android.media.AudioManager.STREAM_MUSIC,
                android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )

            if (result == android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                println("✅ Audio focus granted, YouTube Music should stop!")
            } else {
                println("❌ Audio focus request failed!")
            }

            // ✅ Send Pause Command for YouTube Music (Alternative Method)
            val ytIntent = Intent("com.android.music.musicservicecommand")
            ytIntent.putExtra("command", "pause")
            sendBroadcast(ytIntent)
            println("✅ Sent additional pause command for YouTube Music")

        } catch (e: Exception) {
            println("❌ Error stopping Bluetooth music: ${e.message}")
        }
    }

    /** ✅ Play Adhan on phone or Bluetooth speaker */
    private fun playAdhanAudio() {
        mediaPlayer?.release() // Release previous media player instance if playing
        mediaPlayer = MediaPlayer.create(this, R.raw.adhan)
        mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener {
            it.release() // Release media player when done
        }
    }
}