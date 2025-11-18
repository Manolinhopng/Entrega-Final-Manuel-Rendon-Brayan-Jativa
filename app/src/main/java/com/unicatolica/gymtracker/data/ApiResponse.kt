// src/main/java/com/unicatolica/gymtracker/data/ApiResponse.kt
package com.unicatolica.gymtracker.data

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T?
)