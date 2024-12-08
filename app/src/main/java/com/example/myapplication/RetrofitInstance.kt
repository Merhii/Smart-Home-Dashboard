package com.example.myapplication

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


//val url="http://10.0.2.2:8080"
var gson: Gson? = GsonBuilder()
    .setLenient()
    .create()

object RetrofitInstance {
    val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080") // local host
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService = retrofit.create(UserApi::class.java)
}