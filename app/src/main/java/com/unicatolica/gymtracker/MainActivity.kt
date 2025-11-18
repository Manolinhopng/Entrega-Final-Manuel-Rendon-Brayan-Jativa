package com.unicatolica.gymtracker

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.unicatolica.gymtracker.api.ApiClient
import com.unicatolica.gymtracker.data.LoginRequest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = prefs.getString("userId", null)
        val rememberMe = prefs.getBoolean("rememberMe", false)

        if (userId != null && rememberMe) {
            // Redirige automáticamente
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val checkBoxRememberMe = findViewById<CheckBox>(R.id.checkBoxRememberMe)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val tvRegisterLink = findViewById<TextView>(R.id.tvRegisterLink)

        checkBoxRememberMe.isChecked = rememberMe

        // Navegar a Registro
        tvRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Login con llamada al backend
        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✨ Llamada real al backend
            lifecycleScope.launch {
                try {
                    val response = ApiClient.apiService.login(LoginRequest(email, password))

                    if (response.isSuccessful && response.body()?.success == true) {
                        val userId = response.body()!!.userId!!

                        // Guardar userId para usarlo después (ej: en historial, perfil)
                        getSharedPreferences("user_session", Context.MODE_PRIVATE)
                            .edit()
                            .putString("userId", userId)
                            .putBoolean("rememberMe", checkBoxRememberMe.isChecked)
                            .apply()

                        Toast.makeText(this@MainActivity, "Login exitoso", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@MainActivity, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Mensaje del backend o genérico
                        val errorMsg = response.body()?.message ?: "Credenciales incorrectas"
                        Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}