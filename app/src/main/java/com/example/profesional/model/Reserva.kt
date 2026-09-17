package com.example.profesional.model

object EstadoReserva {
    const val SOLICITADA = "SOLICITADA"
    const val ACEPTADA = "ACEPTADA"
    const val RECHAZADA = "RECHAZADA"
    const val REPROGRAMADA = "REPROGRAMADA"

    val TODOS: List<String> = listOf(SOLICITADA, ACEPTADA, RECHAZADA, REPROGRAMADA)

    fun esAccionable(estado: String): Boolean =
        estado == SOLICITADA || estado == REPROGRAMADA
}

data class Reserva(
    val id: String,
    val idCliente: String,
    val nombreCliente: String,
    val servicioId: String,
    val servicio: String,
    val profesionalId: String,
    val profesional: String,
    val fecha: String,
    val hora: String,
    val estado: String,
    val observacion: String = "",
    val fechaNueva: String? = null,
    val horaNueva: String? = null
) {
    companion object {
        const val COLECCION = "reservas"

        /** Convierte un documento de Firestore en una Reserva. Devuelve null si no hay datos. */
        fun fromMap(id: String, data: Map<String, Any>?): Reserva? {
            if (data == null) return null
            return Reserva(
                id = id,
                idCliente = data["idCliente"] as? String ?: "",
                nombreCliente = data["nombreCliente"] as? String ?: "",
                servicioId = data["servicioId"] as? String ?: "",
                servicio = data["servicio"] as? String ?: "",
                profesionalId = data["profesionalId"] as? String ?: "",
                profesional = data["profesional"] as? String ?: "",
                fecha = data["fecha"] as? String ?: "",
                hora = data["hora"] as? String ?: "",
                estado = data["estado"] as? String ?: "",
                observacion = data["observacion"] as? String ?: "",
                fechaNueva = data["fechaNueva"] as? String?,
                horaNueva = data["horaNueva"] as? String?
            )
        }
    }
}