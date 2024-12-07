package com.example.myapplication

import com.example.myapplication.DTO.LoginUserDto
import com.example.myapplication.DTO.RegisterUserDto
import com.example.myapplication.DTO.VerifyUserDto
import com.example.myapplication.Entity.Automation
import com.example.myapplication.Entity.Device
import com.example.myapplication.Entity.User
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {
//    @GET("/getAll")
//   suspend fun getUsers() : Response<List<User>>
@POST("/auth/signup")
fun registerUser(@Body registerUserDto: RegisterUserDto): Call<User>

    @POST("/auth/verify")
    fun verifiyUser(@Body verifyUserDto: VerifyUserDto): Call<User>

    @POST("/auth/resend")
    fun resendVerificationCode(@Query("email") email: String): Call<String>

    @POST("/auth/login")
    fun loginUser(@Body loginUserDto: LoginUserDto): Call<User>

    @POST("/automation/add")
    fun createOrUpdateAutomation(@Body automation: Automation): Call<Automation>

    // Get all automations
    @GET("/automation/getall")
    fun getAllAutomations(): Call<List<Automation>>

    // Delete an automation by ID
    @DELETE("/automation/{id}")
    fun deleteAutomationById(@Path("id") id: Int): Call<String>

    @GET("/location/{location}")
    fun getDevicesByLocation(@Path("location") location: String): Call<List<Device>>

    // Update device status
    @PUT("/{deviceid}/{status}")
    fun updateDeviceStatus(@Path("deviceid") deviceId: Int, @Path("status") status: Int): Call<String>
}