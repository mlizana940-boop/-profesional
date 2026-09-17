package com.example.profesional

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.profesional.model.Reserva
import com.example.profesional.util.Formato
import com.google.android.material.chip.Chip

/** Adaptador del listado de reservas que se muestra al profesional. */
class ReservaAdapter(
    private val alHacerClick: (Reserva) -> Unit
) : RecyclerView.Adapter<ReservaAdapter.ReservaViewHolder>() {

    private val items = mutableListOf<Reserva>()

    fun actualizar(nuevaLista: List<Reserva>) {
        items.clear()
        items.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    fun estaVacia(): Boolean = items.isEmpty()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reserva, parent, false)
        return ReservaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservaViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ReservaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCliente: TextView = itemView.findViewById(R.id.tvCliente)
        private val tvServicio: TextView = itemView.findViewById(R.id.tvServicio)
        private val tvFechaHora: TextView = itemView.findViewById(R.id.tvFechaHora)
        private val chipEstado: Chip = itemView.findViewById(R.id.chipEstado)

        fun bind(reserva: Reserva) {
            tvCliente.text = reserva.nombreCliente
            tvServicio.text = reserva.servicio
            tvFechaHora.text = "${Formato.fechaCorta(reserva.fecha)} · ${reserva.hora} h"
            chipEstado.text = reserva.estado
            chipEstado.setTextColor(Color.WHITE)
            chipEstado.setChipBackgroundColorResource(colorDeEstado(reserva.estado))
            itemView.setOnClickListener { alHacerClick(reserva) }
        }
    }

    private fun colorDeEstado(estado: String): Int = when (estado) {
        "SOLICITADA" -> R.color.estado_solicitada
        "ACEPTADA" -> R.color.estado_aceptada
        "RECHAZADA" -> R.color.estado_rechazada
        "REPROGRAMADA" -> R.color.estado_reprogramada
        else -> R.color.estado_solicitada
    }
}