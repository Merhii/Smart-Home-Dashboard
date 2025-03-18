package com.example.myapplication

import com.example.myapplication.api.PrayerApiService
import com.example.myapplication.UserApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

var gson: Gson = GsonBuilder()
    .setLenient()
    .create()

object RetrofitInstance {
    // User API (Your Localhost API)
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080")  // Localhost for user-related APIs
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: UserApi = retrofit.create(UserApi::class.java) // ✅ Define user API

    // Adhan Prayer API
    private val adhanRetrofit = Retrofit.Builder()
        .baseUrl("https://api.aladhan.com/") // Adhan API
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val prayerApi: PrayerApiService = adhanRetrofit.create(PrayerApiService::class.java) // ✅ Define Adhan API
}
