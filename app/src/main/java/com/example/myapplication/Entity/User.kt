package com.example.myapplication.Entity

import java.time.LocalDateTime

data class User(
    val email: String,
    val googleid: String,
    val username: String,
    val password: String,
    val userid: Int,
    val enable: Boolean,
    val verificationCode: String,
    val verificationCodeExpiresAt: String,
    val auth: AuthType
) {
    enum class AuthType {
        local, google
    }
}
