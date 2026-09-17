package com.example.profesional.repository

/**
 * Resultado de una operación de escritura en Firestore.
 * Exito: operación completada. Error: mensaje legible para mostrar al usuario.
 */
sealed class ResultadoOperacion {
    object Exito : ResultadoOperacion()
    data class Error(val mensaje: String) : ResultadoOperacion()
}

// Mensajes exigidos por los errores de la pauta (ERR-01, ERR-06, ERR-04).
const val MSG_SIN_CONEXION = "Sin conexión. Intente nuevamente"
const val MSG_NO_CONFIGURADO = "Firebase no configurado. Completa FirebaseConfig.kt"
const val MSG_RESERVA_NO_ENCONTRADA = "Reserva no encontrada"