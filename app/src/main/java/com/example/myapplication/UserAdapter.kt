package com.example.myapplication

class UserAdapter {
    private val userApi = RetrofitInstance.api

    suspend fun getUsers(): List<User>? {
        val response = userApi.getUsers()
        return if (response.isSuccessful) {
            response.body()
        } else {
            null // Handle API error (e.g., log or throw an exception if needed)
        }}

}