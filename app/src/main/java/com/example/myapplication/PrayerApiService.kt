package com.example.myapplication.api

import retrofit2.Call
import retrofit2.http.GET

interface PrayerApiService {
    @GET("v1/timingsByCity?city=Beirut&country=Lebanon&method=2")
    fun getPrayerTimes(): Call<PrayerTimesResponse>
}
