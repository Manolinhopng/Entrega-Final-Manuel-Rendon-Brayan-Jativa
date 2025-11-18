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
        val etAltura = findViewById<EditText>(R.id.etAltura)
        val etPassword = findViewById<EditText>(R.id.etPassword)
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
            val altura = etAltura.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val genero = spinnerGenero.selectedItem as String
            val aceptaTerminos = checkTerms.isChecked

            // Validación
            if (nombre.isEmpty() || edad.isEmpty() || email.isEmpty() ||
                peso.isEmpty() || altura.isEmpty() || password.isEmpty() ||
                genero == "Seleccionar") {

                Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!aceptaTerminos) {
                Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validación de formato de email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Formato de email inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validación de edad
            val edadInt = edad.toIntOrNull()
            if (edadInt == null || edadInt <= 0) {
                Toast.makeText(this, "Edad inválida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validación de peso
            val pesoDouble = peso.toDoubleOrNull()
            if (pesoDouble == null || pesoDouble <= 0) {
                Toast.makeText(this, "Peso inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validación de altura
            val alturaDouble = altura.toDoubleOrNull()
            if (alturaDouble == null || alturaDouble <= 0) {
                Toast.makeText(this, "Altura inválida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validación de contraseña (mínimo 6 caracteres)
            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            lifecycleScope.launch {
                try {
                    val user = User(
                        id = null,
                        name = nombre,
                        age = edadInt,
                        email = email,
                        password = password,
                        weight = pesoDouble,
                        height = alturaDouble,
                        gender = genero,
                        trainingFrequency = "",
                        dietType = ""
                    )

                    val response = ApiClient.apiService.register(user)

                    if (response.isSuccessful && response.body()?.success == true) {
                        val userId = response.body()?.userId ?: ""
                        val message = response.body()?.message ?: "Registro exitoso"

                        // Guardar sesión
                        getSharedPreferences("user_session", Context.MODE_PRIVATE)
                            .edit()
                            .putString("userId", userId)
                            .putString("userName", nombre)
                            .putString("userEmail", email)
                            .apply()

                        Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
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