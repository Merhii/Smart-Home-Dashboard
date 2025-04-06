package com.example.myapplication


object IP {
    var ip: String = "http://192.168.1.117"
    var ledon : String = ip+"/led/on"
    var ledoff : String = ip+"/led/off"
    var curtsoff : String = ip+"/curts/off"
    var curtson : String = ip+"/curts/on"
    var test : String= ip+"/test"
}
