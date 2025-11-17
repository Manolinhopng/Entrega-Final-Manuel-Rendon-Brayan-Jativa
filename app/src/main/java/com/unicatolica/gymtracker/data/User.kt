package com.unicatolica.gymtracker.data

data class User(
    val email: String,
    val name: String,
    val age: Int,
    val weight: String,
    val height: String,
    val gender: String,
    val trainingFrequency: String? = null,
    val dietType: String? = null
)