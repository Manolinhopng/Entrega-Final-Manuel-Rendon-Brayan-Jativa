// src/main/java/com/unicatolica/gymtracker/data/ApiRepository.kt
package com.unicatolica.gymtracker.data

import com.unicatolica.gymtracker.api.ApiClient
import com.unicatolica.gymtracker.api.ApiService // ✅ Importa ApiService
import retrofit2.HttpException
import java.io.IOException

class ApiRepository(
    private val apiService: ApiService = ApiClient.apiService // Inyectar ApiService
) {

    suspend fun getTrainingStatsFromApi(userId: String): TrainingStats {
        return try {
            val response = apiService.getTrainingStats(userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.stats != null) {
                    body.stats
                } else {

                    TrainingStats()
                }
            } else {
                // Error HTTP (400, 401, 500, etc.)
                TrainingStats()
            }
        } catch (e: IOException) {
            // Error de red
            e.printStackTrace()
            TrainingStats()
        } catch (e: HttpException) {
            // Error específico de HTTP con Retrofit
            e.printStackTrace()
            TrainingStats()
        }
    }
}