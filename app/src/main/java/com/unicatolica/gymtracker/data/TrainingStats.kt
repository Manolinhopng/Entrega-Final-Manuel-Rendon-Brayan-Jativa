package com.unicatolica.gymtracker.data

data class TrainingStats(
    val totalSessions: Int = 0,
    val totalDurationSeconds: Int = 0,
    val totalWeightLifted: Double = 0.0,
    val weightProgression: List<Double> = emptyList()
)

data class GetStatsResponse(
    val success: Boolean,
    val stats: TrainingStats? = null,
    val message: String? = null
)