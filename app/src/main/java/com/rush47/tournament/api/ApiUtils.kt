package com.rush47.tournament.api

import com.google.gson.Gson
import com.rush47.tournament.models.ApiResponse
import retrofit2.Response

// Retrofit sirf 2xx (success) response par hi response.body() bharta hai.
// Agar server 400/401/409/500 jaisa error status bhejta hai (jaise "username
// already registered"), to response.body() NULL rehta hai — asli error message
// response.errorBody() ke andar hota hai. Pehle isko parse nahi kiya ja raha
// tha, isliye har validation error "Signup/Login failed" jaisa generic dikh
// raha tha aur asli wajah chhup jaati thi. Ye function dono cases handle karta hai.
fun extractApiResponse(response: Response<ApiResponse>): ApiResponse? {
    if (response.isSuccessful) {
        return response.body()
    }
    return try {
        val errorJson = response.errorBody()?.string()
        if (errorJson.isNullOrEmpty()) null else Gson().fromJson(errorJson, ApiResponse::class.java)
    } catch (e: Exception) {
        null
    }
}
