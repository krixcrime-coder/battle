package com.rush47.tournament.models

import com.google.gson.JsonElement

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: JsonElement?
)
