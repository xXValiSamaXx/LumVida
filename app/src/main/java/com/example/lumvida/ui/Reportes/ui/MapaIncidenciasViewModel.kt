/*Utiliza FirebaseFirestore para obtener y escuchar los reportes en tiempo real,
almacenándolos en una lista que es filtrada según la categoría seleccionada. El ViewModel
 también maneja la localización del usuario, centrándose en la ubicación actual y ajustando
  el mapa según la proximidad a Quintana Roo. Implementa funciones para obtener sugerencias
   de búsqueda basadas en la ubicación, con un caché optimizado para mejorar el rendimiento,
    y calcula la distancia entre ubicaciones. También gestiona permisos de ubicación,
    asegurándose de que se tiene acceso antes de obtener la última posición conocida del usuario.*/

package com.example.lumvida.ui.Reportes.ui

import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.util.LruCache
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumvida.network.RetrofitClient
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class MapaIncidenciasViewModel : ViewModel() {
    var reportes by mutableStateOf<List<ReporteMap>>(emptyList())
    var filteredReportes by mutableStateOf<List<ReporteMap>>(emptyList())
    var selectedCategory by mutableStateOf<String?>(null)
    private var _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Constantes para los límites de Quintana Roo
    companion object {
        const val QUINTANA_ROO_LAT_MIN = 18.0
        const val QUINTANA_ROO_LAT_MAX = 21.6
        const val QUINTANA_ROO_LON_MIN = -89.5
        const val QUINTANA_ROO_LON_MAX = -86.7
        const val QUINTANA_ROO_CENTER_LAT = 20.5001889
        const val QUINTANA_ROO_CENTER_LON = -87.796146

        // Constantes para los niveles de zoom
        const val ZOOM_LEVEL_1 = 14.0  // Azul
        const val ZOOM_LEVEL_2 = 16.0  // Verde
        const val ZOOM_LEVEL_3 = 18.0  // Amarillo
        const val ZOOM_LEVEL_4 = 22.0  // Rojo
        const val ZOOM_LEVEL_STATE = 8.0 // Zoom para ver todo el estado
        const val ZOOM_LEVEL_LOCAL = 15.0 // Zoom para ubicación local
        const val CIRCLE_RADIUS = 0.00015 // Aproximadamente 15 metros
    }

    // Modelo de datos para los reportes
    data class ReporteMap(
        val id: String = "",
        val folio: Int = 0,
        val categoria: String = "",
        val descripcion: String = "",
        val direccion: String = "",
        val latitud: Double = 0.0,
        val longitud: Double = 0.0,
        val fecha: Timestamp = Timestamp.now(),
        val estado: String = "",
        val userId: String = ""
    )

    private val db = FirebaseFirestore.getInstance()

    private var searchJob: Job? = null
    private val _searchSuggestions = MutableStateFlow<List<String>>(emptyList())
    val searchSuggestions: StateFlow<List<String>> = _searchSuggestions

    // Añadir propiedad para almacenar la última ubicación conocida
    private var lastKnownLocation: Location? = null

    fun updateLastKnownLocation(location: Location?) {
        lastKnownLocation = location
    }

    // Reducir el delay para hacer las sugerencias más rápidas
    private val searchDebounce = 150L // reducido de 300ms a 150ms

    // Mejorar la estructura del caché
    private val suggestionsCache = LruCache<String, List<String>>(100) // Límite de 100 entradas

    fun getSuggestions(query: String) {
        if (query.length < 2) { // Reducido de 3 a 2 caracteres
            _searchSuggestions.value = emptyList()
            return
        }

        // Buscar en caché primero
        val cacheKey = "$query-${lastKnownLocation?.latitude?.toInt()}-${lastKnownLocation?.longitude?.toInt()}"
        suggestionsCache.get(cacheKey)?.let {
            _searchSuggestions.value = it
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                delay(searchDebounce)

                withContext(Dispatchers.IO) {
                    val searchQuery = lastKnownLocation?.let { location ->
                        mapOf(
                            "q" to "$query, Quintana Roo",
                            "limit" to "5",
                            "format" to "json",
                            "lat" to location.latitude.toString(),
                            "lon" to location.longitude.toString(),
                            "bounded" to "1",
                            "viewbox" to "${QUINTANA_ROO_LON_MIN},${QUINTANA_ROO_LAT_MIN},${QUINTANA_ROO_LON_MAX},${QUINTANA_ROO_LAT_MAX}",
                            "countrycodes" to "mx",
                            "addressdetails" to "1"
                        )
                    } ?: mapOf(
                        "q" to "$query, Quintana Roo",
                        "limit" to "5",
                        "format" to "json",
                        "bounded" to "1",
                        "viewbox" to "${QUINTANA_ROO_LON_MIN},${QUINTANA_ROO_LAT_MIN},${QUINTANA_ROO_LON_MAX},${QUINTANA_ROO_LAT_MAX}",
                        "countrycodes" to "mx",
                        "addressdetails" to "1"
                    )

                    val results = RetrofitClient.nominatimService.searchLocationWithParams(searchQuery)

                    val suggestions = results
                        .filter { result ->
                            val lat = result.lat.toDouble()
                            val lon = result.lon.toDouble()
                            isLocationInQuintanaRoo(lat, lon)
                        }
                        .sortedBy { result ->
                            lastKnownLocation?.let { location ->
                                calcularDistancia(
                                    location.latitude,
                                    location.longitude,
                                    result.lat.toDouble(),
                                    result.lon.toDouble()
                                )
                            } ?: 0.0
                        }
                        .map { result ->
                            // Simplificar el nombre de la ubicación para hacerlo más legible
                            result.displayName.split(",").take(2).joinToString(", ")
                        }

                    // Guardar en caché
                    suggestionsCache.put(cacheKey, suggestions)

                    withContext(Dispatchers.Main) {
                        _searchSuggestions.value = suggestions
                    }
                }
            } catch (e: Exception) {
                Log.e("MapScreen", "Error getting suggestions", e)
                _searchSuggestions.value = emptyList()
            }
        }
    }

    fun obtenerReportes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Obtener todos los reportes sin filtros iniciales
                val reportesDocRef = db.collection("reportes")
                    .get()
                    .await()

                // Mapear y filtrar en memoria
                reportes = reportesDocRef.documents.mapNotNull { doc ->
                    try {
                        // Obtener el objeto ubicacion
                        val ubicacion = doc.get("ubicacion") as? Map<*, *>
                        val lat = ubicacion?.get("latitud") as? Double
                        val lon = ubicacion?.get("longitud") as? Double

                        // Verificar que las coordenadas existan y sean válidas
                        if (lat != null && lon != null) {
                            ReporteMap(
                                id = doc.id,
                                folio = (doc.getLong("folio") ?: 0).toInt(),
                                categoria = doc.getString("categoria") ?: "",
                                descripcion = doc.getString("comentario") ?: "", // Cambiado a "comentario"
                                direccion = doc.getString("direccion") ?: "",
                                latitud = lat,
                                longitud = lon,
                                fecha = doc.getTimestamp("fecha") ?: Timestamp.now(),
                                estado = doc.getString("estado") ?: "pendiente",
                                userId = doc.getString("userId") ?: ""
                            )
                        } else {
                            Log.d("MapaViewModel", "Reporte ${doc.id} descartado por coordenadas inválidas: lat=$lat, lon=$lon")
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("MapaViewModel", "Error al mapear reporte ${doc.id}", e)
                        null
                    }
                }

                // Actualizar los reportes filtrados
                updateSelectedCategory(selectedCategory)

                Log.d("MapaViewModel", "Reportes obtenidos: ${reportes.size}")

                // Mostrar más detalles si no hay reportes
                if (reportes.isEmpty()) {
                    val totalDocs = reportesDocRef.documents.size
                    Log.d("MapaViewModel", "Total de documentos en Firestore: $totalDocs")
                    reportesDocRef.documents.forEach { doc ->
                        val ubicacion = doc.get("ubicacion") as? Map<*, *>
                        val lat = ubicacion?.get("latitud") as? Double
                        val lon = ubicacion?.get("longitud") as? Double
                        Log.d("MapaViewModel", "Documento ${doc.id} data: ubicacion=$ubicacion, lat=$lat, lon=$lon")
                    }
                }

            } catch (e: Exception) {
                Log.e("MapaViewModel", "Error al obtener reportes", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun iniciarEscuchaReportes() {
        try {
            val reportesDocRef = db.collection("reportes")

            reportesDocRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("MapaViewModel", "Error al escuchar reportes", e)
                    return@addSnapshotListener
                }

                val reportesActualizados = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        // Obtener el objeto ubicacion
                        val ubicacion = doc.get("ubicacion") as? Map<*, *>
                        val lat = ubicacion?.get("latitud") as? Double
                        val lon = ubicacion?.get("longitud") as? Double

                        // Verificar que las coordenadas existan y sean válidas
                        if (lat != null && lon != null) {
                            ReporteMap(
                                id = doc.id,
                                folio = (doc.getLong("folio") ?: 0).toInt(),
                                categoria = doc.getString("categoria") ?: "",
                                descripcion = doc.getString("comentario") ?: "", // Cambiado a "comentario"
                                direccion = doc.getString("direccion") ?: "",
                                latitud = lat,
                                longitud = lon,
                                fecha = doc.getTimestamp("fecha") ?: Timestamp.now(),
                                estado = doc.getString("estado") ?: "pendiente",
                                userId = doc.getString("userId") ?: ""
                            )
                        } else {
                            Log.d("MapaViewModel", "Reporte ${doc.id} descartado por coordenadas inválidas: lat=$lat, lon=$lon")
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("MapaViewModel", "Error al mapear reporte ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                reportes = reportesActualizados
                updateSelectedCategory(selectedCategory)

                // Log detallado
                if (reportesActualizados.isEmpty()) {
                    Log.d("MapaViewModel", "No se encontraron reportes válidos")
                    snapshot?.documents?.forEach { doc ->
                        val ubicacion = doc.get("ubicacion") as? Map<*, *>
                        val lat = ubicacion?.get("latitud") as? Double
                        val lon = ubicacion?.get("longitud") as? Double
                        Log.d("MapaViewModel", "Documento ${doc.id} data: ubicacion=$ubicacion, lat=$lat, lon=$lon")
                    }
                } else {
                    Log.d("MapaViewModel", "Reportes actualizados: ${reportesActualizados.size}")
                }
            }
        } catch (e: Exception) {
            Log.e("MapaViewModel", "Error al iniciar escucha de reportes", e)
        }
    }

    fun updateSelectedCategory(category: String?) {
        selectedCategory = category
        filteredReportes = if (category == null) {
            reportes
        } else {
            reportes.filter { it.categoria.equals(category, ignoreCase = true) }
        }
        Log.d("MapaViewModel", "Filtro actualizado. Categoría: $category, Reportes: ${filteredReportes.size}")
    }

    // Función para centrar el mapa en la ubicación del usuario
    fun centerMapOnUserLocation(context: Context, mapView: MapView?) {
        getCurrentLocation(context)?.let { location ->
            val latitude = location.latitude
            val longitude = location.longitude
            val isInQuintanaRoo = isLocationInQuintanaRoo(latitude, longitude)

            mapView?.let { map ->
                if (isInQuintanaRoo) {
                    map.controller.apply {
                        animateTo(GeoPoint(latitude, longitude))
                        setZoom(ZOOM_LEVEL_LOCAL)
                    }
                } else {
                    // Si el usuario no está en Quintana Roo, centrar en el centro del estado
                    map.controller.apply {
                        animateTo(GeoPoint(QUINTANA_ROO_CENTER_LAT, QUINTANA_ROO_CENTER_LON))
                        setZoom(ZOOM_LEVEL_STATE)
                    }
                }
            }
        }
    }

    // Función para verificar si una ubicación está dentro de Quintana Roo
    private fun isLocationInQuintanaRoo(latitude: Double, longitude: Double): Boolean {
        return latitude >= QUINTANA_ROO_LAT_MIN &&
                latitude <= QUINTANA_ROO_LAT_MAX &&
                longitude >= QUINTANA_ROO_LON_MIN &&
                longitude <= QUINTANA_ROO_LON_MAX
    }

    // Función para obtener la ubicación actual
    private fun getCurrentLocation(context: Context): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }
        return null
    }

    // Funciones auxiliares para el manejo de datos
    fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radioTierra = 6371 // Radio de la Tierra en kilómetros
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return radioTierra * c
    }
}