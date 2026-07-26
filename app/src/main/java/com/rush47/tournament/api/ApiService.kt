package com.rush47.tournament.api

import com.rush47.tournament.models.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("register.php")
    fun register(@Body body: Map<String, String>): Call<ApiResponse>

    @POST("login.php")
    fun login(@Body body: Map<String, String>): Call<ApiResponse>

    @POST("forgot_password.php")
    fun forgotPassword(@Body body: Map<String, String>): Call<ApiResponse>

    @POST("reset_password.php")
    fun resetPassword(@Body body: Map<String, String>): Call<ApiResponse>
}
