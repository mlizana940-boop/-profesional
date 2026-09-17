package com.example.profesional

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

/**
 * Inicializa Firebase de forma programática (sin google-services.json)
 * usando los valores de [FirebaseConfig].
 */
class ProfesionalApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (FirebaseConfig.isConfigured) {
            val options = FirebaseOptions.Builder()
                .setApplicationId(FirebaseConfig.APPLICATION_ID)
                .setProjectId(FirebaseConfig.PROJECT_ID)
                .setApiKey(FirebaseConfig.API_KEY)
                .build()
            FirebaseApp.initializeApp(this, options)
            Log.i(TAG, "Firebase inicializado correctamente")
        } else {
            Log.w(TAG, "Firebase no configurado: completa los valores de FirebaseConfig.kt")
        }
    }

    companion object {
        private const val TAG = "ProfesionalApp"
    }
}