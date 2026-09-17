package com.example.profesional

/**
 * Configuración del proyecto Firebase.
 *
 * Completa los valores REEMPLAZAR con los datos de tu proyecto:
 *  1. Entra a la consola de Firebase: https://console.firebase.google.com
 *  2. Crea el proyecto y agrega una app Android con applicationId `com.example.profesional`.
 *  3. En *Configuración del proyecto → General → Tus apps* copia:
 *       - ID del proyecto            → PROJECT_ID
 *       - Clave de API de la app Web → API_KEY
 */
object FirebaseConfig {

    /** ID del proyecto Firebase, ej. "mi-proyecto-12345". */
    const val PROJECT_ID = "REEMPLAZAR"

    /** applicationId definido en app/build.gradle.kts. No debe cambiar. */
    const val APPLICATION_ID = "com.example.profesional"

    /** Clave de API (Web API Key) de la app en Firebase. */
    const val API_KEY = "REEMPLAZAR"

    /** true cuando el estudiante ya completó los valores reales. */
    val isConfigured: Boolean
        get() = PROJECT_ID != "REEMPLAZAR" && API_KEY != "REEMPLAZAR"
}