package com.unicatolica.gymtracker

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.unicatolica.gymtracker.api.ApiClient
import com.unicatolica.gymtracker.data.User
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var etPeso: EditText
    private lateinit var etEstatura: EditText
    private lateinit var etFrecuencia: EditText
    private lateinit var etDieta: EditText
    private lateinit var btnGuardar: Button

    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        etPeso = findViewById(R.id.etPeso)
        etEstatura = findViewById(R.id.etEstatura)
        etFrecuencia = findViewById(R.id.etFrecuencia)
        etDieta = findViewById(R.id.etDieta)
        btnGuardar = findViewById(R.id.btnGuardar)

        // Cargar datos del backend
        loadUserProfile()

        btnGuardar.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun loadUserProfile() {
        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = prefs.getString("userId", null)

        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val response = ApiClient.service.getProfile(userId)
                if (response.isSuccessful) {
                    val user = response.body()!!
                    currentUser = user

                    // Mostrar datos en los campos
                    etPeso.setText(user.weight)
                    etEstatura.setText(user.height)
                    etFrecuencia.setText(user.trainingFrequency)
                    etDieta.setText(user.dietType)
                } else {
                    Toast.makeText(this@ProfileActivity, "Error al cargar el perfil", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserProfile() {
        val peso = etPeso.text.toString().trim()
        val estatura = etEstatura.text.toString().trim()
        val frecuencia = etFrecuencia.text.toString().trim()
        val dieta = etDieta.text.toString().trim()

        if (peso.isEmpty() || estatura.isEmpty() || frecuencia.isEmpty() || dieta.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = prefs.getString("userId", null) ?: return

        // Actualizar el objeto usuario
        val updatedUser = currentUser?.copy(
            weight = peso,
            height = estatura,
            trainingFrequency = frecuencia,
            dietType = dieta
        ) ?: User(
            id = userId,
            name = prefs.getString("userName", "Usuario") ?: "Usuario",
            age = 0,
            email = "",
            weight = peso,
            height = estatura,
            gender = "",
            trainingFrequency = frecuencia,
            dietType = dieta
        )

        lifecycleScope.launch {
            try {
                val response = ApiClient.service.updateProfile(userId, updatedUser)
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@ProfileActivity, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}