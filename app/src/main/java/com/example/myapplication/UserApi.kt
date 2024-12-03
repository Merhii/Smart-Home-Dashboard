package com.example.myapplication

import com.example.myapplication.DTO.RegisterUserDto
import com.example.myapplication.DTO.VerifyUserDto
import com.example.myapplication.Entity.User
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserApi {
//    @GET("/getAll")
//   suspend fun getUsers() : Response<List<User>>
@POST("/auth/signup")
fun registerUser(@Body registerUserDto: RegisterUserDto): Call<User>

@POST("/auth/verify")
fun verifiyUser(@Body verifyUserDto: VerifyUserDto): Call<User>
}