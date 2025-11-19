package com.unicatolica.gymtracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.unicatolica.gymtracker.api.ApiClient
import com.unicatolica.gymtracker.data.Routine
import com.unicatolica.gymtracker.data.RoutinesResponse
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter

class TrainingHistoryActivity : AppCompatActivity() {

    // Rutinas cargadas
    private var loadedRoutines: List<Routine> = emptyList()

    // Selector de archivo (CSV o PDF)
    private val createFileLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri: Uri? ->
            if (uri != null && tempExportType != null) {
                when (tempExportType) {
                    "csv" -> exportCSVToUri(uri)
                    "pdf" -> exportPDFToUri(uri)
                }
            }
        }

    private var tempExportType: String? = null

    // Views
    private lateinit var llRoutinesContainer: LinearLayout
    private lateinit var btnSeeMore: Button
    private lateinit var btnBack: ImageView
    private lateinit var btnExport: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training_history)

        // Inicializar views
        llRoutinesContainer = findViewById(R.id.llRoutinesContainer)
        btnSeeMore = findViewById(R.id.btnSeeMore)
        btnBack = findViewById(R.id.btnBack)
        btnExport = findViewById(R.id.btnExport)

        // Listener para exportar
        btnExport.setOnClickListener {
            if (loadedRoutines.isEmpty()) {
                Toast.makeText(this, "No hay rutinas para exportar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val items = arrayOf("Exportar en CSV", "Exportar en PDF")

            android.app.AlertDialog.Builder(this)
                .setTitle("Seleccionar formato")
                .setItems(items) { _, which ->
                    if (which == 0) {
                        tempExportType = "csv"
                        createFileLauncher.launch("entrenamientos.csv")
                    } else {
                        tempExportType = "pdf"
                        createFileLauncher.launch("entrenamientos.pdf")
                    }
                }
                .show()
        }

        // Acción botón "Ver más"
        btnSeeMore.setOnClickListener {
            Toast.makeText(this, "Cargando más rutinas...", Toast.LENGTH_SHORT).show()
        }

        // Botón de retroceso
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Cargar rutinas reales
        loadRoutinesFromBackend()
    }

    // ----------------------------
    // CARGAR RUTINAS DESDE BACKEND
    // ----------------------------
    private fun loadRoutinesFromBackend() {
        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = prefs.getString("userId", null)

        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getRoutines(userId)
                if (response.isSuccessful) {
                    val responseBody = response.body()

                    if (responseBody != null && responseBody.success) {
                        loadedRoutines = responseBody.routines
                        displayRoutines(loadedRoutines)
                    } else {
                        Toast.makeText(
                            this@TrainingHistoryActivity,
                            responseBody?.message ?: "Error desconocido",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@TrainingHistoryActivity,
                        "Error HTTP ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@TrainingHistoryActivity,
                    "Error de red: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ----------------------------
    // EXPORTAR CSV
    // ----------------------------
    private fun exportCSVToUri(uri: Uri) {
        try {
            val csv = StringBuilder()
            csv.append("Nombre,Fecha,Duración\n")

            for (r in loadedRoutines) {
                csv.append("${r.name},${r.date},${r.duration ?: "N/A"}\n")
            }

            contentResolver.openOutputStream(uri)?.use { out ->
                out.write(csv.toString().toByteArray())
            }

            Toast.makeText(this, "CSV exportado exitosamente", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error al exportar CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ----------------------------
    // EXPORTAR PDF
    // ----------------------------
    private fun exportPDFToUri(uri: Uri) {
        try {
            val outputStream = contentResolver.openOutputStream(uri)
            val document = Document()
            PdfWriter.getInstance(document, outputStream)
            document.open()

            document.add(Paragraph("Historial de Entrenamientos\n\n"))

            for (routine in loadedRoutines) {
                document.add(Paragraph("Nombre: ${routine.name}"))
                document.add(Paragraph("Fecha: ${routine.date}"))
                document.add(Paragraph("Duración: ${routine.duration ?: "N/A"}"))
                document.add(Paragraph("--------------------------------------\n"))
            }

            document.close()

            Toast.makeText(this, "PDF exportado exitosamente", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error al exportar PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ----------------------------
    // MOSTRAR RUTINAS EN UI
    // ----------------------------
    private fun displayRoutines(routines: List<Routine>) {
        val inflater = LayoutInflater.from(this) // ✅ Declara 'inflater' aquí

        llRoutinesContainer.removeAllViews() // ✅ Limpia antes de agregar nuevas vistas

        if (routines.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "No tienes rutinas guardadas"
                setPadding(32, 32, 32, 32)
                textSize = 16f
            }
            llRoutinesContainer.addView(emptyView)
            return
        }

        for (routine in routines) {
            val view = inflater.inflate(R.layout.item_routine, llRoutinesContainer, false)

            view.findViewById<TextView>(R.id.tvRoutineName).text = routine.name
            view.findViewById<TextView>(R.id.tvDuration).text = "Duración: ${routine.duration ?: "N/A"}"
            view.findViewById<TextView>(R.id.tvDate).text = "Fecha: ${routine.date ?: "N/A"}"

            // 🔥 Evento: abrir pantalla para editar
// En TrainingHistoryActivity.kt
            view.setOnClickListener {
                val intent = Intent(this, EditRoutineActivity::class.java)
                intent.putExtra("routineId", routine.id) // <-- Aquí se pasa el ID
                startActivity(intent)
            }

            llRoutinesContainer.addView(view)
        }
    }
}