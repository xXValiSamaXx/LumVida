package com.example.lumviva.ui.CrearReporte.ui

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
import com.example.lumvida.ui.CrearReporte.ui.CustomInfoWindow
import com.example.lumvida.ui.CrearReporte.ui.calculateCenter
import com.example.lumvida.ui.CrearReporte.ui.calculateCircleRadius
import com.example.lumvida.ui.CrearReporte.ui.createCirclePoints
import com.example.lumvida.ui.CrearReporte.ui.createNeighborhoodCircle
import com.example.lumvida.ui.CrearReporte.ui.createSimpleStreetOverlay
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onLocationSelected: (GeoPoint, String) -> Unit,
    onDismiss: () -> Unit,
    initialLocation: GeoPoint,
    categoriasViewModel: CategoriasViewModel,
    onNavigate: (String) -> Unit = {},
    viewModel: CrearReporteViewModel
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

                when (result.type.lowercase()) {
                    // Para calles y caminos
                    "highway", "road", "path", "street", "pedestrian", "service", "residential" -> {
                        val query = OverpassService.buildStreetQuery(
                            result.lat.toDouble(),
                            result.lon.toDouble(),
                            result.displayName.split(",")[0] // Tomar solo el nombre de la calle
                        )

                        val response = RetrofitClient.apiService.getStreetGeometry(query)
                        val overpassResponse = Gson().fromJson(
                            response.string(),
                            OverpassResponse::class.java
                        )

                        val streetPoints = OverpassService.parseGeometryFromResponse(overpassResponse)

                        if (streetPoints.isNotEmpty()) {
                            // Primero verificar si los puntos están en el área de Chetumal
                            val chetumalBounds = BoundingBox(
                                18.55, // North (máximo lat de Chetumal)
                                -88.25, // East (máximo lon de Chetumal)
                                18.45, // South (mínimo lat de Chetumal)
                                -88.35  // West (mínimo lon de Chetumal)
                            )

                            // Verificar si los puntos están dentro de Chetumal
                            val isInChetumal = streetPoints.any { point ->
                                chetumalBounds.contains(point)
                            }

                            if (!isInChetumal) {
                                // Si no está en Chetumal, centrar en Chetumal
                                val chetumalCenter = GeoPoint(18.5001889, -88.296146)
                                mapView.controller.animateTo(chetumalCenter)
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

                                // Crear bounds limitado al área de Chetumal
                                val bounds = BoundingBox(
                                    minOf(maxLat + latMargin, chetumalBounds.latNorth),  // north
                                    minOf(maxLon + lonMargin, chetumalBounds.lonEast),   // east
                                    maxOf(minLat - latMargin, chetumalBounds.latSouth),  // south
                                    maxOf(minLon - lonMargin, chetumalBounds.lonWest)    // west
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

                                // Si el zoom actual es menor que el zoom objetivo, ajustarlo
                                if (currentZoom < targetZoom) {
                                    mapView.controller.setZoom(targetZoom)
                                    // Centrar en el punto medio de la calle
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

                    // Para lugares específicos (edificios, puntos de interés, etc.)
                    "house", "building", "amenity", "shop", "leisure" -> {
                        val marker = Marker(mapView).apply {
                            id = "search_overlay"
                            position = GeoPoint(result.lat.toDouble(), result.lon.toDouble())
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            icon = ContextCompat.getDrawable(mapView.context, R.drawable.ic_location_marker)?.apply {
                                setTint(android.graphics.Color.rgb(255, 0, 255))
                            }
                            title = result.displayName
                        }
                        mapView.overlays.add(marker)
                    }

                    // Para colonias o áreas residenciales
                    "suburb", "neighbourhood", "residential" -> {
                        val polygon = org.osmdroid.views.overlay.Polygon().apply {
                            id = "search_overlay"
                            points = createNeighborhoodCircle(
                                GeoPoint(result.lat.toDouble(), result.lon.toDouble())
                            )
                            fillColor = android.graphics.Color.argb(30, 0, 255, 0)
                            strokeColor = android.graphics.Color.rgb(0, 255, 0)
                            strokeWidth = 3f
                        }
                        mapView.overlays.add(polygon)
                    }

                    // Caso por defecto para otros tipos de ubicaciones
                    else -> {
                        val marker = Marker(mapView).apply {
                            id = "search_overlay"
                            position = GeoPoint(result.lat.toDouble(), result.lon.toDouble())
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            icon = ContextCompat.getDrawable(mapView.context, R.drawable.ic_location_marker)
                            title = result.displayName
                        }
                        mapView.overlays.add(marker)
                    }
                }
                mapView.invalidate()
            } catch (e: Exception) {
                Log.e("MapScreen", "Error al obtener geometría de la calle", e)
                // Si algo falla, usar el marcador simple como respaldo
                createSimpleStreetOverlay(mapView, result)
            }
        }
    }

    // Función de respaldo para crear un marcador simple
    fun createSimpleStreetOverlay(mapView: MapView, result: NominatimResponse) {
        val marker = Marker(mapView).apply {
            id = "search_overlay"
            position = GeoPoint(result.lat.toDouble(), result.lon.toDouble())
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = ContextCompat.getDrawable(mapView.context, R.drawable.ic_location_marker)?.apply {
                setTint(android.graphics.Color.rgb(255, 0, 0))
            }
            title = result.displayName
        }
        mapView.overlays.add(marker)
        mapView.invalidate()
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

    // Función auxiliar para crear un círculo para colonias
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

    // Efecto para actualizar marcadores cuando cambien los reportes (reemplaza el LaunchedEffect existente)
    LaunchedEffect(viewModel.filteredReportes, viewModel.selectedCategory) {
        mapView?.let { map ->
            try {
                map.overlays.removeAll { it is Marker || it is org.osmdroid.views.overlay.Polygon }
                val reportesToShow = viewModel.filteredReportes
                Log.d("MapScreen", "Mostrando ${reportesToShow.size} reportes filtrados")

                // Crear círculo que engloba todos los reportes
                if (reportesToShow.isNotEmpty()) {
                    val points = reportesToShow.map { GeoPoint(it.latitud, it.longitud) }
                    val radius = points.calculateCircleRadius()
                    val centerPoint = reportesToShow.calculateCenter()

                    val circle = org.osmdroid.views.overlay.Polygon().apply {
                        this.points = centerPoint.createCirclePoints(radius)
                        fillColor = when (viewModel.selectedCategory?.lowercase()) {
                            null -> android.graphics.Color.argb(120, 135, 206, 235) // Azul suave más visible
                            "alumbrado público" -> android.graphics.Color.argb(50, 255, 165, 0) // Naranja
                            "bacheo" -> android.graphics.Color.argb(50, 255, 0, 0) // Rojo
                            "basura acumulada" -> android.graphics.Color.argb(50, 0, 255, 0) // Verde
                            "drenajes obstruidos" -> android.graphics.Color.argb(50, 0, 0, 255) // Azul
                            else -> android.graphics.Color.argb(50, 128, 128, 128)
                        }
                        strokeWidth = 5f
                        strokeColor = fillColor
                    }
                    map.overlays.add(circle)
                }

                // Añadir marcadores
                reportesToShow.forEach { reporte ->
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
                    val results = RetrofitClient.nominatimService.searchLocation(
                        query = "$query, Chetumal, Quintana Roo"
                    )
                    searchSuggestions = results.map { it.displayName }
                    showSuggestions = searchSuggestions.isNotEmpty()
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
            try {
                isSearching = true
                val results = RetrofitClient.nominatimService.searchLocation(
                    query = query,
                    limit = 1
                )
                if (results.isNotEmpty()) {
                    val location = results[0]
                    val point = GeoPoint(
                        location.lat.toDouble(),
                        location.lon.toDouble()
                    )
                    mapView?.controller?.animateTo(point)
                    mapView?.controller?.setZoom(21.0)
                    mapView?.let { createSearchOverlay(it, location) }
                    showSuggestions = false
                }
            } catch (e: Exception) {
                Log.e("MapScreen", "Error searching location", e)
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

    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(18.0)
                    controller.setCenter(initialLocation)

                    setBuiltInZoomControls(false)
                    setMultiTouchControls(true)

                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                    locationOverlay.enableMyLocation()

                    overlays.add(locationOverlay)

                    // Guardar la referencia del overlay para usarla después
                    val savedLocationOverlay = locationOverlay

                    // Centrar una vez al inicio
                    getCurrentLocation(context)?.let { location ->
                        controller.animateTo(GeoPoint(location.latitude, location.longitude))
                    }

                    // Añadir los marcadores de reportes
                    addReportMarkers(viewModel.reportes, context)

                    mapView = this
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
                modifier = Modifier.fillMaxWidth()
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

        // Barra de navegación inferior
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color.Black.copy(alpha = 0.85f)
        ) {
            Box(modifier = Modifier.fillMaxWidth()){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reports con menú desplegable
                    Box {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showCategoriesMenu = true }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_chart),
                                contentDescription = "Reports",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text("Reportes", color = Color.White)
                        }

                        // Menú desplegable de categorías
                        DropdownMenu(
                            expanded = showCategoriesMenu,
                            onDismissRequest = { showCategoriesMenu = false },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.85f))
                                .width(145.dp),
                            offset = DpOffset(x = (-30).dp, y = 0.dp)
                        ) {
                            // Opción "Todos los reportes"
                            DropdownMenuItem(
                                onClick = {
                                    showCategoriesMenu = false
                                    // Cerrar todas las ventanas de información abiertas
                                    InfoWindow.closeAllInfoWindowsOn(mapView)
                                    viewModel.updateSelectedCategory(null)
                                    centerMapOnReports(mapView, viewModel.reportes)
                                    Log.d("MapScreen", "Seleccionado: Todos los reportes")
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

                            // Dividir entre las opciones
                            Divider(color = Color.White.copy(alpha = 0.2f))

                            // Opciones de categorías
                            categoriasViewModel.categorias.forEach { categoria ->
                                DropdownMenuItem(
                                    onClick = {
                                        showCategoriesMenu = false
                                        // Cerrar todas las ventanas de información abiertas
                                        InfoWindow.closeAllInfoWindowsOn(mapView)
                                        viewModel.updateSelectedCategory(categoria.titulo)
                                        centerMapOnReports(mapView, viewModel.filteredReportes)
                                        Log.d("MapScreen", "Categoría seleccionada: ${categoria.titulo}")
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

                    // Alerts
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onNavigate("alerts") }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_notifications),
                            contentDescription = "Alerts",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text("Alertas", color = Color.White)
                    }

                    // Me
                    Box{
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {  showOptionsMenu = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Opciones",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text("Opciones", color = Color.White)
                        }

                        DropdownMenu(
                            expanded = showOptionsMenu,  // Nueva variable de estado
                            onDismissRequest = { showOptionsMenu = false },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.85f))
                                .width(150.dp),
                            offset = DpOffset(x = 38.dp, y = (-6).dp)
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    showOptionsMenu = false
                                    getCurrentLocation(context)?.let { location ->
                                        val point = GeoPoint(location.latitude, location.longitude)
                                        val address = obtenerDireccion(context, location.latitude, location.longitude)
                                        onLocationSelected(point, address)
                                        onDismiss()
                                    }
                                },
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MyLocation,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Seleccionar mi ubicación",
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

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    isSearching: Boolean,
    mapView: MapView?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
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
                textStyle = TextStyle(
                    color = Color.Black
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchQuery.isNotEmpty()) {
                            onSearch()
                        }
                    }
                ),
                modifier = Modifier
                    .weight(1f)
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
                            if (searchQuery.isNotEmpty()) {
                                onSearch()
                            }
                            true
                        } else {
                            false
                        }
                    },
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Buscar ubicación",
                                color = Color.Gray
                            )
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
                        // Limpiar overlays cuando se limpia la búsqueda
                        mapView?.let { map ->
                            // Cerrar cualquier InfoWindow abierta
                            InfoWindow.closeAllInfoWindowsOn(map)

                            // Limpiar overlays de búsqueda
                            map.overlays?.removeAll { overlay ->
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
                            map.invalidate()
                        }
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