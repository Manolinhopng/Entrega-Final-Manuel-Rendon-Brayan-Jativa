// src/main/java/com/unicatolica/gymtracker/data/User.kt
package com.unicatolica.gymtracker.data

import com.google.gson.annotations.SerializedName
import java.util.Date

data class User(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String,
    @SerializedName("age") val age: Int?,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String?,
    @SerializedName("weight") val weight: Double?,
    @SerializedName("height") val height: Double?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("trainingFrequency") val trainingFrequency: String?,
    @SerializedName("dietType") val dietType: String?,
)