// src/main/java/com/unicatolica/gymtracker/CreateRoutineActivity.kt
package com.unicatolica.gymtracker

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.unicatolica.gymtracker.api.ApiClient
import com.unicatolica.gymtracker.data.CreateRoutineRequest
import com.unicatolica.gymtracker.data.Exercise
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CreateRoutineActivity : AppCompatActivity() {

    private lateinit var llExerciseContainer: LinearLayout
    private lateinit var btnAddExercise: Button
    private lateinit var btnSaveRoutine: Button
    private lateinit var btnBack: ImageView
    private lateinit var etDuration: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_routine)

        // Inicializar vistas
        llExerciseContainer = findViewById(R.id.llExerciseContainer)
        btnAddExercise = findViewById(R.id.btnAddExercise)
        btnSaveRoutine = findViewById(R.id.btnSaveRoutine)
        btnBack = findViewById(R.id.btnBack)
        etDuration = findViewById(R.id.etDuration)

        // Agregar primer ejercicio por defecto
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

    /** Agrega dinámicamente un bloque de ejercicio */
    private fun addExerciseBlock() {
        val inflater = LayoutInflater.from(this)
        val exerciseView =
            inflater.inflate(R.layout.item_exercise, llExerciseContainer, false)
        llExerciseContainer.addView(exerciseView)
    }

    /** Envía al backend la rutina creada */
    private fun sendRoutineToBackend() {

        // Obtener duración ingresada
        val durationInput = etDuration.text.toString().trim()
        val finalDuration = if (durationInput.isEmpty()) "00:30:00" else durationInput

        // Validar y recolectar ejercicios
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

        // Obtener userId
        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = prefs.getString("userId", null)

        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear objeto para enviar
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val requestRoutine = CreateRoutineRequest(
            userId = userId,
            name = "Rutina del $currentDate",
            exercises = exercises,
            date = currentDate,
            duration = finalDuration
        )

        // Enviar al backend
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.createRoutine(requestRoutine)

                if (response.isSuccessful) {
                    val apiResponse = response.body()

                    if (apiResponse != null && apiResponse.success) {
                        val routineId = apiResponse.data?.id

                        if (routineId != null) {
                            // ✅ Rutina creada, ahora la marcamos como completada
                            markRoutineAsCompleted(routineId, finalDuration)
                        } else {
                            Toast.makeText(this@CreateRoutineActivity, "Error: ID de rutina no recibido", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(this@CreateRoutineActivity, apiResponse?.message ?: "Error desconocido", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val msg = when (response.code()) {
                        400 -> "Datos inválidos"
                        401 -> "No autorizado"
                        404 -> "No encontrado"
                        422 -> "Datos incompletos"
                        else -> "Error (código ${response.code()})"
                    }
                    Toast.makeText(this@CreateRoutineActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CreateRoutineActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun markRoutineAsCompleted(routineId: String, duration: String) {
        lifecycleScope.launch {
            try {
                // ✅ Convertir los valores a String
                val updateData = mapOf(
                    "completed" to true.toString(), // <-- Convertido a String ("true")
                    "duration" to duration          // <-- Ya es String
                )
                val response = ApiClient.apiService.updateRoutine(routineId, updateData)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success) {
                        Toast.makeText(this@CreateRoutineActivity, "Rutina creada y marcada como completada", Toast.LENGTH_SHORT).show()
                        // ✅ Navegar al DashboardActivity
                        startActivity(Intent(this@CreateRoutineActivity, DashboardActivity::class.java))
                        // ✅ Opcional: finish() para que al presionar "back" desde el Dashboard no vuelva a la creación
                        finish()
                    } else {
                        Toast.makeText(this@CreateRoutineActivity, "Rutina creada, pero error al marcar como completada: ${apiResponse?.message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@CreateRoutineActivity, "Rutina creada, pero error HTTP al marcar como completada: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CreateRoutineActivity, "Rutina creada, pero error de red al marcar como completada: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}