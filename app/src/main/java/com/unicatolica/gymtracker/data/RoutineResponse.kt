package com.unicatolica.gymtracker.data

import com.google.gson.annotations.SerializedName

data class RoutineResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("routine") val routine: Routine // <-- Aquí es donde se espera el objeto Routine
)