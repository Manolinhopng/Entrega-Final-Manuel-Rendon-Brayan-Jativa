package com.unicatolica.gymtracker

import android.os.Bundle
import android.content.Context
import android.util.Log // Asegúrate de importar Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.unicatolica.gymtracker.api.ApiClient
import com.unicatolica.gymtracker.data.Routine
import kotlinx.coroutines.launch

class EditRoutineActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etDuration: EditText
    private lateinit var etDate: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var btnBack: ImageView

    private var routineId: String? = null
    private var loadedRoutine: Routine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_routine)

        println("DEBUG: onCreate EditRoutineActivity")

        // Recoger ID
        routineId = intent.getStringExtra("routineId")
        println("DEBUG: Recibido routineId: $routineId")

        if (routineId == null) {
            Toast.makeText(this, "Error: rutina inválida", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Views
        etName = findViewById(R.id.etName)
        etDuration = findViewById(R.id.etDuration)
        etDate = findViewById(R.id.etDate)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }
        btnCancel.setOnClickListener { finish() }
        btnSave.setOnClickListener { saveChanges() }

        loadRoutineDetails() // Llama a la función una vez
    }

    // 📌 Cargar la rutina desde el backend (Definición única)
// En EditRoutineActivity.kt
    private fun loadRoutineDetails() {
        lifecycleScope.launch {
            try {
                val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
                val userId = prefs.getString("userId", null)

                if (userId == null) {
                    Toast.makeText(this@EditRoutineActivity, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                // ✅ Llama a la API con el ID de la rutina y el ID del usuario
                val response = ApiClient.apiService.getRoutineById(routineId!!, userId)

                if (response.isSuccessful && response.body()?.success == true && response.body()?.routine != null) {
                    // ... procesa los datos ...
                } else {
                    // ... maneja el error ...
                }
            } catch (e: Exception) {
                // ... maneja la excepción ...
            }
        }
    }

    private fun saveChanges() {
        val updatedName = etName.text.toString().trim()
        val updatedDuration = etDuration.text.toString().trim()
        val updatedDate = etDate.text.toString().trim()

        if (updatedName.isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val requestBody = mapOf(
                    "name" to updatedName,
                    "duration" to updatedDuration,
                    "date" to updatedDate
                )

                // ✅ También necesitas pasar el userId a updateRoutine
                // Asegúrate de que tu ApiClient.apiService.updateRoutine también espere el userId
                // Si no lo espera, y el backend lo requiere, debes actualizarlo también en ApiService y ApiClient
                // Por ahora, asumiremos que updateRoutine NO requiere userId directamente, solo para operaciones de lectura.
                // Si el backend lo requiere para update, también debes modificarlo.

                val response = ApiClient.apiService.updateRoutine(
                    routineId!!, // Este es el ID de la rutina
                    requestBody
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@EditRoutineActivity, "Rutina actualizada", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditRoutineActivity, "Error al guardar: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@EditRoutineActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}