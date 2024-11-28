package com.example.lumvida.ui.CrearReporte.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.lumvida.network.RetrofitClient
import com.example.lumvida.network.model.NominatimResponse
import com.example.lumvida.ui.components.NetworkStatusMessage
import com.example.lumvida.ui.theme.Primary
import com.example.lumvida.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lumvida.LumVidaApplication
import com.example.lumvida.R
import com.example.lumvida.data.model.RecentSearch
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

@RequiresApi(Build.VERSION_CODES.O)

//Constantes de limites de chetumal
private const val MIN_ZOOM = 14.0
private const val MAX_ZOOM = 21.0

//Constantes de limites
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
        21.30, // North
        -86.75, // East
        20.95, // South
        -86.95  // West
    ),
    center = GeoPoint(21.1483, -86.8339)
)

data class CityConfig(
    val bounds: BoundingBox,
    val center: GeoPoint,
    val defaultZoom: Double = 15.0
)

@Composable
fun MapaReporte(
    onLocationSelected: (GeoPoint, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: MapaReporteViewModel = viewModel(
        factory = MapaReporteViewModel.Companion.Factory(
            (context.applicationContext as LumVidaApplication).searchHistoryRepository
        )
    )
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var searchText by remember { mutableStateOf("") }
    var isConnected by remember { mutableStateOf(RetrofitClient.isOnline(context)) }
    var wasDisconnected by remember { mutableStateOf(false) }
    var showSearchModal by remember { mutableStateOf(false) }
    var isLocationSelectionMode by remember { mutableStateOf(false) }
    var currentMarker by remember { mutableStateOf<Marker?>(null) }
    var lastUpdateTime by remember { mutableStateOf(0L) }
    val debounceTime = 200L // milisegundos
    var searchSuggestions by remember { mutableStateOf<List<NominatimResponse>>(emptyList()) }

    // Mostrar el modal cuando showSearchModal es true
    if (showSearchModal) {
        Dialog(
            onDismissRequest = { showSearchModal = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            SearchLocationModal(
                searchText = searchText,
                onDismiss = { showSearchModal = false },
                onLocationSelected = { location ->
                    val geoPoint = GeoPoint(location.lat.toDouble(), location.lon.toDouble())
                    mapView?.controller?.animateTo(geoPoint)
                    mapView?.controller?.setZoom(18.0)
                    searchText = location.displayName
                },
                viewModel = viewModel,
                onLocationSelectionMode = {
                    isLocationSelectionMode = true
                },
                mapView = mapView,
                onSearchTextChange = { newText ->
                    searchText = newText
                },
                isLocationSelectionMode = isLocationSelectionMode,  // Añadido
                currentMarker = currentMarker,  // Añadido
                onLocationSelectionModeChange = { newValue ->  // Añadido
                    isLocationSelectionMode = newValue
                },
                onCurrentMarkerChange = { newMarker ->  // Añadido
                    currentMarker = newMarker
                }
            )
        }
    }

    // Monitorear la conexión
    LaunchedEffect(Unit) {
        while(true) {
            val connectionState = RetrofitClient.isOnline(context)
            if (isConnected != connectionState) {
                if (!connectionState) {
                    wasDisconnected = true
                }
                isConnected = connectionState
            }
            delay(1000)
        }
    }

    // Efecto para obtener y establecer la dirección inicial
    LaunchedEffect(Unit) {
        getCurrentLocation(context)?.let { location ->
            val direccionActual = obtenerDireccion(
                context,
                location.latitude,
                location.longitude
            )
            searchText = direccionActual
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue = context.packageName
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Mapa
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)  // Cambiado a false para permitir movimiento con un dedo
                    isClickable = true  // Asegura que el mapa responde a toques
                    setBuiltInZoomControls(false)
                    setHorizontalMapRepetitionEnabled(false)
                    setVerticalMapRepetitionEnabled(false)

                    // Configurar ubicación actual
                    getCurrentLocation(context)?.let { location ->
                        val lat = location.latitude
                        val lon = location.longitude

                        // Determinar qué configuración usar basado en la ubicación
                        val config = when {
                            CHETUMAL_CONFIG.bounds.contains(lat, lon) -> CHETUMAL_CONFIG
                            CANCUN_CONFIG.bounds.contains(lat, lon) -> CANCUN_CONFIG
                            else -> CHETUMAL_CONFIG
                        }

                        // Aplicar límites según la ciudad
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

                        // Resto de la configuración
                        setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                        isFlingEnabled = true
                        maxZoomLevel = MAX_ZOOM
                        minZoomLevel = MIN_ZOOM

                        controller.setZoom(20.0)
                        controller.setCenter(GeoPoint(lat, lon))
                        controller.animateTo(GeoPoint(lat, lon))

                        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                        locationOverlay.enableMyLocation()
                        overlays.add(locationOverlay)
                    } ?: run {
                        // Si no hay ubicación, centrar en Chetumal por defecto
                        controller.setCenter(CHETUMAL_CONFIG.center)
                        controller.setZoom(CHETUMAL_CONFIG.defaultZoom)
                        setScrollableAreaLimitLatitude(
                            CHETUMAL_CONFIG.bounds.latNorth,
                            CHETUMAL_CONFIG.bounds.latSouth,
                            0
                        )
                        setScrollableAreaLimitLongitude(
                            CHETUMAL_CONFIG.bounds.lonWest,
                            CHETUMAL_CONFIG.bounds.lonEast,
                            0
                        )
                    }

                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        LaunchedEffect(isLocationSelectionMode) {
            if (isLocationSelectionMode) {
                mapView?.let { map ->
                    // Eliminar marcador anterior si existe
                    currentMarker?.let { map.overlays.remove(it) }

                    // Crear nuevo marcador con posición ajustada
                    val marker = Marker(map).apply {
                        isDraggable = true
                        icon = ContextCompat.getDrawable(context, R.drawable.ic_location_marker)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }

                    // Función para actualizar la posición del marcador
                    fun updateMarkerPosition() {
                        val offset = 0.2 // Factor de desplazamiento hacia arriba
                        val center = map.mapCenter
                        val newLat = center.latitude + (map.boundingBox.latitudeSpan * offset)
                        marker.position = GeoPoint(newLat, center.longitude)
                    }

                    // Establecer posición inicial
                    updateMarkerPosition()

                    // Actualizar listener del mapa
                    map.setMapListener(object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            updateMarkerPosition()
                            map.invalidate()
                            return true
                        }

                        override fun onZoom(event: ZoomEvent?): Boolean {
                            updateMarkerPosition()
                            map.invalidate()
                            return true
                        }
                    })

                    currentMarker = marker
                    map.overlays.add(marker)
                    map.invalidate()
                }
            } else {
                // Limpiar cuando se desactiva el modo de selección
                currentMarker?.let { marker ->
                    mapView?.overlays?.remove(marker)
                    mapView?.invalidate()
                }
                currentMarker = null
            }
        }

        // Añadir el listener del mapa justo después
        LaunchedEffect(mapView) {
            mapView?.setMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    if (isLocationSelectionMode) {
                        val currentTime = System.currentTimeMillis()
                        val map = mapView ?: return true
                        val center = map.mapCenter

                        // Actualizar posición del marcador inmediatamente
                        val offset = 0.3
                        val newLat = center.latitude + (map.boundingBox.latitudeSpan * offset)
                        val geoPoint = GeoPoint(newLat, center.longitude)
                        currentMarker?.position = geoPoint

                        // Debounce la actualización de la dirección
                        if (currentTime - lastUpdateTime > debounceTime) {
                            searchText = obtenerDireccion(context, newLat, center.longitude)
                            lastUpdateTime = currentTime
                        }

                        map.invalidate()
                    }
                    return true
                }

                override fun onZoom(event: ZoomEvent?): Boolean {
                    if (isLocationSelectionMode) {
                        val currentTime = System.currentTimeMillis()
                        val map = mapView ?: return true
                        val center = map.mapCenter

                        val offset = 0.3
                        val newLat = center.latitude + (map.boundingBox.latitudeSpan * offset)
                        val geoPoint = GeoPoint(newLat, center.longitude)
                        currentMarker?.position = geoPoint

                        // Debounce la actualización de la dirección
                        if (currentTime - lastUpdateTime > debounceTime) {
                            searchText = obtenerDireccion(context, newLat, center.longitude)
                            lastUpdateTime = currentTime
                        }

                        map.invalidate()
                    }
                    return true
                }
            })
        }

        // Panel ahora en la parte inferior
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            color = Color.Black.copy(alpha = 0.8f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Barra de arrastre clickeable
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(24.dp) // Aumentado para mejor área táctil
                        .clickable { showSearchModal = true }
                        .padding(vertical = 10.dp), // Padding para centrar la barra
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }

                Text(
                    text = "Mi ubicación",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Mostrar mensaje solo en modo de selección
                if (isLocationSelectionMode) {
                    Text(
                        text = "Arrastra el mapa para mover el marcador",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Barra de búsqueda
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    placeholder = { Text("Buscando ubicación actual...", color = Color.White.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = Color.White
                        )
                    },
                    singleLine = true
                )

                // Botones
                // Botones
                Button(
                    onClick = {
                        if (isLocationSelectionMode) {
                            currentMarker?.let { marker ->
                                val geoPoint = marker.position
                                onLocationSelected(geoPoint, searchText)
                            }
                        } else {
                            // Si no está en modo selección, usar la ubicación actual mostrada
                            mapView?.mapCenter?.let { center ->
                                val geoPoint = GeoPoint(center.latitude, center.longitude)
                                onLocationSelected(geoPoint, searchText)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(
                        "Confirmar ubicación",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        if (isLocationSelectionMode) {
                            isLocationSelectionMode = false
                            currentMarker?.let {
                                mapView?.overlays?.remove(it)
                                currentMarker = null // Limpia la referencia
                            }
                            mapView?.invalidate()
                        }
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text(
                        "Cancelar",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        // Controles de zoom y ubicación
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .width(48.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Controles de zoom
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(24.dp)),
                color = Color.Black.copy(alpha = 0.75f),
                shadowElevation = 4.dp
            ) {
                Column {
                    IconButton(
                        onClick = { mapView?.controller?.zoomIn() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Text("+", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Divider(color = Color.White.copy(alpha = 0.2f))
                    IconButton(
                        onClick = { mapView?.controller?.zoomOut() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Text("-", color = Color.White, fontWeight = FontWeight.Bold)
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
                // Botón de ubicación actual
                IconButton(
                    onClick = {
                        // Desactivar modo de selección si está activo
                        if (isLocationSelectionMode) {
                            isLocationSelectionMode = false
                            currentMarker?.let { marker ->
                                mapView?.overlays?.remove(marker)
                                currentMarker = null
                            }
                        }
                        mapView?.let { map ->
                            InfoWindow.closeAllInfoWindowsOn(map)
                            map.overlays.removeAll { overlay ->
                                overlay is Marker && overlay.id == "search_result_marker"
                            }
                            map.invalidate()
                        }
                        getCurrentLocation(context)?.let { location ->
                            val point = GeoPoint(location.latitude, location.longitude)
                            // Actualizar la dirección en el texto
                            searchText = obtenerDireccion(
                                context,
                                location.latitude,
                                location.longitude
                            )
                            mapView?.controller?.animateTo(point)
                            mapView?.controller?.setZoom(20.0)
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
}

@Composable
private fun SearchLocationModal(
    searchText: String,
    onDismiss: () -> Unit,
    onLocationSelected: (location: NominatimResponse) -> Unit,
    viewModel: MapaReporteViewModel,
    onLocationSelectionMode: () -> Unit,
    mapView: MapView?,
    onSearchTextChange: (String) -> Unit,
    isLocationSelectionMode: Boolean,  // Añadido
    currentMarker: Marker?,  // Añadido
    onLocationSelectionModeChange: (Boolean) -> Unit,  // Añadido
    onCurrentMarkerChange: (Marker?) -> Unit  // Añadido
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchSuggestions by remember { mutableStateOf<List<NominatimResponse>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val recentSearches by viewModel.recentSearches.collectAsState()

    fun formatTimeAgo(seconds: Long): String = when {
        seconds < 60 -> "$seconds seg"
        seconds < 3600 -> "${seconds / 60} min"
        seconds < 86400 -> "${seconds / 3600} hr"
        else -> "${seconds / 86400} d"
    }

    fun addToRecentSearches(suggestion: NominatimResponse) {
        viewModel.addToRecentSearches(suggestion)
    }

    fun removeFromRecentSearches(search: RecentSearch) {
        viewModel.removeFromRecentSearches(search)
    }

    fun getSuggestions(query: String) {
        if (query.length >= 3) {
            scope.launch {
                try {
                    isSearching = true
                    getCurrentLocation(context)?.let { location ->
                        val ciudad = when {
                            CHETUMAL_CONFIG.bounds.contains(location.latitude, location.longitude) -> "Chetumal"
                            CANCUN_CONFIG.bounds.contains(location.latitude, location.longitude) -> "Cancun"
                            else -> "Chetumal" // Default a Chetumal si no está en ninguna
                        }

                        val results = RetrofitClient.nominatimService.searchLocation(
                            query = "$query, $ciudad, Quintana Roo"
                        )
                        searchSuggestions = results
                    }
                } catch (e: Exception) {
                    Log.e("SearchLocationModal", "Error getting suggestions", e)
                    searchSuggestions = emptyList()
                } finally {
                    isSearching = false
                }
            }
        }
    }

    fun removeSuggestion(suggestionToRemove: NominatimResponse) {
        searchSuggestions = searchSuggestions.filterNot { it == suggestionToRemove }  // Cambiado de suggestions a searchSuggestions
    }

    fun clearSearch() {
        searchQuery = ""
        searchSuggestions = emptyList()
    }

    // Función para añadir marcador
    fun addMarkerToLocation(location: NominatimResponse) {
        mapView?.let { map ->
            // Limpiar marcadores existentes de búsqueda
            map.overlays.removeAll { overlay ->
                overlay is Marker && overlay.id == "search_result_marker"
            }

            // Crear y añadir nuevo marcador
            val marker = Marker(map).apply {
                id = "search_result_marker"
                position = GeoPoint(location.lat.toDouble(), location.lon.toDouble())
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = ContextCompat.getDrawable(map.context, R.drawable.ic_location_marker)
                title = location.displayName
            }
            map.overlays.add(marker)
            map.invalidate()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .statusBarsPadding()
            ) {
                // Header con título y botón cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Busca tu ubicación",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Barra de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        getSuggestions(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar dirección", color = Color.White.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { clearSearch() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar búsqueda",
                                    tint = Color.White
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = Color.White
                            )
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Resultados de búsqueda
                if (searchSuggestions.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                            .background(Color.Black)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(
                                items = searchSuggestions,
                                key = { it.placeId } // Usar un identificador único
                            ) { suggestion ->
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = suggestion.displayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.White,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    // Desactivar modo de selección si está activo
                                                    if (isLocationSelectionMode) {
                                                        onLocationSelectionModeChange(false)
                                                        currentMarker?.let { marker ->
                                                            mapView?.overlays?.remove(marker)
                                                            onCurrentMarkerChange(null)
                                                        }
                                                    }
                                                    addMarkerToLocation(suggestion)  // Añadir marcador
                                                    addToRecentSearches(suggestion)
                                                    onLocationSelected(suggestion)
                                                    onDismiss()
                                                }
                                        )

                                        IconButton(
                                            onClick = { removeSuggestion(suggestion) },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .padding(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Eliminar sugerencia",
                                                tint = Color.Gray
                                            )
                                        }
                                    }
                                    Divider(color = Color.White.copy(alpha = 0.1f))
                                }
                            }
                        }
                    }
                }

                // Búsquedas recientes
                // Búsquedas recientes y botón de establecer ubicación
                Column {
                    // Mostrar búsquedas recientes si existen
                    if (recentSearches.isNotEmpty()) {
                        Text(
                            text = "Recientes",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(max = 200.dp) // Altura máxima para el área de recientes
                        ) {
                            LazyColumn (
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(recentSearches) { search ->
                                    val secondsAgo = (System.currentTimeMillis() - search.timestamp) / 1000
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.width(70.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = formatTimeAgo(secondsAgo),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }

                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 8.dp)
                                                .clickable {
                                                    // Desactivar modo de selección si está activo
                                                    if (isLocationSelectionMode) {
                                                        onLocationSelectionModeChange(false)
                                                        currentMarker?.let { marker ->
                                                            mapView?.overlays?.remove(marker)
                                                            onCurrentMarkerChange(null)
                                                        }
                                                    }
                                                    val geoPoint = GeoPoint(search.lat, search.lon)
                                                    mapView?.let { map ->
                                                        // Limpiar marcadores existentes
                                                        map.overlays.removeAll { overlay ->
                                                            overlay is Marker && overlay.id == "search_result_marker"
                                                        }

                                                        // Añadir nuevo marcador
                                                        val marker = Marker(map).apply {
                                                            id = "search_result_marker"
                                                            position = geoPoint
                                                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                                            icon = ContextCompat.getDrawable(map.context, R.drawable.ic_location_marker)
                                                            title = search.address
                                                        }
                                                        map.overlays.add(marker)
                                                        map.controller.animateTo(geoPoint)
                                                        map.controller.setZoom(18.0)
                                                        map.invalidate()
                                                    }
                                                    onSearchTextChange(search.address)
                                                    onDismiss()
                                                }
                                        ) {
                                            Text(
                                                text = search.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White
                                            )
                                            Text(
                                                text = search.address,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }

                                        IconButton(
                                            onClick = { removeFromRecentSearches(search) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Eliminar",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Divider(color = Color.White.copy(alpha = 0.1f))
                                }
                            }
                        }

                        Divider(
                            color = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }

                    // Botón establecer ubicación en mapa (siempre visible)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Limpiar marcadores e info windows antes de cambiar de modo
                                mapView?.let { map ->
                                    InfoWindow.closeAllInfoWindowsOn(map) // Cerrar todos los info windows
                                    map.overlays.removeAll { overlay ->
                                        overlay is Marker && overlay.id == "search_result_marker"
                                    }
                                    map.invalidate()
                                }
                                onLocationSelectionMode()
                                onDismiss()
                            }
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Establecer la ubicación en el mapa",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
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
                    if (thoroughfare != null) {
                        append(thoroughfare)
                        if (subThoroughfare != null) {
                            append(" ")
                            append(subThoroughfare)
                        }
                        if (subLocality != null) {
                            append(", ")
                            append(subLocality)
                        }
                        append(", ")
                        append(locality ?: "Chetumal")
                        append(", ")
                        append(adminArea ?: "Quintana Roo")
                    } else {
                        append(getAddressLine(0))
                    }
                }
            }
        } else {
            "Ubicación actual (${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)})"
        }
    } catch (e: Exception) {
        "Ubicación actual (${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)})"
    }
}