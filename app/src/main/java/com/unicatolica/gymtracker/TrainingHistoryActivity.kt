// src/main/java/com/unicatolica/gymtracker/TrainingHistoryActivity.kt
package com.unicatolica.gymtracker

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.unicatolica.gymtracker.api.ApiClient
import com.unicatolica.gymtracker.data.Routine
import com.unicatolica.gymtracker.data.RoutinesResponse // Importar el nuevo modelo
import kotlinx.coroutines.launch

class TrainingHistoryActivity : AppCompatActivity() {

    private lateinit var llRoutinesContainer: LinearLayout
    private lateinit var btnSeeMore: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training_history)

        llRoutinesContainer = findViewById(R.id.llRoutinesContainer)
        btnSeeMore = findViewById(R.id.btnSeeMore)

        // Cargar rutinas reales desde el backend
        loadRoutinesFromBackend()

        btnSeeMore.setOnClickListener {
            Toast.makeText(this, "Cargando más rutinas...", Toast.LENGTH_SHORT).show()
            // Aquí podrías implementar paginación en el futuro
        }


    }

    private fun loadRoutinesFromBackend() {
        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = prefs.getString("userId", null)

        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getRoutines(userId)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && responseBody.success) {
                        // Accedemos a la lista de rutinas desde 'responseBody.routines'
                        val routines = responseBody.routines
                        displayRoutines(routines)
                    } else {
                        val errorMessage = responseBody?.message ?: "Error desconocido"
                        Toast.makeText(this@TrainingHistoryActivity, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@TrainingHistoryActivity, "Error HTTP ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TrainingHistoryActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayRoutines(routines: List<Routine>) {
        val inflater = LayoutInflater.from(this)

        // Limpiar contenedor (por si se llama dos veces)
        llRoutinesContainer.removeAllViews()

        if (routines.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "No tienes rutinas guardadas"
                setPadding(32, 32, 32, 32)
                textSize = 16f
            }
            llRoutinesContainer.addView(emptyView)
            return
        }

        for (routine in routines) {
            val routineView = inflater.inflate(R.layout.item_routine, llRoutinesContainer, false)

            val tvName = routineView.findViewById<TextView>(R.id.tvRoutineName)
            val tvDuration = routineView.findViewById<TextView>(R.id.tvDuration)
            val tvDate = routineView.findViewById<TextView>(R.id.tvDate)

            tvName.text = routine.name
            tvDuration.text = "Duración ${routine.duration ?: "N/A"}"
            tvDate.text = "Fecha : ${routine.date ?: "N/A"}"

            llRoutinesContainer.addView(routineView)
        }
    }
}