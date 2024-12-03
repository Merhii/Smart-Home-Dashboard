package com.example.myapplication

import com.example.myapplication.Entity.User
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
//val url="http://10.0.2.2:8080"
object RetrofitInstance {
    val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080") // Replace with your server's base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService = retrofit.create(UserApi::class.java)
}