package com.example.lumvida.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object NetworkUtils {
    suspend fun isNetworkAvailable(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return@withContext false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return@withContext false

            return@withContext capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            return@withContext false
        }
    }

    fun getConnectionType(context: Context): String {
        var result = "Sin conexión"
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        cm.activeNetwork?.let { network ->
            cm.getNetworkCapabilities(network)?.let { capabilities ->
                result = when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Datos móviles"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                    else -> "Desconocido"
                }
            }
        }

        return result
    }

    suspend fun executeWithConnection(
        context: Context,
        onNoConnection: suspend () -> Unit = {},
        action: suspend () -> Unit
    ) {
        if (isNetworkAvailable(context)) {
            try {
                action()
            } catch (e: IOException) {
                onNoConnection()
            }
        } else {
            onNoConnection()
        }
    }
}