package com.unicatolica.gymtracker.data
data class Exercise(
    val name: String,
    val reps: Int,
    val weight: Double? = null
)