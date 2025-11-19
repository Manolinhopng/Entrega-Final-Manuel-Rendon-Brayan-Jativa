package com.unicatolica.gymtracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.unicatolica.gymtracker.data.ApiRepository
import com.unicatolica.gymtracker.data.TrainingStats

class ApiViewModel(
    private val repository: ApiRepository
) : ViewModel() {

    fun getTrainingStats(userId: String): LiveData<TrainingStats> {
        return liveData {
            try {
                val stats = repository.getTrainingStatsFromApi(userId)
                emit(stats)
            } catch (e: Exception) {
                e.printStackTrace()
                emit(TrainingStats()) // Return empty stats instead of null
            }
        }
    }
}