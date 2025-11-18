package com.unicatolica.gymtracker.data

import com.google.gson.annotations.SerializedName

data class CreateRoutineRequest(
    @SerializedName("userId") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("exercises") val exercises: List<Exercise>,
    @SerializedName("date") val date: String?, // Opcional o bien formateado
    @SerializedName("duration") val duration: String? // Opcional o bien formateado
)