package com.unicatolica.gymtracker.data

data class Routine(
    val id: String = "",
    val userId: String,
    val name: String,
    val duration: String,
    val date: String,
    val exercises: List<Exercise> = emptyList()
)