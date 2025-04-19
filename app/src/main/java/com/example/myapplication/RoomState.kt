// RoomState.kt
package com.example.myapplication

object RoomState {
    val roomStates = mutableMapOf<String, MutableMap<String, Boolean>>()

    init {
        roomStates["Bedroom"] = mutableMapOf(
            "Lights" to false,
            "Curtains" to false
        )
        roomStates["Livingroom"] = mutableMapOf(
            "Lights" to false,
            "Curtains" to false,
            "AC" to false
        )
        roomStates["Bathroom"] = mutableMapOf(
            "Lights" to false,
            "Heater" to false
        )
        roomStates["Kitchen"] = mutableMapOf("Lights" to false)
        roomStates["Entrance"] = mutableMapOf("Lights" to false)
        roomStates["Garage"] = mutableMapOf("Lights" to false)
        roomStates["Electric"] = mutableMapOf("Lights" to false)
        roomStates["Laundry"] = mutableMapOf("Lights" to false)
    }
}
