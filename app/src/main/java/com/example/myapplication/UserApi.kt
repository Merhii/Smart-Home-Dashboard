package com.example.myapplication

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserApi {
    @GET("/getAll")
   suspend fun getUsers() : Response<List<User>>

//    @POST("/ibciw")
//    fun create(@Body) : Response<User>

}