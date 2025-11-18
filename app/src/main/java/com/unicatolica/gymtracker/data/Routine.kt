package com.unicatolica.gymtracker.data

data class Routine(
    val id: String = "", // Puede mantenerse como String si siempre se devuelve, o String? si puede ser null
    val userId: String,  // Requerido
    val name: String,    // Requerido
    val duration: String?, // <-- Cambiado a String? si puede ser null
    val date: String?,   // <-- Cambiado a String? si puede ser null
    val exercises: List<Exercise> = emptyList()
)