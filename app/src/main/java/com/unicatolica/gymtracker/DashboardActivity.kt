package com.unicatolica.gymtracker

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class DashboardActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        prefs = getSharedPreferences("user_session", MODE_PRIVATE)

        val tvRegisterTraining = findViewById<TextView>(R.id.tvRegisterTraining)
        val tvTrainingHistory = findViewById<TextView>(R.id.tvTrainingHistory)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)
        val logoutContainer = findViewById<LinearLayout>(R.id.logoutContainer)

        tvRegisterTraining.setOnClickListener {
            startActivity(Intent(this, CreateRoutineActivity::class.java))
        }

        tvTrainingHistory.setOnClickListener {
            // ✅ Aquí se abre la actividad que cargará los datos reales
            startActivity(Intent(this, TrainingHistoryActivity::class.java))
        }

        ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        logoutContainer.setOnClickListener {
            // Limpiar sesión
            prefs.edit().clear().apply()
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}