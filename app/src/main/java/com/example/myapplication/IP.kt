package com.example.myapplication


object IP {
    var ip: String = "http://192.168.10.224"
    var ledon : String = ip+"/led/on"
    var ledoff : String = ip+"/led/off"
    var curtsoff : String = ip+"/curts/off"
    var curtson : String = ip+"/curts/on"
}
