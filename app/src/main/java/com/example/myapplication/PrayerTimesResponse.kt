package com.example.myapplication.api

data class PrayerTimesResponse(
    val data: PrayerData
)

data class PrayerData(
    val timings: PrayerTimings
)

data class PrayerTimings(
    val Fajr: String,
    val Dhuhr: String,
    val Asr: String,
    val Maghrib: String,
    val Isha: String
)
