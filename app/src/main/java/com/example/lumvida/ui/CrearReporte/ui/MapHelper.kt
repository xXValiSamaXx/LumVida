package com.example.lumvida.ui.CrearReporte.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint

// Clase para gestionar el estado del marcador
data class MarkerState(
    val position: GeoPoint,
    val isDraggable: Boolean = true
)

// Clase para gestionar el estado del mapa
data class MapState(
    val isLocationSelectionMode: Boolean = false,
    val currentZoom: Double = 16.0,
    val minZoom: Double = 14.0,
    val maxZoom: Double = 19.0
)

// Clase para coordenadas
data class Coordinates(
    val latitude: Double,
    val longitude: Double
) {
    fun toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)
}

// Clase para gestionar los límites del mapa
object MapBounds {
    val QUINTANA_ROO_BOUNDS = BoundingBox(
        21.6043, // North latitude
        -86.7101, // East longitude
        17.8912, // South latitude
        -89.2967  // West longitude
    )
}

// Clase para gestionar la geocodificación
class GeocodingManager(private val context: Context) {
    private val geocoder = Geocoder(context)

    fun getAddress(latitude: Double, longitude: Double): String {
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                formatAddress(addresses[0])
            } else {
                formatCoordinates(latitude, longitude)
            }
        } catch (e: Exception) {
            formatCoordinates(latitude, longitude)
        }
    }

    private fun formatAddress(address: Address): String {
        return buildString {
            address.thoroughfare?.let { append(it) }
            address.subThoroughfare?.let { append(" ").append(it) }
            address.subLocality?.let { append(", ").append(it) }
            append(", ")
            append(address.locality ?: "Chetumal")
            append(", ")
            append(address.adminArea ?: "Quintana Roo")
        }
    }

    private fun formatCoordinates(latitude: Double, longitude: Double): String {
        return "Ubicación actual (${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)})"
    }
}

// Clase para gestionar la configuración del mapa
class MapConfiguration(private val context: Context) {
    fun configure() {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue = context.packageName
            osmdroidBasePath = context.getExternalFilesDir(null)
            osmdroidTileCache = context.getExternalFilesDir("tiles")
            expirationOverrideDuration = 7 * 24 * 60 * 60 * 1000L // 7 días
            tileFileSystemCacheMaxBytes = 200L * 1024 * 1024 // 200MB
            tileDownloadThreads = 4
            tileFileSystemThreads = 4
            isMapViewRecyclerFriendly = true
        }
    }
}