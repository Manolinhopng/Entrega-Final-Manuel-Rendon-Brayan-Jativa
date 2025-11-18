package com.unicatolica.gymtracker

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.unicatolica.gymtracker.api.ApiClient
import com.unicatolica.gymtracker.data.User // Asegúrate de tener este modelo importado
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var etPeso: EditText
    private lateinit var etEstatura: EditText
    private lateinit var etFrecuencia: EditText
    private lateinit var etDieta: EditText
    private lateinit var btnGuardar: Button

    private var currentUser: User? = null // Para mantener los datos originales si es necesario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvUserName = findViewById(R.id.tvUserName)
        etPeso = findViewById(R.id.etPeso)
        etEstatura = findViewById(R.id.etEstatura)
        etFrecuencia = findViewById(R.id.etFrecuencia)
        etDieta = findViewById(R.id.etDieta)
        btnGuardar = findViewById(R.id.btnGuardar)

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
                val response = ApiClient.apiService.getProfile(userId)

                val apiResponse = response.body()

                if (response.isSuccessful && apiResponse?.success == true && apiResponse.data != null) {
                    val user = apiResponse.data
                    currentUser = user // Guardamos el usuario original para usarlo en la actualización

                    // Mostrar datos del usuario
                    tvUserName.text = user.name
                    etPeso.setText(user.weight?.toString() ?: "")
                    etEstatura.setText(user.height?.toString() ?: "")
                    etFrecuencia.setText(user.trainingFrequency ?: "")
                    etDieta.setText(user.dietType ?: "")

                } else {
                    Toast.makeText(
                        this@ProfileActivity,
                        apiResponse?.message ?: "Error al cargar el perfil",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun saveUserProfile() {
        val pesoStr = etPeso.text.toString().trim()
        val estaturaStr = etEstatura.text.toString().trim()
        val frecuencia = etFrecuencia.text.toString().trim()
        val dieta = etDieta.text.toString().trim()

        if (pesoStr.isEmpty() || estaturaStr.isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios (Peso y Estatura)", Toast.LENGTH_SHORT).show()
            return
        }

        val peso = pesoStr.toDoubleOrNull()
        val estatura = estaturaStr.toDoubleOrNull()

        if (peso == null || estatura == null) {
            Toast.makeText(this, "Peso y estatura deben ser números válidos", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = prefs.getString("userId", null) ?: run {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val userToUpdate = User(
            id = userId,
            name = currentUser?.name ?: "",
            email = currentUser?.email ?: "",
            password = currentUser?.password ?:"",
            age = currentUser?.age,
            gender = currentUser?.gender,
            weight = peso,
            height = estatura,
            trainingFrequency = frecuencia,
            dietType = dieta
        )

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.updateProfile(userId, userToUpdate)

                if (response.isSuccessful) {
                    val apiResponse = response.body()

                    if (apiResponse != null && apiResponse.success) {
                        Toast.makeText(
                            this@ProfileActivity,
                            apiResponse.message ?: "Datos guardados correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            apiResponse?.message ?: "Error al guardar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Error HTTP ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}