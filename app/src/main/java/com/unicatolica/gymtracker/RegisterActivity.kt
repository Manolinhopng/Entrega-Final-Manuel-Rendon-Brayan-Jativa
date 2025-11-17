package com.unicatolica.gymtracker

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.unicatolica.gymtracker.api.ApiClient
import com.unicatolica.gymtracker.data.User
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etEdad = findViewById<EditText>(R.id.etEdad)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPeso = findViewById<EditText>(R.id.etPeso)
        val spinnerGenero = findViewById<Spinner>(R.id.spinnerGenero)
        val checkTerms = findViewById<CheckBox>(R.id.checkTerms)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)

        val generos = arrayOf("Seleccionar", "Masculino", "Femenino", "Otro")
        spinnerGenero.adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            generos
        )

        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val edad = etEdad.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val peso = etPeso.text.toString().trim()
            val genero = spinnerGenero.selectedItem as String
            val aceptaTerminos = checkTerms.isChecked

            // Validación
            if (nombre.isEmpty() || edad.isEmpty() || email.isEmpty() || peso.isEmpty() || genero == "Seleccionar") {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!aceptaTerminos) {
                Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✨ Enviar al backend
            lifecycleScope.launch {
                try {
                    val user = User(
                        name = nombre,
                        age = edad.toIntOrNull() ?: 0,
                        email = email,
                        weight = peso,
                        height = "0", // Si no tienes campo de estatura, usa valor por defecto
                        gender = genero,
                        trainingFrequency = "",
                        dietType = ""
                    )

                    val response = ApiClient.service.register(user)

                    if (response.isSuccessful && response.body()?.success == true) {
                        val userId = response.body()!!.userId!!

                        // Guardar sesión
                        getSharedPreferences("user_session", Context.MODE_PRIVATE)
                            .edit()
                            .putString("userId", userId)
                            .putString("userName", nombre)
                            .apply()

                        Toast.makeText(this@RegisterActivity, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, DashboardActivity::class.java))
                        finish()
                    } else {
                        val errorMsg = response.body()?.message ?: "Error al registrar"
                        Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}