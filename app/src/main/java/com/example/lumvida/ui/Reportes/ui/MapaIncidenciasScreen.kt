/*La pantalla permite a los usuarios visualizar marcadores de diferentes categorías de
reportes (como bacheo, alumbrado público, drenajes obstruidos y basura acumulada) sobre
 un mapa OpenStreetMap. Los usuarios pueden buscar ubicaciones, filtrar reportes por categoría,
  ver detalles de incidencias al hacer clic en marcadores, y interactuar con controles de zoom
  y ubicación. El mapa está configurado con límites geográficos específicos de Quintana Roo,
  muestra la ubicación actual del usuario, y tiene funcionalidades como sugerencias de búsqueda,
   manejo de permisos de ubicación, y gestión de estado de conexión a internet, todo esto implementado
    de manera modular y reactiva utilizando las tecnologías de Kotlin, Jetpack Compose y la librería
    osmdroid para renderizar mapas.*/

package com.example.lumvida.ui.Reportes.ui

import java.util.*
import org.osmdroid.views.overlay.infowindow.InfoWindow
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.lumvida.R
import com.example.lumvida.network.RetrofitClient
import com.example.lumvida.network.model.NominatimResponse
import com.example.lumvida.ui.Categorias.ui.CategoriasViewModel
import com.example.lumvida.ui.Reportes.ui.MapaIncidenciasViewModel.Companion.QUINTANA_ROO_CENTER_LAT
import com.example.lumvida.ui.Reportes.ui.MapaIncidenciasViewModel.Companion.QUINTANA_ROO_CENTER_LON
import com.example.lumvida.ui.Reportes.ui.MapaIncidenciasViewModel.Companion.ZOOM_LEVEL_STATE
import com.example.lumvida.ui.components.NetworkStatusMessage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapaIncidenciasScreen(
    viewModel: MapaIncidenciasViewModel,
    categoriasViewModel: CategoriasViewModel,
    onLocationSelected: (GeoPoint, String) -> Unit,
    onDismiss: () -> Unit,
    initialLocation: GeoPoint,
    onNavigate: (String) -> Unit = {},
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
    var isConnected by remember { mutableStateOf(RetrofitClient.isOnline(context)) }
    var wasDisconnected by remember { mutableStateOf(false) }

    // Permisos de ubicación
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Actualizar la ubicación del usuario cuando cambie
    LaunchedEffect(Unit) {
        getCurrentLocation(context)?.let { location ->
            viewModel.updateLastKnownLocation(location)
        }
    }

    // Escuchar cambios de ubicación
    DisposableEffect(Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener = LocationListener { location ->
            viewModel.updateLastKnownLocation(location)
        }

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000, // Actualizar cada 5 segundos
                10f,  // O cuando se mueva 10 metros
                locationListener
            )
        }

        onDispose {
            locationManager.removeUpdates(locationListener)
        }
    }

    // Monitorear conexión
    LaunchedEffect(Unit) {
        while(true) {
            val connectionState = RetrofitClient.isOnline(context)
            if (isConnected != connectionState) {
                if (!connectionState) {
                    wasDisconnected = true
                }
                isConnected = connectionState
                if (isConnected && wasDisconnected) {
                    viewModel.obtenerReportes()
                }
            }
            delay(1000)
        }
    }

    // Obtener reportes iniciales
    LaunchedEffect(Unit) {
        viewModel.obtenerReportes()
        viewModel.iniciarEscuchaReportes()
    }

    // Efecto para agregar los marcadores cuando cambien los reportes filtrados
    LaunchedEffect(viewModel.filteredReportes, viewModel.selectedCategory) {
        mapView?.let { map ->
            try {
                // Mantener overlays existentes
                val locationOverlay = map.overlays.find { it is MyLocationNewOverlay }
                val searchOverlays = map.overlays.filter { overlay ->
                    when (overlay) {
                        is Marker -> overlay.id in listOf("search_overlay", "search_overlay_start", "search_overlay_end")
                        is org.osmdroid.views.overlay.Polyline -> overlay.id == "search_overlay"
                        else -> false
                    }
                }

                map.overlays.clear()
                locationOverlay?.let { map.overlays.add(it) }
                map.overlays.addAll(searchOverlays)

                viewModel.filteredReportes.forEach { reporte ->
                    val marker = Marker(map).apply {
                        position = GeoPoint(reporte.latitud, reporte.longitud)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                        // Seleccionar el ícono correcto según la categoría
                        icon = ContextCompat.getDrawable(context, when(reporte.categoria.lowercase()) {
                            "bacheo" -> R.drawable.ic_marker_warning
                            "alumbrado público" -> R.drawable.ic_marker_lightbulb
                            "drenajes obstruidos" -> R.drawable.ic_marker_water
                            "basura acumulada" -> R.drawable.ic_marker_delete
                            else -> R.drawable.ic_location_marker
                        })?.apply {
                            // Aplicar color según la categoría
                            setTint(when(reporte.categoria.lowercase()) {
                                "bacheo" -> android.graphics.Color.RED
                                "alumbrado público" -> android.graphics.Color.rgb(255, 165, 0)  // Naranja
                                "drenajes obstruidos" -> android.graphics.Color.BLUE
                                "basura acumulada" -> android.graphics.Color.GREEN
                                else -> android.graphics.Color.BLACK
                            })
                        }

                        relatedObject = reporte
                        infoWindow = CustomInfoWindow(map)

                        setOnMarkerClickListener { marker, mapView ->
                            try {
                                InfoWindow.closeAllInfoWindowsOn(mapView)
                                marker.showInfoWindow()
                                mapView.controller.animateTo(marker.position)
                                mapView.controller.setZoom(18.0)
                                true
                            } catch (e: Exception) {
                                Log.e("MarkerClick", "Error al mostrar info window", e)
                                false
                            }
                        }
                    }

                    map.overlays.add(marker)
                }

                map.invalidate()
            } catch (e: Exception) {
                Log.e("MapaIncidencias", "Error al actualizar marcadores", e)
            }
        }
    }

    // Configuración inicial del mapa
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue = context.packageName
        }

        mapView?.let { view ->
            viewModel.centerMapOnUserLocation(context, view)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        // Componente del mapa
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    setBuiltInZoomControls(false)
                    mapView = this

                    setupMapInitialConfig(context)

                    controller.setCenter(GeoPoint(QUINTANA_ROO_CENTER_LAT, QUINTANA_ROO_CENTER_LON))
                    controller.setZoom(ZOOM_LEVEL_STATE)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Barra de búsqueda
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { query ->
                searchQuery = query
                viewModel.getSuggestions(query)
                showSuggestions = true
            },
            onSearch = { searchLocation(searchQuery, scope, mapView) },
            isSearching = isSearching,
            mapView = mapView,
            showCategoriesMenu = showCategoriesMenu,
            onShowCategoriesMenu = { showCategoriesMenu = it },
            viewModel = viewModel,
            categoriasViewModel = categoriasViewModel,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            showSuggestions = showSuggestions,
            onShowSuggestionsChange = { showSuggestions = it }
        )

        // Controles del mapa
        MapControls(
            onZoomIn = { mapView?.controller?.zoomIn() },
            onZoomOut = { mapView?.controller?.zoomOut() },
            onLocationClick = {
                getCurrentLocation(context)?.let { location ->
                    currentLocation = GeoPoint(location.latitude, location.longitude)
                    mapView?.controller?.animateTo(currentLocation)
                    mapView?.controller?.setZoom(18.0)
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        )

        // Estado de la red
        NetworkStatusMessage(
            isConnected = isConnected,
            wasDisconnected = wasDisconnected,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
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
private fun MapControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(48.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(24.dp)),
            color = Color.Black.copy(alpha = 0.75f),
            shadowElevation = 4.dp
        ) {
            Column {
                IconButton(
                    onClick = onZoomIn,
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
                    onClick = onZoomOut,
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

        Surface(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            color = Color.Black.copy(alpha = 0.75f),
            shadowElevation = 4.dp
        ) {
            IconButton(onClick = onLocationClick) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_mylocation),
                    contentDescription = "Mi ubicación",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    isSearching: Boolean,
    mapView: MapView?,
    showCategoriesMenu: Boolean,
    onShowCategoriesMenu: (Boolean) -> Unit,
    viewModel: MapaIncidenciasViewModel,
    categoriasViewModel: CategoriasViewModel,
    modifier: Modifier = Modifier,
    showSuggestions: Boolean,
    onShowSuggestionsChange: (Boolean) -> Unit
) {
    val suggestions by viewModel.searchSuggestions.collectAsState()

    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                        keyboardActions = KeyboardActions(onSearch = {
                            if (searchQuery.isNotEmpty()) {
                                onSearch()
                                onShowSuggestionsChange(false)
                            }
                        }),
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
                                onShowSuggestionsChange(false)
                                mapView?.let { map ->
                                    InfoWindow.closeAllInfoWindowsOn(map)
                                    map.overlays.removeAll { overlay ->
                                        when (overlay) {
                                            is Marker -> overlay.id in listOf("search_overlay", "search_overlay_start", "search_overlay_end")
                                            is org.osmdroid.views.overlay.Polyline -> overlay.id == "search_overlay"
                                            is org.osmdroid.views.overlay.Polygon -> overlay.id == "search_overlay"
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

            Surface(
                modifier = Modifier.size(56.dp),
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
        }

        // Sugerencias
        if (showSuggestions && suggestions.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(suggestions) { suggestion ->
                        Text(
                            text = suggestion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSearchQueryChange(suggestion)
                                    onSearch()
                                    onShowSuggestionsChange(false)
                                }
                                .padding(16.dp),
                            color = Color.Black
                        )
                    }
                }
            }
        }

        if (showCategoriesMenu) {
            DropdownMenu(
                expanded = showCategoriesMenu,
                onDismissRequest = { onShowCategoriesMenu(false) },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.85f))
                    .width(200.dp),
                offset = DpOffset(x = (-150).dp, y = 0.dp)
            ) {
                CategoryMenuItems(
                    viewModel = viewModel,
                    categoriasViewModel = categoriasViewModel,
                    mapView = mapView,
                    onShowCategoriesMenu = onShowCategoriesMenu
                )
            }
        }
    }
}

@Composable
private fun CategoryMenuItems(
    viewModel: MapaIncidenciasViewModel,
    categoriasViewModel: CategoriasViewModel,
    mapView: MapView?,
    onShowCategoriesMenu: (Boolean) -> Unit
) {
    DropdownMenuItem(
        onClick = {
            onShowCategoriesMenu(false)
            InfoWindow.closeAllInfoWindowsOn(mapView)
            viewModel.updateSelectedCategory(null)
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

    categoriasViewModel.categorias.forEach { categoria ->
        DropdownMenuItem(
            onClick = {
                onShowCategoriesMenu(false)
                InfoWindow.closeAllInfoWindowsOn(mapView)
                viewModel.updateSelectedCategory(categoria.titulo)
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

private fun MapView.setupMapInitialConfig(context: Context) {
    getCurrentLocation(context)?.let { location ->
        val lat = location.latitude
        val lon = location.longitude
        val isInQuintanaRoo = lat >= MapaIncidenciasViewModel.QUINTANA_ROO_LAT_MIN &&
                lat <= MapaIncidenciasViewModel.QUINTANA_ROO_LAT_MAX &&
                lon >= MapaIncidenciasViewModel.QUINTANA_ROO_LON_MIN &&
                lon <= MapaIncidenciasViewModel.QUINTANA_ROO_LON_MAX

        // Establecer límites de scroll para Quintana Roo
        setScrollableAreaLimitLatitude(
            MapaIncidenciasViewModel.QUINTANA_ROO_LAT_MAX,
            MapaIncidenciasViewModel.QUINTANA_ROO_LAT_MIN,
            0
        )
        setScrollableAreaLimitLongitude(
            MapaIncidenciasViewModel.QUINTANA_ROO_LON_MIN,
            MapaIncidenciasViewModel.QUINTANA_ROO_LON_MAX,
            0
        )

        // Configurar niveles de zoom
        maxZoomLevel = MapaIncidenciasViewModel.ZOOM_LEVEL_4
        minZoomLevel = MapaIncidenciasViewModel.ZOOM_LEVEL_1

        if (isInQuintanaRoo) {
            controller.setCenter(GeoPoint(lat, lon))
            controller.setZoom(MapaIncidenciasViewModel.ZOOM_LEVEL_LOCAL)
        } else {
            controller.setCenter(GeoPoint(
                MapaIncidenciasViewModel.QUINTANA_ROO_CENTER_LAT,
                MapaIncidenciasViewModel.QUINTANA_ROO_CENTER_LON
            ))
            controller.setZoom(MapaIncidenciasViewModel.ZOOM_LEVEL_STATE)
        }

        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
        locationOverlay.enableMyLocation()
        overlays.add(locationOverlay)
    } ?: run {
        controller.setCenter(GeoPoint(
            MapaIncidenciasViewModel.QUINTANA_ROO_CENTER_LAT,
            MapaIncidenciasViewModel.QUINTANA_ROO_CENTER_LON
        ))
        controller.setZoom(MapaIncidenciasViewModel.ZOOM_LEVEL_STATE)
    }
}

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

private fun searchLocation(
    query: String,
    scope: CoroutineScope,
    mapView: MapView?
) {
    scope.launch {
        try {
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
            }
        } catch (e: Exception) {
            Log.e("MapScreen", "Error searching location", e)
        }
    }
}

private fun createSearchOverlay(mapView: MapView, result: NominatimResponse) {
    // Eliminar overlays anteriores
    mapView.overlays.removeAll { overlay ->
        when (overlay) {
            is Marker -> overlay.id in listOf("search_overlay", "search_overlay_start", "search_overlay_end")
            is org.osmdroid.views.overlay.Polyline -> overlay.id == "search_overlay"
            is org.osmdroid.views.overlay.Polygon -> overlay.id == "search_overlay"
            else -> false
        }
    }

    val marker = Marker(mapView).apply {
        id = "search_overlay"
        position = GeoPoint(result.lat.toDouble(), result.lon.toDouble())
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        icon = ContextCompat.getDrawable(mapView.context, R.drawable.ic_location_marker)?.apply {
            setTint(android.graphics.Color.BLACK)
        }
        title = result.displayName
    }
    mapView.overlays.add(marker)
    mapView.invalidate()
}

private fun createCirclePoints(center: GeoPoint, radiusMeters: Double): ArrayList<GeoPoint> {
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