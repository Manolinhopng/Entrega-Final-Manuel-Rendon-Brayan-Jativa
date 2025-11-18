package com.unicatolica.gymtracker.data

import com.google.gson.annotations.SerializedName

data class RoutinesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("routines") val routines: List<Routine>
)