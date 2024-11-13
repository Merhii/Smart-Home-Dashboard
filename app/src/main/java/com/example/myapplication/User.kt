package com.example.myapplication

class User {

    var UserId: Int =0
    var name: String =""
    var email: String =""
    var isAdmin: Boolean =false

    constructor(email:String,name:String,isAdmin:Boolean)
    {
        this.email=email
        this.name=name
        this.isAdmin=isAdmin
    }
}