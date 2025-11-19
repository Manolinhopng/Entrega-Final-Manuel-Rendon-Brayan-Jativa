package com.unicatolica.gymtracker.data // Asegúrate de que esta sea tu package correcta

import com.google.gson.annotations.SerializedName

data class Timestamp(
    @SerializedName("_seconds") val seconds: Long,
    @SerializedName("_nanoseconds") val nanoseconds: Int
)

data class Routine(
    @SerializedName("id") val id: String, // <-- Ahora es obligatorio recibirlo en la respuesta
    @SerializedName("userId") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("duration") val duration: String?, // Puede ser null en la respuesta si no se calculó
    @SerializedName("date") val date: String,
    @SerializedName("exercises") val exercises: List<Exercise>, // <-- Kotlin ahora sabe qué es 'Exercise'
    @SerializedName("createdAt") val createdAt: Timestamp?, // O Date si lo conviertes
    @SerializedName("updatedAt") val updatedAt: Timestamp?, // O Date si lo conviertes
    @SerializedName("completed") val completed: Boolean = false // Valor por defecto si no se envía
)