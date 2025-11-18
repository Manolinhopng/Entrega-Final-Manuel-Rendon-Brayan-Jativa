// src/main/java/com/unicatolica/gymtracker/CreateRoutineActivity.kt
package com.unicatolica.gymtracker

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.unicatolica.gymtracker.api.ApiClient
import com.unicatolica.gymtracker.data.CreateRoutineRequest
import com.unicatolica.gymtracker.data.Exercise
import com.unicatolica.gymtracker.data.Routine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.widget.ImageView
class CreateRoutineActivity : AppCompatActivity() {

    private lateinit var llExerciseContainer: LinearLayout
    private lateinit var btnAddExercise: Button
    private lateinit var btnSaveRoutine: Button
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_routine)

        llExerciseContainer = findViewById(R.id.llExerciseContainer)
        btnAddExercise = findViewById(R.id.btnAddExercise)
        btnSaveRoutine = findViewById(R.id.btnSaveRoutine)
        btnBack = findViewById(R.id.btnBack)

        addExerciseBlock()

        btnAddExercise.setOnClickListener {
            addExerciseBlock()
        }

        btnSaveRoutine.setOnClickListener {
            sendRoutineToBackend()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun addExerciseBlock() {
        val inflater = LayoutInflater.from(this)
        val exerciseView = inflater.inflate(R.layout.item_exercise, llExerciseContainer, false)
        llExerciseContainer.addView(exerciseView)
    }

    private fun sendRoutineToBackend() {
        // 1. Validar y recolectar ejercicios
        val exercises = mutableListOf<Exercise>()

        for (i in 0 until llExerciseContainer.childCount) {
            val view = llExerciseContainer.getChildAt(i)
            val name = view.findViewById<EditText>(R.id.etExerciseName).text.toString().trim()
            val weightStr = view.findViewById<EditText>(R.id.etWeight).text.toString().trim()
            val repsStr = view.findViewById<EditText>(R.id.etReps).text.toString().trim()

            if (name.isEmpty() || weightStr.isEmpty() || repsStr.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos del ejercicio ${i + 1}", Toast.LENGTH_SHORT).show()
                return
            }

            // Convertir strings a números
            val weight = weightStr.toDoubleOrNull()
            val reps = repsStr.toIntOrNull()

            if (weight == null || reps == null) {
                Toast.makeText(this, "Peso y repeticiones deben ser números válidos", Toast.LENGTH_SHORT).show()
                return
            }

            exercises.add(Exercise(name, reps, weight))
        }

        if (exercises.isEmpty()) {
            Toast.makeText(this, "Agrega al menos un ejercicio", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Obtener userId del almacenamiento local
        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = prefs.getString("userId", null)
        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Crear objeto para enviar (usando el nuevo modelo)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) // Formato ISO
        val requestRoutine = CreateRoutineRequest( // <-- Usar CreateRoutineRequest
            userId = userId,
            name = "Rutina del $currentDate",
            exercises = exercises,
            date = currentDate,
            duration = "00:00:00" // O puedes calcularlo si implementas eso
        )

        // 4. Enviar al backend
        lifecycleScope.launch {
            try {
                // Enviar el objeto requestRoutine en lugar del modelo Routine
                val response = ApiClient.apiService.createRoutine(requestRoutine)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success) {
                        Toast.makeText(this@CreateRoutineActivity, apiResponse.message ?: "Rutina guardada correctamente", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@CreateRoutineActivity, DashboardActivity::class.java))
                        finish()
                    } else {
                        val errorMsg = apiResponse?.message ?: "Error desconocido del servidor"
                        Toast.makeText(this@CreateRoutineActivity, "Error: $errorMsg", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Datos inválidos (verifica campos)"
                        401 -> "No autorizado"
                        404 -> "Recurso no encontrado"
                        422 -> "Datos incompletos"
                        else -> "Error al guardar la rutina (Código: ${response.code()})"
                    }
                    Toast.makeText(this@CreateRoutineActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CreateRoutineActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}