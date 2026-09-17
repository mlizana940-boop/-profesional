package com.example.profesional.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Utilidades de formato para mostrar fechas en español. */
object Formato {

    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    /** Convierte "2026-09-10" en "10/09/2026". Si falla, devuelve el texto original. */
    fun fechaCorta(fechaISO: String): String {
        return try {
            LocalDate.parse(fechaISO).format(formatter)
        } catch (_: Exception) {
            fechaISO
        }
    }
}