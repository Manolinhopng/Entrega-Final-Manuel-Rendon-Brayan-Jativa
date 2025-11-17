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
import kotlinx.coroutines.launch

class TrainingHistoryActivity : AppCompatActivity() {

    private lateinit var llRoutinesContainer: LinearLayout
    private lateinit var btnSeeMore: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training_history)

        llRoutinesContainer = findViewById(R.id.llRoutinesContainer)
        btnSeeMore = findViewById(R.id.btnSeeMore)
        btnBack = findViewById(R.id.btnBack)

        // Cargar rutinas reales desde el backend
        loadRoutinesFromBackend()

        btnSeeMore.setOnClickListener {
            Toast.makeText(this, "Cargando más rutinas...", Toast.LENGTH_SHORT).show()
            // Aquí podrías implementar paginación en el futuro
        }

        btnBack.setOnClickListener {
            finish()
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
                val response = ApiClient.service.getRoutines(userId)
                if (response.isSuccessful) {
                    val routines = response.body() ?: emptyList()
                    displayRoutines(routines)
                } else {
                    Toast.makeText(this@TrainingHistoryActivity, "Error al cargar rutinas", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TrainingHistoryActivity, "Error de red", Toast.LENGTH_SHORT).show()
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
            tvDuration.text = "Duración ${routine.duration}"
            tvDate.text = "Fecha : ${routine.date}"

            llRoutinesContainer.addView(routineView)
        }
    }
}