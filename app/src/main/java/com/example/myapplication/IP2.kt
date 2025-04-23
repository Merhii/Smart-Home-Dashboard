package com.example.myapplication

object IP2 {
    const val baseUrl = "http://192.168.1.144"

    // Curtain Control
    val openCurtains = "$baseUrl/curts/open"
    val closeCurtains = "$baseUrl/curts/close"

    // AC Control
    val acOn = "$baseUrl/ac/on"
    val acOff = "$baseUrl/ac/of"

    // Sensor Status Endpoints
    val flameStatus = "$baseUrl/flame/status"
    val gasStatus = "$baseUrl/gas/status"
    val touchStatus = "$baseUrl/touch/status"
    val LDRT= "$baseUrl/ldrT"
    val LDRF= "$baseUrl/ldrF"
}
