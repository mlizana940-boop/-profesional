package com.example.profesional.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.profesional.R
import com.example.profesional.model.EstadoReserva
import com.example.profesional.model.Reserva
import com.example.profesional.repository.MSG_SIN_CONEXION
import com.example.profesional.repository.ReservaRepository
import com.example.profesional.repository.ResultadoOperacion
import com.example.profesional.util.Formato
import com.example.profesional.util.UtilRed
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/** P-PR03: Detalle de una reserva con acciones Aceptar / Rechazar / Reprogramar. */
class DetalleReservaActivity : AppCompatActivity() {

    private lateinit var repository: ReservaRepository

    private var reservaId: String? = null
    private var reserva: Reserva? = null

    private lateinit var tvNombre: TextView
    private lateinit var tvServicio: TextView
    private lateinit var tvProfesional: TextView
    private lateinit var tvFecha: TextView
    private lateinit var tvHora: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvObservacion: TextView
    private lateinit var grupoReprogramacion: LinearLayout
    private lateinit var tvFechaNueva: TextView
    private lateinit var tvHoraNueva: TextView

    private lateinit var btnAceptar: MaterialButton
    private lateinit var btnRechazar: MaterialButton
    private lateinit var btnReprogramar: MaterialButton

    private lateinit var progress: ProgressBar
    private lateinit var contenedorDetalle: View
    private lateinit var tvNoEncontrada: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_reserva)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repository = ReservaRepository()
        reservaId = intent.getStringExtra(ListaReservasActivity.EXTRA_RESERVA_ID)

        vincularVistas()
        configurarBotones()
        cargarReserva()
    }

    private fun vincularVistas() {
        tvNombre = findViewById(R.id.tvNombre)
        tvServicio = findViewById(R.id.tvServicio)
        tvProfesional = findViewById(R.id.tvProfesional)
        tvFecha = findViewById(R.id.tvFecha)
        tvHora = findViewById(R.id.tvHora)
        tvEstado = findViewById(R.id.tvEstado)
        tvObservacion = findViewById(R.id.tvObservacion)
        grupoReprogramacion = findViewById(R.id.grupoReprogramacion)
        tvFechaNueva = findViewById(R.id.tvFechaNueva)
        tvHoraNueva = findViewById(R.id.tvHoraNueva)
        btnAceptar = findViewById(R.id.btnAceptar)
        btnRechazar = findViewById(R.id.btnRechazar)
        btnReprogramar = findViewById(R.id.btnReprogramar)
        progress = findViewById(R.id.progress)
        contenedorDetalle = findViewById(R.id.contenedorDetalle)
        tvNoEncontrada = findViewById(R.id.tvNoEncontrada)
    }

    private fun configurarBotones() {
        btnAceptar.setOnClickListener { aceptarReserva() }
        btnRechazar.setOnClickListener { mostrarDialogoRechazo() }
        btnReprogramar.setOnClickListener { mostrarSeleccionNuevaFecha() }
    }

    /** Carga el documento de la reserva; si no existe muestra ERR-04. */
    private fun cargarReserva() {
        val id = reservaId
        if (id == null) {
            mostrarNoEncontrada()
            return
        }

        progress.visibility = View.VISIBLE
        contenedorDetalle.visibility = View.GONE
        tvNoEncontrada.visibility = View.GONE

        lifecycleScope.launch {
            val datos = repository.obtenerReserva(id)
            progress.visibility = View.GONE

            if (datos == null) {
                mostrarNoEncontrada()
            } else {
                reserva = datos
                mostrarReserva(datos)
            }
        }
    }

    private fun mostrarReserva(r: Reserva) {
        tvNombre.text = r.nombreCliente
        tvServicio.text = r.servicio
        tvProfesional.text = r.profesional
        tvFecha.text = Formato.fechaCorta(r.fecha)
        tvHora.text = "${r.hora} h"
        tvEstado.text = r.estado
        tvObservacion.text = r.observacion.ifBlank { "—" }

        if (r.estado == EstadoReserva.REPROGRAMADA && r.fechaNueva != null && r.horaNueva != null) {
            grupoReprogramacion.visibility = View.VISIBLE
            tvFechaNueva.text = Formato.fechaCorta(r.fechaNueva!!)
            tvHoraNueva.text = "${r.horaNueva} h"
        } else {
            grupoReprogramacion.visibility = View.GONE
        }

        // P-PR03: acciones solo visibles si está pendiente de respuesta.
        val accionable = EstadoReserva.esAccionable(r.estado)
        btnAceptar.visibility = if (accionable) View.VISIBLE else View.GONE
        btnRechazar.visibility = if (accionable) View.VISIBLE else View.GONE
        btnReprogramar.visibility = if (accionable) View.VISIBLE else View.GONE

        contenedorDetalle.visibility = View.VISIBLE
    }

    private fun mostrarNoEncontrada() {
        tvNoEncontrada.text = getString(R.string.msg_reserva_no_encontrada)
        tvNoEncontrada.visibility = View.VISIBLE
        progress.visibility = View.GONE
        contenedorDetalle.visibility = View.GONE
    }

    // ---------- Acciones del profesional ----------

    private fun aceptarReserva() {
        val id = reserva?.id ?: return
        if (!hayRed()) return
        lifecycleScope.launch {
            btnAceptar.isEnabled = false
            val resultado = repository.aceptar(id)
            when (resultado) {
                is ResultadoOperacion.Exito -> {
                    Snackbar.make(findViewById(R.id.main), R.string.msg_aceptada, Snackbar.LENGTH_LONG).show()
                    cargarReserva()
                }
                is ResultadoOperacion.Error -> Snackbar.make(
                    findViewById(R.id.main),
                    resultado.mensaje,
                    Snackbar.LENGTH_LONG
                ).show()
            }
            btnAceptar.isEnabled = true
        }
    }

    private fun mostrarDialogoRechazo() {
        val input = android.widget.EditText(this).apply {
            hint = getString(R.string.hint_observacion_obligatoria)
            setSingleLine(false)
            minLines = 3
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.titulo_rechazar)
            .setMessage(R.string.msg_rechazo_obligatorio)
            .setView(input)
            .setPositiveButton(getString(R.string.accion_rechazar)) { _, _ ->
                val observacion = input.text.toString().trim()
                // ERR-05: el rechazo exige observación obligatoria.
                if (observacion.isEmpty()) {
                    Snackbar.make(findViewById(R.id.main), R.string.msg_observacion_obligatoria, Snackbar.LENGTH_LONG).show()
                    mostrarDialogoRechazo()
                } else {
                    rechazarReserva(observacion)
                }
            }
            .setNegativeButton(getString(R.string.accion_cancelar), null)
            .show()
    }

    private fun rechazarReserva(observacion: String) {
        val id = reserva?.id ?: return
        if (!hayRed()) return
        lifecycleScope.launch {
            btnRechazar.isEnabled = false
            when (val resultado = repository.rechazar(id, observacion)) {
                is ResultadoOperacion.Exito -> {
                    Snackbar.make(findViewById(R.id.main), R.string.msg_rechazada, Snackbar.LENGTH_LONG).show()
                    cargarReserva()
                }
                is ResultadoOperacion.Error -> Snackbar.make(
                    findViewById(R.id.main), resultado.mensaje, Snackbar.LENGTH_LONG
                ).show()
            }
            btnRechazar.isEnabled = true
        }
    }

    private fun mostrarSeleccionNuevaFecha() {
        val hoy = LocalDate.now()
        DatePickerDialog(
            this,
            { _, anio, mes, dia ->
                val nuevaFecha = LocalDate.of(anio, mes + 1, dia)
                mostrarSeleccionNuevaHora(nuevaFecha)
            },
            hoy.year,
            hoy.monthValue - 1,
            hoy.dayOfMonth
        ).apply {
            datePicker.minDate = hoy.toEpochDay() * 86_400_000L
            show()
        }
    }

    private fun mostrarSeleccionNuevaHora(fechaNueva: LocalDate) {
        val ahora = LocalTime.now()
        TimePickerDialog(
            this,
            { _, hora, minuto ->
                val horaNueva = LocalTime.of(hora, minuto)
                reprogramarReserva(
                    fechaNueva.toString(),
                    "%02d:%02d".format(hora, minuto)
                )
            },
            ahora.hour,
            ahora.minute,
            true
        ).show()
    }

    private fun reprogramarReserva(fechaNueva: String, horaNueva: String) {
        val id = reserva?.id ?: return
        if (!hayRed()) return
        lifecycleScope.launch {
            btnReprogramar.isEnabled = false
            when (val resultado = repository.reprogramar(
                id, fechaNueva, horaNueva,
                getString(R.string.msg_propuesta_horario)
            )) {
                is ResultadoOperacion.Exito -> {
                    Snackbar.make(findViewById(R.id.main), R.string.msg_reprogramada, Snackbar.LENGTH_LONG).show()
                    cargarReserva()
                }
                is ResultadoOperacion.Error -> Snackbar.make(
                    findViewById(R.id.main), resultado.mensaje, Snackbar.LENGTH_LONG
                ).show()
            }
            btnReprogramar.isEnabled = true
        }
    }

    private fun hayRed(): Boolean {
        // ERR-01: si no hay conexión se informa y no se intenta escribir.
        if (!UtilRed.hayInternet(this)) {
            Snackbar.make(findViewById(R.id.main), MSG_SIN_CONEXION, Snackbar.LENGTH_LONG).show()
            return false
        }
        return true
    }
}