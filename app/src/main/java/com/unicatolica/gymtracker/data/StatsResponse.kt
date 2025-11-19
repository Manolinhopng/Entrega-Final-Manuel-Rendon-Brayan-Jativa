package com.unicatolica.gymtracker.data

import com.google.gson.annotations.SerializedName

data class StatsResponse(
    val success: Boolean,
    val stats: TrainingStats?
)
