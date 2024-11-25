package com.example.lumvida.ui.Reportes.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.lumvida.network.model.OverpassResponse
import com.example.lumvida.network.model.OverpassService
import com.example.lumvida.R
import com.example.lumvida.network.RetrofitClient
import com.example.lumvida.network.model.NominatimResponse
import com.example.lumvida.ui.Categorias.ui.CategoriasViewModel
import com.example.lumvida.ui.CrearReporte.ui.CrearReporteViewModel
import com.example.lumvida.ui.components.NetworkStatusMessage
import com.example.lumvida.ui.theme.Primary
import com.example.lumvida.ui.theme.TextPrimary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale

var org.osmdroid.views.overlay.Marker.id: String
    get() = this.getRelatedObject() as? String ?: ""
    set(value) { this.setRelatedObject(value) }

var org.osmdroid.views.overlay.Polygon.id: String
    get() = this.getRelatedObject() as? String ?: ""
    set(value) { this.setRelatedObject(value) }

var org.osmdroid.views.overlay.Polyline.id: String
    get() = this.getRelatedObject() as? String ?: ""
    set(value) { this.setRelatedObject(value) }

// Al inicio del archivo, define las constantes para ambas ciudades
private val CHETUMAL_CONFIG = CityConfig(
    bounds = BoundingBox(
        18.55, // North
        -88.25, // East
        18.45, // South
        -88.35  // West
    ),
    center = GeoPoint(18.5001889, -88.296146)
)

private val CANCUN_CONFIG = CityConfig(
    bounds = BoundingBox(
        21.17, // North
        -86.80, // East
        21.12, // South
        -86.87  // West
    ),
    center = GeoPoint(21.1483, -86.8339)
)

// Clase para la configuración de cada ciudad
data class CityConfig(
    val bounds: BoundingBox,
    val center: GeoPoint,
    val defaultZoom: Double = 15.0
)

// Función auxiliar para calcular distancia
private fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val radioTierra = 6371 // Radio de la Tierra en kilómetros
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return radioTierra * c
}

//Zooms paara cada reporte individualmente
private const val ZOOM_LEVEL_1 = 14.0  // Azul
private const val ZOOM_LEVEL_2 = 16.0  // Verde
private const val ZOOM_LEVEL_3 = 18.0  // Amarillo
private const val ZOOM_LEVEL_4 = 20.0  // Rojo
private const val CIRCLE_RADIUS = 0.00015 // Aproximadamente 15 metros

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onLocationSelected: (GeoPoint, String) -> Unit,
    onDismiss: () -> Unit,
    initialLocation: GeoPoint,
    categoriasViewModel: CategoriasViewModel,
    onNavigate: (String) -> Unit = {},
    viewModel: CrearReporteViewModel,
    isViewMode: Boolean = false
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var showCategoriesMenu by remember { mutableStateOf(false) }
    var searchSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var showOptionsMenu by remember { mutableStateOf(false) }
    var isConnected by remember { mutableStateOf(RetrofitClient.isOnline(context)) }
    var wasDisconnected by remember { mutableStateOf(false) }

    // Efecto para monitorear la conexión
    LaunchedEffect(Unit) {
        while(true) {
            val connectionState = RetrofitClient.isOnline(context)
            if (isConnected != connectionState) {
                if (!connectionState) {
                    // Se perdió la conexión
                    wasDisconnected = true
                }
                isConnected = connectionState
                if (isConnected && wasDisconnected) {
                    // Recargar datos cuando se recupere la conexión
                    viewModel.obtenerReportes()
                }
            }
            delay(1000)
        }
    }

    // Efecto para cargar los reportes
    LaunchedEffect(Unit) {
        viewModel.obtenerReportes()
        viewModel.iniciarEscuchaReportes()
    }

    fun getMarkerIcon(categoria: String): Int {
        return when (categoria.lowercase()) {
            "bacheo" -> R.drawable.ic_marker_warning
            "alumbrado público" -> R.drawable.ic_marker_lightbulb
            "drenajes obstruidos" -> R.drawable.ic_marker_water
            "basura acumulada" -> R.drawable.ic_marker_delete
            else -> R.drawable.ic_marker_default // icono por defecto
        }
    }

    fun createSimpleStreetOverlay(mapView: MapView, result: NominatimResponse) {
        val point = GeoPoint(result.lat.toDouble(), result.lon.toDouble())

        // Limpiar marcadores antiguos
        mapView.overlays.removeAll { it is Marker && it.id == "search_marker" }

        // Crear nuevo marcador
        val marker = Marker(mapView).apply {
            id = "search_marker"
            position = point
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = result.displayName

            val icon = ContextCompat.getDrawable(mapView.context, R.drawable.ic_location_marker)
            icon?.setTint(android.graphics.Color.RED)
            setIcon(icon)
        }

        // Añadir marcador y actualizar mapa
        mapView.overlays.add(marker)
        mapView.controller.animateTo(point)
        mapView.invalidate()
    }

    fun createNeighborhoodCircle(center: GeoPoint): ArrayList<GeoPoint> {
        val points = ArrayList<GeoPoint>()
        val radius = 0.003 // Aproximadamente 300 metros

        for (i in 0..360 step 10) {
            val radian = Math.toRadians(i.toDouble())
            val lat = center.latitude + radius * Math.cos(radian)
            val lon = center.longitude + radius * Math.sin(radian)
            points.add(GeoPoint(lat, lon))
        }
        return points
    }

    // Añadir esta función para crear los diferentes tipos de overlays
    fun createSearchOverlay(mapView: MapView, result: NominatimResponse) {
        scope.launch {
            try {
                // Limpiar overlays anteriores
                mapView.overlays.removeAll { overlay ->
                    when (overlay) {
                        is org.osmdroid.views.overlay.Marker ->
                            overlay.id in listOf("search_overlay", "search_overlay_start", "search_overlay_end")
                        is org.osmdroid.views.overlay.Polyline ->
                            overlay.id == "search_overlay"
                        is org.osmdroid.views.overlay.Polygon ->
                            overlay.id == "search_overlay"
                        else -> false
                    }
                }
                mapView.invalidate()

                // Obtener las coordenadas del resultado
                val lat = result.lat.toDouble()
                val lon = result.lon.toDouble()

                // Determinar en qué ciudad está el punto
                val config = when {
                    CHETUMAL_CONFIG.bounds.contains(lat, lon) -> CHETUMAL_CONFIG
                    CANCUN_CONFIG.bounds.contains(lat, lon) -> CANCUN_CONFIG
                    else -> null
                }

                // Solo mostrar el marcador si está dentro de los límites de alguna ciudad
                if (config != null) {
                    when (result.type.lowercase()) {
                        "highway", "road", "path", "street", "pedestrian", "service", "residential" -> {
                            val query = OverpassService.buildStreetQuery(
                                lat,
                                lon,
                                result.displayName.split(",")[0] // Tomar solo el nombre de la calle
                            )

                            val response = RetrofitClient.apiService.getStreetGeometry(query)
                            val overpassResponse = Gson().fromJson(
                                response.string(),
                                OverpassResponse::class.java
                            )

                            val streetPoints = OverpassService.parseGeometryFromResponse(overpassResponse)

                            if (streetPoints.isNotEmpty()) {
                                val isInBounds = streetPoints.any { point ->
                                    config.bounds.contains(point)
                                }

                                if (!isInBounds) {
                                    // Si no está en los límites, centrar en el centro de la ciudad
                                    mapView.controller.animateTo(config.center)
                                    mapView.controller.setZoom(17.0)
                                } else {
                                    val polyline = org.osmdroid.views.overlay.Polyline().apply {
                                        id = "search_overlay"
                                        outlinePaint.color = android.graphics.Color.argb(180, 255, 0, 0)
                                        outlinePaint.strokeWidth = 12f
                                        setPoints(streetPoints)
                                    }
                                    mapView.overlays.add(polyline)

                                    // Calcular los límites manualmente
                                    var minLat = Double.MAX_VALUE
                                    var maxLat = Double.MIN_VALUE
                                    var minLon = Double.MAX_VALUE
                                    var maxLon = Double.MIN_VALUE

                                    streetPoints.forEach { point ->
                                        minLat = minOf(minLat, point.latitude)
                                        maxLat = maxOf(maxLat, point.latitude)
                                        minLon = minOf(minLon, point.longitude)
                                        maxLon = maxOf(maxLon, point.longitude)
                                    }

                                    // Calcular la longitud de la calle
                                    val latSpan = maxLat - minLat
                                    val lonSpan = maxLon - minLon

                                    // Calcular el margen y zoom basado en la longitud de la calle
                                    val marginFactor = when {
                                        latSpan < 0.0005 || lonSpan < 0.0005 -> 0.5  // Calles muy cortas
                                        latSpan < 0.001 || lonSpan < 0.001 -> 0.3    // Calles cortas
                                        latSpan < 0.002 || lonSpan < 0.002 -> 0.2    // Calles medianas
                                        else -> 0.1                                   // Calles largas
                                    }

                                    val latMargin = latSpan * marginFactor
                                    val lonMargin = lonSpan * marginFactor

                                    // Crear bounds limitado al área de la ciudad
                                    val bounds = BoundingBox(
                                        minOf(maxLat + latMargin, config.bounds.latNorth),  // north
                                        minOf(maxLon + lonMargin, config.bounds.lonEast),   // east
                                        maxOf(minLat - latMargin, config.bounds.latSouth),  // south
                                        maxOf(minLon - lonMargin, config.bounds.lonWest)    // west
                                    )

                                    // Animar el mapa a los nuevos límites
                                    mapView.zoomToBoundingBox(bounds, true, 50)

                                    // Ajustar el zoom según la longitud de la calle
                                    val currentZoom = mapView.zoomLevel
                                    val targetZoom = when {
                                        latSpan < 0.0005 || lonSpan < 0.0005 -> 19.0  // Calles muy cortas
                                        latSpan < 0.001 || lonSpan < 0.001 -> 18.5    // Calles cortas
                                        latSpan < 0.002 || lonSpan < 0.002 -> 18.0    // Calles medianas
                                        latSpan < 0.004 || lonSpan < 0.004 -> 17.5    // Calles largas
                                        else -> 17.0                                   // Calles muy largas
                                    }

                                    if (currentZoom < targetZoom) {
                                        mapView.controller.setZoom(targetZoom)
                                        mapView.controller.animateTo(GeoPoint(
                                            (maxLat + minLat) / 2,
                                            (maxLon + minLon) / 2
                                        ))
                                    }

                                    // Agregar marcadores en los extremos de la calle
                                    val startMarker = Marker(mapView).apply {
                                        id = "search_overlay_start"
                                        position = streetPoints.first()
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        icon = ContextCompat.getDrawable(mapView.context, R.drawable.ic_location_marker)?.apply {
                                            setTint(android.graphics.Color.rgb(255, 0, 0))
                                        }
                                        title = "Inicio de ${result.displayName.split(",")[0]}"
                                    }

                                    val endMarker = Marker(mapView).apply {
                                        id = "search_overlay_end"
                                        position = streetPoints.last()
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        icon = ContextCompat.getDrawable(mapView.context, R.drawable.ic_location_marker)?.apply {
                                            setTint(android.graphics.Color.rgb(255, 0, 0))
                                        }
                                        title = "Fin de ${result.displayName.split(",")[0]}"
                                    }

                                    mapView.overlays.add(startMarker)
                                    mapView.overlays.add(endMarker)
                                }
                            } else {
                                // Si no se encontraron puntos, usar el marcador simple
                                createSimpleStreetOverlay(mapView, result)
                            }
                        }

                        "house", "building", "amenity", "shop", "leisure" -> {
                            val marker = Marker(mapView).apply {
                                id = "search_overlay"
                                position = GeoPoint(lat, lon)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                icon = ContextCompat.getDrawable(mapView.context, R.drawable.ic_location_marker)?.apply {
                                    setTint(android.graphics.Color.rgb(255, 0, 0))
                                }
                                title = result.displayName
                            }
                            mapView.overlays.add(marker)
                        }

                        "suburb", "neighbourhood", "residential" -> {
                            val polygon = org.osmdroid.views.overlay.Polygon().apply {
                                id = "search_overlay"
                                points = createNeighborhoodCircle(GeoPoint(lat, lon))
                                fillColor = android.graphics.Color.argb(30, 0, 255, 0)
                                strokeColor = android.graphics.Color.rgb(0, 255, 0)
                                strokeWidth = 3f
                            }
                            mapView.overlays.add(polygon)
                        }

                        else -> {
                            val marker = Marker(mapView).apply {
                                id = "search_overlay"
                                position = GeoPoint(lat, lon)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                icon = ContextCompat.getDrawable(mapView.context, R.drawable.ic_location_marker)
                                title = result.displayName
                            }
                            mapView.overlays.add(marker)
                        }
                    }
                }
                mapView.invalidate()
            } catch (e: Exception) {
                Log.e("MapScreen", "Error al obtener geometría de la calle", e)
                // Si algo falla, usar el marcador simple como respaldo
                val lat = result.lat.toDouble()
                val lon = result.lon.toDouble()
                if (CHETUMAL_CONFIG.bounds.contains(lat, lon) ||
                    CANCUN_CONFIG.bounds.contains(lat, lon)) {
                    createSimpleStreetOverlay(mapView, result)
                }
            }
        }
    }

    // Función auxiliar para crear puntos alrededor de una calle
    fun createStreetHighlight(center: GeoPoint): ArrayList<GeoPoint> {
        val points = ArrayList<GeoPoint>()
        val streetWidth = 0.0003 // Aproximadamente 30 metros
        val streetLength = 0.001 // Aproximadamente 100 metros

        points.add(GeoPoint(center.latitude - streetWidth, center.longitude - streetLength))
        points.add(GeoPoint(center.latitude + streetWidth, center.longitude - streetLength))
        points.add(GeoPoint(center.latitude + streetWidth, center.longitude + streetLength))
        points.add(GeoPoint(center.latitude - streetWidth, center.longitude + streetLength))

        return points
    }

    fun centerMapOnReports(mapView: MapView?, reports: List<CrearReporteViewModel.ReporteMap>) {
        if (reports.isEmpty() || mapView == null) return

        // Coordenadas fijas del centro de Chetumal
        val chetumalCenter = GeoPoint(18.5001889, -88.296146)

        // Establecer zoom y centro específicos
        mapView.controller.apply {
            animateTo(chetumalCenter)
            setZoom(15) // Zoom fijo para ver Chetumal
        }
    }

    // Función para formatear la fecha (añade esto fuera del MapScreen, al inicio del archivo)
    fun formatearFecha(timestamp: com.google.firebase.Timestamp): String {
        val fecha = timestamp.toDate()
        val formato = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "MX"))
        return formato.format(fecha)
    }

    // Función corregida para crear los puntos del círculo
    fun createCirclePoints(center: GeoPoint, radiusMeters: Double): ArrayList<GeoPoint> {
        val points = ArrayList<GeoPoint>()
        val earthRadius = 6371000.0 // Radio de la Tierra en metros
        val radiusInDegrees = (radiusMeters / earthRadius) * (180.0 / Math.PI)

        for (i in 0..360) {
            val angle = Math.toRadians(i.toDouble())
            val lat = center.latitude + (radiusInDegrees * Math.cos(angle))
            val lon = center.longitude + (radiusInDegrees * Math.sin(angle) / Math.cos(Math.toRadians(center.latitude)))
            points.add(GeoPoint(lat, lon))
        }
        return points
    }

    // En el LaunchedEffect, reemplaza la parte del círculo y el zoom listener con esto:
    LaunchedEffect(viewModel.filteredReportes, viewModel.selectedCategory) {
        mapView?.let { map ->
            try {
                map.overlays.removeAll { it is Marker || it is org.osmdroid.views.overlay.Polygon }
                val reportesToShow = viewModel.filteredReportes

                // Lista para mantener referencia a los círculos
                val circles = mutableListOf<org.osmdroid.views.overlay.Polygon>()

                // Añadir marcadores y círculos
                reportesToShow.forEach { reporte ->
                    // Crear el círculo
                    val circle = org.osmdroid.views.overlay.Polygon().apply {
                        points = createCirclePoints(
                            GeoPoint(reporte.latitud, reporte.longitud),
                            15.0 // 15 metros de radio
                        )

                        // Configuración inicial del círculo
                        strokeWidth = 2f
                        fillColor = android.graphics.Color.argb(50, 0, 0, 255) // Azul inicial
                        strokeColor = android.graphics.Color.BLUE
                    }

                    circles.add(circle)
                    map.overlays.add(circle)

                    // Mantener el código existente del marcador
                    val marker = Marker(map).apply {
                        position = GeoPoint(reporte.latitud, reporte.longitud)
                        relatedObject = reporte
                        infoWindow = CustomInfoWindow(map)

                        val icon = ContextCompat.getDrawable(context, getMarkerIcon(reporte.categoria))
                        icon?.setTint(
                            when (reporte.categoria.lowercase()) {
                                "bacheo" -> android.graphics.Color.RED
                                "alumbrado público" -> android.graphics.Color.rgb(255, 165, 0)
                                "drenajes obstruidos" -> android.graphics.Color.BLUE
                                "basura acumulada" -> android.graphics.Color.GREEN
                                else -> android.graphics.Color.GRAY
                            }
                        )
                        setIcon(icon)

                        setOnMarkerClickListener { clickedMarker, mapView ->
                            try {
                                InfoWindow.closeAllInfoWindowsOn(mapView)
                                clickedMarker.showInfoWindow()
                                mapView.controller.animateTo(clickedMarker.position)
                                mapView.controller.setZoom(21.0)
                                true
                            } catch (e: Exception) {
                                Log.e("MapScreen", "Error al mostrar InfoWindow", e)
                                false
                            }
                        }
                    }
                    map.overlays.add(marker)
                }

                // Observar cambios de zoom
                var lastZoomLevel = map.zoomLevelDouble
                map.setMapListener(object : MapListener {
                    override fun onScroll(event: ScrollEvent?): Boolean {
                        return true
                    }

                    override fun onZoom(event: ZoomEvent?): Boolean {
                        val currentZoom = map.zoomLevelDouble
                        if (currentZoom != lastZoomLevel) {
                            lastZoomLevel = currentZoom
                            circles.forEach { circle ->
                                circle.fillColor = when {
                                    currentZoom >= ZOOM_LEVEL_4 -> android.graphics.Color.argb(50, 255, 0, 0)    // Rojo
                                    currentZoom >= ZOOM_LEVEL_3 -> android.graphics.Color.argb(50, 255, 255, 0)  // Amarillo
                                    currentZoom >= ZOOM_LEVEL_2 -> android.graphics.Color.argb(50, 0, 255, 0)    // Verde
                                    else -> android.graphics.Color.argb(50, 0, 0, 255)                           // Azul
                                }
                                circle.strokeColor = when {
                                    currentZoom >= ZOOM_LEVEL_4 -> android.graphics.Color.RED
                                    currentZoom >= ZOOM_LEVEL_3 -> android.graphics.Color.YELLOW
                                    currentZoom >= ZOOM_LEVEL_2 -> android.graphics.Color.GREEN
                                    else -> android.graphics.Color.BLUE
                                }
                            }
                            map.invalidate()
                        }
                        return true
                    }
                })

                map.invalidate()
            } catch (e: Exception) {
                Log.e("MapScreen", "Error al actualizar marcadores", e)
            }
        }
    }

    // Función para buscar sugerencias usando Nominatim
    fun getSuggestions(query: String) {
        if (query.length >= 3) {
            scope.launch {
                try {
                    isSearching = true

                    // Obtener la ubicación actual
                    getCurrentLocation(context)?.let { location ->
                        val ciudad = when {
                            CHETUMAL_CONFIG.bounds.contains(location.latitude, location.longitude) -> "Chetumal"
                            CANCUN_CONFIG.bounds.contains(location.latitude, location.longitude) -> "Cancun"
                            else -> "Chetumal" // Por defecto
                        }

                        // Realizar la búsqueda en la ciudad correspondiente
                        val results = RetrofitClient.nominatimService.searchLocation(
                            query = "$query, $ciudad, Quintana Roo"
                        )
                        searchSuggestions = results.map { it.displayName }
                        showSuggestions = searchSuggestions.isNotEmpty()
                    }
                } catch (e: Exception) {
                    Log.e("MapScreen", "Error getting suggestions", e)
                    searchSuggestions = emptyList()
                    showSuggestions = false
                } finally {
                    isSearching = false
                }
            }
        } else {
            searchSuggestions = emptyList()
            showSuggestions = false
        }
    }

    // Función para buscar ubicación usando Nominatim
    fun searchLocation(query: String) {
        scope.launch {
            var location: NominatimResponse? = null

            try {
                isSearching = true

                val results = RetrofitClient.nominatimService.searchLocation(
                    query = query,
                    limit = 1
                )

                if (results.isNotEmpty()) {
                    location = results[0]
                    val map = mapView ?: return@launch

                    // Limpiar overlays anteriores primero
                    map.overlays.removeAll { it is Marker || it is org.osmdroid.views.overlay.Polyline }

                    when (location.type.lowercase()) {
                        // Para calles y caminos
                        "highway", "road", "path", "street", "pedestrian", "service", "residential" -> {
                            val query = OverpassService.buildStreetQuery(
                                location.lat.toDouble(),
                                location.lon.toDouble(),
                                location.displayName.split(",")[0]
                            )

                            val response = RetrofitClient.apiService.getStreetGeometry(query)
                            val overpassResponse = Gson().fromJson(
                                response.string(),
                                OverpassResponse::class.java
                            )

                            val streetPoints = OverpassService.parseGeometryFromResponse(overpassResponse)

                            if (streetPoints.isNotEmpty()) {
                                // Dibujar la línea de la calle
                                val polyline = org.osmdroid.views.overlay.Polyline().apply {
                                    outlinePaint.color = android.graphics.Color.RED
                                    outlinePaint.strokeWidth = 10f
                                    setPoints(streetPoints)
                                }
                                map.overlays.add(polyline)

                                // Añadir marcadores en los extremos
                                val startMarker = Marker(map).apply {
                                    position = streetPoints.first()
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    title = "Inicio de ${location.displayName.split(",")[0]}"
                                    val icon = ContextCompat.getDrawable(context, R.drawable.ic_location_marker)
                                    //icon = ContextCompat.getDrawable(mapView.context, R.drawable.ic_location_marker)
                                    icon?.setTint(android.graphics.Color.RED)
                                    setIcon(icon)
                                }

                                val endMarker = Marker(map).apply {
                                    position = streetPoints.last()
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    title = "Fin de ${location.displayName.split(",")[0]}"
                                    val icon = ContextCompat.getDrawable(context, R.drawable.ic_location_marker)
                                    icon?.setTint(android.graphics.Color.RED)
                                    setIcon(icon)
                                }

                                map.overlays.add(startMarker)
                                map.overlays.add(endMarker)

                                // Calcular los límites para centrar la vista
                                val bounds = BoundingBox.fromGeoPoints(streetPoints)
                                map.zoomToBoundingBox(bounds, true, 100)
                                // Ajustar zoom adicional
                                map.controller.setZoom(18.0)
                            } else {
                                createSimpleStreetOverlay(map, location)
                            }
                        }

                        else -> {
                            createSimpleStreetOverlay(map, location)
                        }
                    }

                    map.invalidate()
                    showSuggestions = false
                }
            } catch (e: Exception) {
                Log.e("MapScreen", "Error searching location", e)
                val map = mapView
                if (location != null && map != null) {
                    createSimpleStreetOverlay(map, location)
                }
            } finally {
                isSearching = false
            }
        }
    }

    // Función para añadir marcadores al mapa
    fun MapView.addReportMarkers(reportes: List<CrearReporteViewModel.ReporteMap>, context: Context) {
        overlays.removeAll { it is Marker }  // Eliminar marcadores existentes

        reportes.forEach { reporte ->
            val marker = Marker(this).apply {
                position = GeoPoint(reporte.latitud, reporte.longitud)
                title = reporte.categoria
                snippet = reporte.direccion

                val icon = ContextCompat.getDrawable(context, R.drawable.ic_notifications)
                icon?.setTint(android.graphics.Color.rgb(255, 0, 0))
                setIcon(icon)

                setOnMarkerClickListener { marker, mapView ->
                    marker.showInfoWindow()
                    mapView.controller.animateTo(marker.position)
                    true
                }
            }
            overlays.add(marker)
        }
        invalidate()  // Redibujar el mapa
    }

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue = context.packageName
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                WindowInsets.systemBars.asPaddingValues() // Considera tanto la barra de estado como la de navegación
            )
    ) {
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    setBuiltInZoomControls(false)

                    // Guardar referencia del mapView
                    mapView = this

                    // Configurar ubicación actual
                    getCurrentLocation(context)?.let { location ->
                        val lat = location.latitude
                        val lon = location.longitude

                        // Determinar qué configuración usar basado en la ubicación
                        val config = when {
                            // Si está en Chetumal
                            CHETUMAL_CONFIG.bounds.contains(lat, lon) -> CHETUMAL_CONFIG
                            // Si está en Cancún
                            CANCUN_CONFIG.bounds.contains(lat, lon) -> CANCUN_CONFIG
                            // Por defecto usa Chetumal
                            else -> CHETUMAL_CONFIG
                        }

                        // Aplicar límites
                        setScrollableAreaLimitLatitude(
                            config.bounds.latNorth,
                            config.bounds.latSouth,
                            0
                        )
                        setScrollableAreaLimitLongitude(
                            config.bounds.lonWest,
                            config.bounds.lonEast,
                            0
                        )

                        // Configurar zoom
                        maxZoomLevel = ZOOM_LEVEL_4
                        minZoomLevel = ZOOM_LEVEL_1

                        // Centrar el mapa
                        if (config.bounds.contains(lat, lon)) {
                            controller.setCenter(GeoPoint(lat, lon))
                        } else {
                            controller.setCenter(config.center)
                        }
                        controller.setZoom(config.defaultZoom)

                        // Añadir overlay de ubicación
                        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                        locationOverlay.enableMyLocation()
                        overlays.add(locationOverlay)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Barra de búsqueda mejorada
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { newQuery ->
                    searchQuery = newQuery
                    getSuggestions(newQuery)
                },
                onSearch = { searchLocation(searchQuery) },
                isSearching = isSearching,
                mapView = mapView,
                showCategoriesMenu = showCategoriesMenu,
                onShowCategoriesMenu = { showCategoriesMenu = it },
                viewModel = viewModel,
                categoriasViewModel = categoriasViewModel,
                onCenterMap = { map, reports -> centerMapOnReports(map, reports) },
                modifier = Modifier.fillMaxWidth(),
                showSuggestions = showSuggestions,
                onShowSuggestionsChange = { showSuggestions = it }
            )

            // Lista de sugerencias con scroll
            if (showSuggestions) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 60.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        items(searchSuggestions) { suggestion ->
                            Column {
                                Text(
                                    text = suggestion,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchQuery = suggestion
                                            showSuggestions = false
                                            searchLocation(suggestion)
                                        }
                                        .padding(16.dp),
                                    color = Color.Black
                                )
                                if (suggestion != searchSuggestions.last()) {
                                    Divider(color = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Controles de navegación (zoom y ubicación)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .width(48.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Controles de zoom
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp)),
                color = Color.Black.copy(alpha = 0.75f),
                shadowElevation = 4.dp
            ) {
                Column {
                    IconButton(
                        onClick = { mapView?.controller?.zoomIn() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Text(
                            "+",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Divider(
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { mapView?.controller?.zoomOut() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Text(
                            "-",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            // Botón de ubicación actual
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                color = Color.Black.copy(alpha = 0.75f),
                shadowElevation = 4.dp
            ) {
                IconButton(
                    onClick = {
                        getCurrentLocation(context)?.let { location ->
                            currentLocation = GeoPoint(location.latitude, location.longitude)
                            mapView?.controller?.animateTo(currentLocation)
                            mapView?.controller?.setZoom(18.0)
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_mylocation),
                        contentDescription = "Mi ubicación",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Mensaje de estado de red
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            NetworkStatusMessage(
                isConnected = isConnected,
                wasDisconnected = wasDisconnected
            )
        }
    }
    // Diálogo de permisos
    if (!locationPermissionState.status.isGranted) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Permisos necesarios") },
            text = { Text("Se necesita acceso a la ubicación para mostrar tu posición en el mapa.") },
            confirmButton = {
                Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                    Text("Conceder permisos")
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    isSearching: Boolean,
    mapView: MapView?,
    showCategoriesMenu: Boolean,
    onShowCategoriesMenu: (Boolean) -> Unit,
    viewModel: CrearReporteViewModel,
    categoriasViewModel: CategoriasViewModel,
    onCenterMap: (MapView, List<CrearReporteViewModel.ReporteMap>) -> Unit,
    modifier: Modifier = Modifier,
    showSuggestions: Boolean,
    onShowSuggestionsChange: (Boolean) -> Unit
) {
    val context = LocalContext.current // Obtener el contexto local

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Barra de búsqueda
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_search),
                    contentDescription = "Search",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(color = Color.Black),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { if (searchQuery.isNotEmpty()) onSearch() }),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (searchQuery.isEmpty()) {
                                Text(text = "Buscar ubicación", color = Color.Gray)
                            }
                            innerTextField()
                        }
                    }
                )

                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Gray
                    )
                } else if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onSearchQueryChange("")
                            mapView?.let { map ->
                                // Limpiar todos los overlays de búsqueda
                                map.overlays.removeAll { overlay ->
                                    when (overlay) {
                                        is Marker -> true
                                        is org.osmdroid.views.overlay.Polyline -> true
                                        is org.osmdroid.views.overlay.Polygon -> true
                                        else -> false
                                    }
                                }
                                // Restaurar el zoom y la posición original
                                map.controller.setZoom(15.0)
                                getCurrentLocation(context)?.let { location ->
                                    map.controller.animateTo(GeoPoint(location.latitude, location.longitude))
                                }
                                map.invalidate()
                            }
                            onShowSuggestionsChange(false)
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                            contentDescription = "Limpiar búsqueda",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Botón de Reportes
        Box {
            Surface(
                modifier = Modifier
                    .size(56.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.75f),
                shadowElevation = 4.dp
            ) {
                IconButton(onClick = { onShowCategoriesMenu(true) }) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Reportes",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = showCategoriesMenu,
                onDismissRequest = { onShowCategoriesMenu(false) },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.85f))
                    .width(200.dp),
                offset = DpOffset(x = (-150).dp, y = 0.dp)
            ) {
                // Opción "Todos los reportes"
                DropdownMenuItem(
                    onClick = {
                        onShowCategoriesMenu(false)
                        InfoWindow.closeAllInfoWindowsOn(mapView)
                        viewModel.updateSelectedCategory(null)
                        mapView?.let { map -> onCenterMap(map, viewModel.reportes) }
                    },
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Todos los reportes",
                                color = Color.White,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                )

                Divider(color = Color.White.copy(alpha = 0.2f))

                // Opciones de categorías
                categoriasViewModel.categorias.forEach { categoria ->
                    DropdownMenuItem(
                        onClick = {
                            onShowCategoriesMenu(false)
                            InfoWindow.closeAllInfoWindowsOn(mapView)
                            viewModel.updateSelectedCategory(categoria.titulo)
                            mapView?.let { map -> onCenterMap(map, viewModel.filteredReportes) }
                        },
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = categoria.icono,
                                    contentDescription = null,
                                    tint = when (categoria.titulo.lowercase()) {
                                        "bacheo" -> Color.Red
                                        "alumbrado público" -> Color(0xFFFFA500)
                                        "drenajes obstruidos" -> Color.Blue
                                        "basura acumulada" -> Color.Green
                                        else -> Color.White
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = categoria.titulo,
                                    color = Color.White,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun getCurrentLocation(context: Context): Location? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }
    return null
}

private fun obtenerDireccion(context: Context, latitude: Double, longitude: Double): String {
    return try {
        val geocoder = android.location.Geocoder(context)
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses?.isNotEmpty() == true) {
            with(addresses[0]) {
                buildString {
                    append(getAddressLine(0))
                }
            }
        } else {
            "Lat: $latitude, Lon: $longitude"
        }
    } catch (e: Exception) {
        "Lat: $latitude, Lon: $longitude"
    }
}