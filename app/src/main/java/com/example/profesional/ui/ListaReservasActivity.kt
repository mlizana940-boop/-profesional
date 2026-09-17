package com.example.profesional.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.profesional.R
import com.example.profesional.ReservaAdapter
import com.example.profesional.model.EstadoReserva
import com.example.profesional.model.Reserva
import com.example.profesional.repository.MSG_SIN_CONEXION
import com.example.profesional.repository.ReservaRepository
import com.example.profesional.util.Formato
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ListenerRegistration
import java.time.LocalDate

/** P-PR02: Listado y filtrado de reservas (fecha + estado). */
class ListaReservasActivity : AppCompatActivity() {

    private lateinit var repository: ReservaRepository
    private lateinit var adapter: ReservaAdapter
    private var listenerReservas: ListenerRegistration? = null

    private lateinit var btnFecha: MaterialButton
    private lateinit var tvVacio: TextView
    private lateinit var progress: ProgressBar

    private var fechaFiltro: LocalDate = LocalDate.now()
    private var estadoFiltro: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lista_reservas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repository = ReservaRepository()
        btnFecha = findViewById(R.id.btnFecha)
        tvVacio = findViewById(R.id.tvVacio)
        progress = findViewById(R.id.progress)

        configurarRecyclerView()
        configurarFecha()
        configurarSpinnerEstado()

        actualizarEtiquetaFecha()
        escucharReservas()
    }

    private fun configurarRecyclerView() {
        adapter = ReservaAdapter { reserva ->
            abrirDetalle(reserva)
        }
        findViewById<RecyclerView>(R.id.rvReservas).apply {
            layoutManager = LinearLayoutManager(this@ListaReservasActivity)
            adapter = this@ListaReservasActivity.adapter
        }
    }

    private fun configurarFecha() {
        btnFecha.setOnClickListener {
            val now = LocalDate.now()
            DatePickerDialog(
                this,
                { _, anio, mes, dia ->
                    fechaFiltro = LocalDate.of(anio, mes + 1, dia)
                    actualizarEtiquetaFecha()
                    escucharReservas()
                },
                fechaFiltro.year,
                fechaFiltro.monthValue - 1,
                fechaFiltro.dayOfMonth
            ).apply {
                datePicker.minDate = now.toEpochDay() * 86_400_000L
                show()
            }
        }
    }

    private fun configurarSpinnerEstado() {
        val opciones = listOf("Todos los estados") + EstadoReserva.TODOS
        val spinner = findViewById<androidx.appcompat.widget.AppCompatSpinner>(R.id.spEstado)
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opciones
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                estadoFiltro =
                    if (position == 0) null else EstadoReserva.TODOS[position - 1]
                escucharReservas()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    /** P-PR02 + RF-11/RF-12: escucha en tiempo real con filtro por fecha y estado. */
    private fun escucharReservas() {
        listenerReservas?.remove()
        progress.visibility = View.VISIBLE

        listenerReservas = repository.escucharReservas(
            fecha = fechaFiltro.toString(),
            estado = estadoFiltro,
            onResult = { lista ->
                progress.visibility = View.GONE
                adapter.actualizar(lista)
                tvVacio.visibility =
                    if (lista.isEmpty()) View.VISIBLE else View.GONE
            },
            onError = { mensaje ->
                progress.visibility = View.GONE
                tvVacio.visibility = View.VISIBLE
                tvVacio.text = if (mensaje == MSG_SIN_CONEXION) {
                    "Sin conexión. Verifique su red"
                } else {
                    mensaje
                }
                Snackbar.make(findViewById(R.id.main), mensaje, Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun actualizarEtiquetaFecha() {
        btnFecha.text = "Fecha: ${Formato.fechaCorta(fechaFiltro.toString())}"
    }

    private fun abrirDetalle(reserva: Reserva) {
        val intent = Intent(this, DetalleReservaActivity::class.java)
        intent.putExtra(EXTRA_RESERVA_ID, reserva.id)
        startActivity(intent)
    }

    override fun onDestroy() {
        // RNF-03: se remueve el listener para evitar memory leaks.
        listenerReservas?.remove()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_RESERVA_ID = "extra_reserva_id"
    }
}