package com.example.lumvida.utils

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class OSMMapManager(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private lateinit var mapView: MapView
    private lateinit var cacheManager: CacheManager

    init {
        val osmConfig = Configuration.getInstance()
        val baseDir = File(context.cacheDir.absolutePath, "osmdroid")
        val tileDir = File(baseDir.absolutePath, "tiles")

        osmConfig.apply {
            osmdroidBasePath = baseDir
            osmdroidTileCache = tileDir
        }

        baseDir.mkdirs()
        tileDir.mkdirs()
    }

    fun initialize(mapView: MapView) {
        this.mapView = mapView
        this.cacheManager = CacheManager(mapView)
    }

    suspend fun downloadOfflineMap(radius: Double = 10.0) {
        if (!::mapView.isInitialized) {
            throw IllegalStateException("MapView no inicializado. Llama a initialize() primero.")
        }

        withContext(Dispatchers.Main) {
            try {
                val location = withContext(Dispatchers.IO) { getCurrentLocation() }
                val centerPoint = GeoPoint(location.latitude, location.longitude)
                val bbox = calculateBoundingBox(centerPoint, radius)

                cacheManager.downloadAreaAsync(
                    context,  // Agregamos el contexto aquí
                    bbox,
                    10,
                    16,
                    object : CacheManager.CacheManagerCallback {
                        override fun onTaskComplete() {
                            Log.d("OSMMapManager", "Descarga completada")
                        }

                        override fun onTaskFailed(errors: Int) {
                            Log.e("OSMMapManager", "Error en la descarga: $errors errores")
                        }

                        override fun updateProgress(progress: Int, currentZoomLevel: Int, zoomMin: Int, zoomMax: Int) {
                            val percentage = (progress * 100 / (zoomMax - zoomMin + 1))
                            Log.d("OSMMapManager", "Progreso: $percentage% en zoom $currentZoomLevel")
                        }

                        override fun downloadStarted() {
                            Log.d("OSMMapManager", "Iniciando descarga")
                        }

                        override fun setPossibleTilesInArea(total: Int) {
                            Log.d("OSMMapManager", "Total de tiles a descargar: $total")
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("OSMMapManager", "Error al descargar mapa", e)
                throw e
            }
        }
    }

    private suspend fun getCurrentLocation(): Location {
        return suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            continuation.resume(location)
                        } else {
                            continuation.resumeWithException(Exception("No se pudo obtener la ubicación"))
                        }
                    }
                    .addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            } catch (e: SecurityException) {
                continuation.resumeWithException(e)
            }
        }
    }

    private fun calculateBoundingBox(center: GeoPoint, radiusKm: Double): BoundingBox {
        val earthRadius = 6371.0
        val latOffset = (radiusKm / earthRadius) * (180.0 / Math.PI)
        val lonOffset = (radiusKm / (earthRadius * Math.cos(Math.toRadians(center.latitude)))) * (180.0 / Math.PI)

        return BoundingBox(
            center.latitude + latOffset,
            center.longitude + lonOffset,
            center.latitude - latOffset,
            center.longitude - lonOffset
        )
    }

    fun getCacheSize(): Long {
        return try {
            val tileCache = File(Configuration.getInstance().osmdroidTileCache.absolutePath, "cache.db")
            if (tileCache.exists()) {
                tileCache.length()
            } else {
                0L
            }
        } catch (e: Exception) {
            Log.e("OSMMapManager", "Error obteniendo tamaño del caché", e)
            0L
        }
    }

    fun clearCache() {
        try {
            val tileCache = File(Configuration.getInstance().osmdroidTileCache.absolutePath)
            if (tileCache.exists()) {
                tileCache.deleteRecursively()
                tileCache.mkdirs()
            }
        } catch (e: Exception) {
            Log.e("OSMMapManager", "Error limpiando caché", e)
        }
    }
}