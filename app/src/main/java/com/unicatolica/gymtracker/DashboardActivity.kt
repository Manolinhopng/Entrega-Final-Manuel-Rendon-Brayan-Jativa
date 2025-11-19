package com.unicatolica.gymtracker

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.unicatolica.gymtracker.viewmodel.ApiViewModel
import com.unicatolica.gymtracker.viewmodel.ApiViewModelFactory
import com.unicatolica.gymtracker.data.ApiRepository
import com.unicatolica.gymtracker.data.TrainingStats
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import android.graphics.Color
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var viewModel: ApiViewModel
    private lateinit var tvTotalSessions: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var tvTotalWeight: TextView
    private lateinit var chart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        prefs = getSharedPreferences("user_session", MODE_PRIVATE)

        initializeViews()
        setupViewModel()
        setupChart()
        setupClickListeners()

        val userId = prefs.getString("userId", "") ?: ""
        Log.d("Dashboard", "UserId obtenido en onCreate: '$userId'")

        if (userId.isNotEmpty()) {
            loadTrainingStats(userId)
        } else {
            Log.e("Dashboard", "UserId es vacío o nulo en onCreate. ¿El usuario está autenticado?")
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_LONG).show()
            redirectToLogin()
        }
    }

    private fun initializeViews() {
        tvTotalSessions = findViewById(R.id.tvTotalSessions)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        tvTotalWeight = findViewById(R.id.tvTotalWeight)
        chart = findViewById(R.id.chartWeight)
    }

    private fun setupViewModel() {
        val repository = ApiRepository()
        val factory = ApiViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ApiViewModel::class.java]
    }

    private fun setupClickListeners() {
        val tvRegisterTraining = findViewById<TextView>(R.id.tvRegisterTraining)
        val tvTrainingHistory = findViewById<TextView>(R.id.tvTrainingHistory)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)
        val logoutContainer = findViewById<LinearLayout>(R.id.logoutContainer)

        tvRegisterTraining.setOnClickListener {
            startActivity(Intent(this, CreateRoutineActivity::class.java))
        }

        tvTrainingHistory.setOnClickListener {
            startActivity(Intent(this, TrainingHistoryActivity::class.java))
        }

        ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        logoutContainer.setOnClickListener {
            logoutUser()
        }
    }

    private fun loadTrainingStats(userId: String) {
        viewModel.getTrainingStats(userId).observe(this) { result -> // 'result' ahora es de tipo TrainingStats
            Log.d("Dashboard", "Observador de stats llamado. Resultado: $result")

            if (result != null) {
                // Asumiendo que si 'result' no es nulo, es un objeto TrainingStats válido
                Log.d("Dashboard", "Datos recibidos: Sesiones=${result.totalSessions}, Duración=${result.totalDurationSeconds}, Peso=${result.totalWeightLifted}")
                if (result.totalSessions > 0) {
                    updateStatsUI(result)
                    updateChartWithData(result)
                } else {
                    Log.d("Dashboard", "Datos recibidos, pero totalSessions es 0. Mostrando estado vacío.")
                    showEmptyState()
                }
            } else {
                Log.e("Dashboard", "Observador recibió 'null' como resultado de TrainingStats.")
                showEmptyState() // Manejar caso de resultado nulo
            }
        }
    }


    private fun updateStatsUI(stats: TrainingStats) {
        tvTotalSessions.text = stats.totalSessions.toString()
        tvTotalTime.text = formatDuration(stats.totalDurationSeconds.toLong())
        tvTotalWeight.text = "${stats.totalWeightLifted.toInt()} kg"
    }

    private fun formatDuration(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    private fun setupChart() {
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            setBackgroundColor(Color.WHITE)

            // X-axis configuration
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.BLACK
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value >= 0) "Día ${value.toInt() + 1}" else ""
                    }
                }
            }

            // Left Y-axis configuration
            axisLeft.apply {
                textColor = Color.BLACK
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                axisMinimum = 0f
                granularity = 50f // Adjust based on your data range
            }

            axisRight.isEnabled = false

            // Legend configuration
            legend.apply {
                textColor = Color.BLACK
                isEnabled = true
            }
        }
    }

    private fun updateChartWithData(stats: TrainingStats) {

        if (stats.weightProgression == null || stats.weightProgression.isEmpty()) {
            Log.d("Dashboard", "updateChartWithData: weightProgression es null o vacío.")
            chart.clear()
            chart.invalidate()
            return
        }

        val entries = ArrayList<Entry>()

        // Convertir datos reales del backend a puntos del gráfico
        stats.weightProgression.forEachIndexed { index, weight ->
            entries.add(Entry(index.toFloat(), weight.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Progreso de Peso (kg)").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            lineWidth = 2f
            setCircleColor(Color.BLUE)
            circleRadius = 4f
            setDrawCircleHole(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(false)
        }

        chart.data = LineData(dataSet)
        chart.invalidate()
    }

    private fun showEmptyState() {
        Log.d("Dashboard", "showEmptyState llamado.")
        tvTotalSessions.text = "0"
        tvTotalTime.text = "0m"
        tvTotalWeight.text = "0 kg"

        // Clear chart data
        chart.clear()
        chart.invalidate()

        // Opcional: Mostrar un Toast indicando que no hay datos
        // Toast.makeText(this, "No hay datos de entrenamiento disponibles", Toast.LENGTH_SHORT).show()
    }

    private fun logoutUser() {
        prefs.edit().clear().apply()
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        Log.d("Dashboard", "onResume llamado")
        // Refresh data when returning to dashboard
        val userId = prefs.getString("userId", "") ?: ""
        if (userId.isNotEmpty()) {
            loadTrainingStats(userId)
        } else {
            Log.e("Dashboard", "UserId es vacío o nulo en onResume.")
            // Opcional: redirigir al login si se perdió la sesión
            // redirectToLogin()
        }
    }
}