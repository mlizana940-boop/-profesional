package com.example.profesional.repository

import android.util.Log
import com.example.profesional.model.EstadoReserva
import com.example.profesional.model.Reserva
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Capa de acceso a Cloud Firestore (RNF-02).
 * Centraliza consultas, listeners y actualizaciones de la colección `reservas`.
 */
class ReservaRepository {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val coleccion get() = db.collection(Reserva.COLECCION)

    /**
     * Escucha en tiempo real las reservas de una fecha, con filtro de estado opcional.
     * El listener se retorna para poder removerlo en onDestroy (RNF-03).
     */
    fun escucharReservas(
        fecha: String,
        estado: String?,
        onResult: (List<Reserva>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        var consulta: Query = coleccion.whereEqualTo("fecha", fecha)
        if (estado != null) {
            consulta = consulta.whereEqualTo("estado", estado)
        }

        return consulta.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error escuchando reservas", error)
                onError(mensajeDeError(error))
                return@addSnapshotListener
            }
            val lista = snapshot?.documents
                ?.mapNotNull { doc -> Reserva.fromMap(doc.id, doc.data) }
                ?: emptyList()
            onResult(lista)
        }
    }

    /** Trae un documento puntual de una reserva. null si no existe (ERR-04). */
    suspend fun obtenerReserva(id: String): Reserva? {
        val doc = coleccion.document(id).get().await()
        return if (doc.exists()) Reserva.fromMap(doc.id, doc.data) else null
    }

    /** Acepta una reserva: estado -> ACEPTADA (RF-14). */
    suspend fun aceptar(id: String): ResultadoOperacion {
        return try {
            coleccion.document(id)
                .update("estado", EstadoReserva.ACEPTADA, "actualizadoEn", FieldValue.serverTimestamp())
                .await()
            ResultadoOperacion.Exito
        } catch (t: Throwable) {
            Log.e(TAG, "Error al aceptar reserva $id", t)
            ResultadoOperacion.Error(mensajeDeError(t))
        }
    }

    /** Rechaza una reserva: estado -> RECHAZADA con observación obligatoria (RF-15). */
    suspend fun rechazar(id: String, observacion: String): ResultadoOperacion {
        return try {
            coleccion.document(id)
                .update(
                    "estado", EstadoReserva.RECHAZADA,
                    "observacion", observacion,
                    "actualizadoEn", FieldValue.serverTimestamp()
                )
                .await()
            ResultadoOperacion.Exito
        } catch (t: Throwable) {
            Log.e(TAG, "Error al rechazar reserva $id", t)
            ResultadoOperacion.Error(mensajeDeError(t))
        }
    }

    /** Reprograma una reserva: estado -> REPROGRAMADA con nueva fecha/hora (RF-16). */
    suspend fun reprogramar(
        id: String,
        fechaNueva: String,
        horaNueva: String,
        observacion: String
    ): ResultadoOperacion {
        return try {
            coleccion.document(id)
                .update(
                    "estado", EstadoReserva.REPROGRAMADA,
                    "fechaNueva", fechaNueva,
                    "horaNueva", horaNueva,
                    "observacion", observacion,
                    "actualizadoEn", FieldValue.serverTimestamp()
                )
                .await()
            ResultadoOperacion.Exito
        } catch (t: Throwable) {
            Log.e(TAG, "Error al reprogramar reserva $id", t)
            ResultadoOperacion.Error(mensajeDeError(t))
        }
    }

    /** Traduce un error de Firestore a mensajes claros para el usuario. */
    fun mensajeDeError(t: Throwable): String {
        if (t is FirebaseFirestoreException) {
            return when (t.code) {
                Code.PERMISSION_DENIED -> "Permiso denegado: revisa las reglas de Firestore (ERR-06)"
                Code.NOT_FOUND -> MSG_RESERVA_NO_ENCONTRADA
                Code.UNAVAILABLE, Code.DEADLINE_EXCEEDED -> MSG_SIN_CONEXION
                else -> "Error de Firestore: ${t.message ?: t.code.toString()}"
            }
        }
        return MSG_SIN_CONEXION
    }

    companion object {
        private const val TAG = "ReservaRepository"
    }
}