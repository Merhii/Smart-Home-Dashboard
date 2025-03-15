// RoomState.kt
package com.example.myapplication

object RoomState {
    // A map to store the state of devices in each room
    val roomStates = mutableMapOf<String, MutableMap<String, Boolean>>()

    init {
        // Initialize the state for all rooms and devices
        roomStates["Bedroom"] = mutableMapOf("Lights" to false, "Curtains" to false)
        roomStates["Livingroom"] = mutableMapOf("Lights" to false, "Curtains" to false)
        roomStates["Bathroom"] = mutableMapOf("Lights" to false)
        roomStates["Kitchen"] = mutableMapOf("Lights" to false)
        roomStates["Entrance"] = mutableMapOf("Lights" to false)
        roomStates["Garage"] = mutableMapOf("Lights" to false)
    }
}