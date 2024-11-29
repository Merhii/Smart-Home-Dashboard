package com.example.myapplication

data class User(
    val email: String,
    val googleid: String,
    val name: String,
    val password: String,
    val userid: Int,

    val auth: AuthType
) {
    enum class AuthType {
        local, google
    }
}
