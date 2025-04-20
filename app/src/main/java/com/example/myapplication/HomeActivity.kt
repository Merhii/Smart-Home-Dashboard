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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import kotlinx.coroutines.withContext
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.api.PrayerTimesResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Switch
import android.content.SharedPreferences
import android.view.View
import android.widget.LinearLayout
import kotlinx.coroutines.Dispatchers
import okhttp3.Request
import java.io.IOException
import android.graphics.Color
import android.view.Gravity
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import android.os.Handler
import android.os.Looper
import okhttp3.OkHttpClient

class HomeActivity : ComponentActivity() {
    val BILL_RATE = 0.4f
    private var prayerTimings: PrayerTimesResponse? = null
    private val client = OkHttpClient()
    private val notificationApiUrls = listOf("http://192.168.1.139:5000/hello")
    private val notificationMessages = mutableListOf<String>()
    private lateinit var notificationListLayout: LinearLayout
    private lateinit var tvNoNotifications: TextView

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var updateHandler: Handler
    private val updateInterval = 10000L // 30 sec
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateElectricityUI()
            updateHandler.postDelayed(this, updateInterval)
        }
    }
    private fun updateElectricityUI() {
        val prefs = getSharedPreferences("ElectricityData", Context.MODE_PRIVATE)
        val kwh = prefs.getFloat("TotalConsumption", 0f)

        val tvElectricity = findViewById<TextView>(R.id.tvElectricityValue)
        val tvBill = findViewById<TextView>(R.id.tvApproxBillValue)

        val bill = kwh * BILL_RATE
        tvElectricity.text = String.format("%.2f kWh", kwh)
        tvBill.text = String.format("%.2f $", bill)
    }




    override fun onPause() {
        super.onPause()
        updateHandler.removeCallbacks(updateRunnable)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home_status)
        fetchAllNotifications()
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("RoomState", Context.MODE_PRIVATE)


// Also update the UI immediately if needed:
        val tvElectricity = findViewById<TextView>(R.id.tvElectricityValue)
        val tvBill = findViewById<TextView>(R.id.tvApproxBillValue)

        tvElectricity.text = "0.00 kWh"
        tvBill.text = "0.00 $"

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
        var electric = findViewById<Button>(R.id.Electric)
        var laundry = findViewById<Button>(R.id.Laundry)
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
        electric.setOnClickListener {
            val intent = Intent(this@HomeActivity, ElectricActivity::class.java)
            intent.putExtra("loc", "Electric")
            startActivity(intent)
        }
        laundry.setOnClickListener {
            val intent = Intent(this@HomeActivity, ElectricActivity::class.java)
            intent.putExtra("loc", "Laundry")
            startActivity(intent)
        }

        updateNotificationDot(true)
        // Set up listeners for Quick Profiles
        val switchBed = findViewById<Switch>(R.id.switchControl1)
        val switchEnergy = findViewById<Switch>(R.id.switchControl2)
        val switchMorning = findViewById<Switch>(R.id.switchControl3)

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

        switchMorning.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                activateMorningProfile()
            }
        }
        val btnClearBill = findViewById<Button>(R.id.btnClearBill)
        btnClearBill.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm Reset")
                .setMessage("Are you sure you want to clear the bill and reset KWH?")
                .setPositiveButton("Yes") { _, _ ->
                    val prefs = getSharedPreferences("ElectricityData", Context.MODE_PRIVATE)
                    prefs.edit().putFloat("TotalConsumption", 0f).apply()

                    val tvElectricity = findViewById<TextView>(R.id.tvElectricityValue)
                    val tvBill = findViewById<TextView>(R.id.tvApproxBillValue)

                    tvElectricity.text = "0.00 kWh"
                    tvBill.text = "0.00 $"

                    Toast.makeText(this, "Electricity bill reset.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

    }
    private fun fetchAllNotifications() {
        lifecycleScope.launch {
            notificationMessages.clear()

            for (url in notificationApiUrls) {
                try {
                    val result = notireq(url)
                    val json = org.json.JSONObject(result)
                    val message = json.getString("data")
                    notificationMessages.add(message)
                } catch (e: Exception) {
                    notificationMessages.add("Failed to fetch from $url")
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        updateHandler = Handler(Looper.getMainLooper())
        updateHandler.postDelayed(updateRunnable, updateInterval)
        updateElectricityUI() // Initial update
        val prefs = getSharedPreferences("ElectricityData", Context.MODE_PRIVATE)
        val kwh = prefs.getFloat("TotalConsumption", 0f)

        val tvElectricity = findViewById<TextView>(R.id.tvElectricityValue)
        val tvBill = findViewById<TextView>(R.id.tvApproxBillValue)

        val bill = kwh * BILL_RATE

        tvElectricity.text = String.format("%.2f kWh", kwh)
        tvBill.text = String.format("%.2f $", bill)

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
                when (device) {
                    "Lights", "Heater", "AC" -> {
                        RoomState.roomStates[room]?.put(device, false)
                        sharedPreferences.edit().putBoolean("${room}_$device", false).apply()
                    }
                }
            }
        }
        notifyRooms()
    }



    private fun activateMorningProfile() {
        // Turn off all lights and close curtains
        RoomState.roomStates.forEach { (room, devices) ->
            devices.forEach { (device, _) ->
                when (device) {
                    "Curtains" -> {
                        RoomState.roomStates[room]?.put(device, false)
                        sharedPreferences.edit().putBoolean("${room}_Curtains", false).apply()
                    }
                }
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
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_notifications, null)
        notificationListLayout = dialogView.findViewById(R.id.notificationListLayout)
        tvNoNotifications = dialogView.findViewById(R.id.tvNoNotifications)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        // Close button
        dialogView.findViewById<Button>(R.id.btnCloseDialog).setOnClickListener {
            dialog.dismiss()
        }

        // Display notifications
        if (notificationMessages.isEmpty()) {
            tvNoNotifications.visibility = View.VISIBLE
        } else {
            tvNoNotifications.visibility = View.GONE
            notificationMessages.forEach { msg -> addNotification(msg, dialogView) }
        }
    }

    private fun addNotification(notificationText: String, dialogView: View) {
        val notificationListLayout: LinearLayout = dialogView.findViewById(R.id.notificationListLayout)
        val tvNoNotifications: TextView = dialogView.findViewById(R.id.tvNoNotifications)

        // Hide the "No notifications" message with fade out if visible
        if (tvNoNotifications.visibility == View.VISIBLE) {
            tvNoNotifications.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    tvNoNotifications.visibility = View.GONE
                    tvNoNotifications.alpha = 1f
                }
                .start()
        }

        // Card container
        val cardLayout = FrameLayout(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            background = ContextCompat.getDrawable(this@HomeActivity, R.drawable.bg_notification_card)
            elevation = 8f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }

        // TextView inside card
        val textView = TextView(this).apply {
            text = notificationText
            textSize = 14f
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        // Red X Button
        val closeBtn = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(Color.RED)
            setPadding(16, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
            }

            setOnClickListener {
                // Animate removal
                cardLayout.animate()
                    .alpha(0f)
                    .translationX(100f)
                    .setDuration(300)
                    .withEndAction {
                        notificationListLayout.removeView(cardLayout)

                        // Show fallback message if empty
                        if (notificationListLayout.childCount == 0) {
                            tvNoNotifications.alpha = 0f
                            tvNoNotifications.visibility = View.VISIBLE
                            tvNoNotifications.animate().alpha(1f).setDuration(300).start()

                                updateNotificationDot(false)


                        }
                    }
                    .start()
            }
        }

        // Add views
        container.addView(textView)
        container.addView(closeBtn)
        cardLayout.addView(container)

        // Add to layout with fade in
        cardLayout.alpha = 0f
        notificationListLayout.addView(cardLayout)
        cardLayout.animate().alpha(1f).setDuration(300).start()
    }


    private fun updateNotificationDot(hasNew: Boolean) {
        val dotView: View = findViewById(R.id.ivNotificationDot)
        dotView.visibility = if (hasNew) View.VISIBLE else View.GONE

    }


    private suspend fun notireq(url: String): String = withContext(Dispatchers.IO) {
        val request = okhttp3.Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("Unexpected code $response")
        response.body?.string() ?: throw IOException("Empty response body")
    }

    private fun sendRequest(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@HomeActivity, "Failed to send request", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    runOnUiThread {
                        Toast.makeText(this@HomeActivity, "Request Sent Successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
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