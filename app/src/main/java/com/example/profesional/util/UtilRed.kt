package com.example.profesional.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/** Verifica la conectividad a Internet antes de operar con Firestore. */
object UtilRed {

    fun hayInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val red = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(red) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}