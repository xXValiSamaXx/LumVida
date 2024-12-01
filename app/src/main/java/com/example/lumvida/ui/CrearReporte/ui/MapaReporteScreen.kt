package com.example.lumvida.ui.CrearReporte.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lumvida.LumVidaApplication
import com.example.lumvida.R
import com.example.lumvida.network.RetrofitClient
import com.example.lumvida.ui.components.NetworkStatusMessage
import com.example.lumvida.ui.theme.Primary
import com.example.lumvida.ui.theme.TextPrimary
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
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
    var isLocationSelectionMode by remember { mutableStateOf(false) }
    var currentMarker by remember { mutableStateOf<Marker?>(null) }
    var lastUpdateTime by remember { mutableStateOf(0L) }
    val debounceTime = 300L

    val scope = rememberCoroutineScope()

    val sheetState = rememberBottomSheetState(
        initialValue = BottomSheetValue.Collapsed,
        confirmStateChange = { true }
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

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

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp), // Reducido de 32.dp a 16.dp
                horizontalAlignment = Alignment.CenterHorizontally
            )  {
                // Área arrastrable
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }

                Text(
                    text = if (isLocationSelectionMode) "Fija tu destino" else "Mi ubicación",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                if (isLocationSelectionMode) {
                    Text(
                        text = "Arrastra el mapa para mover el marcador",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .draggable(
                            state = rememberDraggableState { delta ->
                                scope.launch {
                                    if (delta > 0 && sheetState.isCollapsed) {
                                        sheetState.expand()
                                    } else if (delta < 0 && sheetState.isExpanded) {
                                        sheetState.collapse()
                                    }
                                }
                            },
                            orientation = Orientation.Vertical
                        ),
                    enabled = true,
                    placeholder = {
                        Text(
                            "Buscar dirección",
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f)
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = Color.White
                        )
                    },
                    singleLine = true,
                    interactionSource = remember { MutableInteractionSource() }
                )

                Button(
                    onClick = {
                        if (isLocationSelectionMode) {
                            currentMarker?.let { marker ->
                                val geoPoint = marker.position
                                onLocationSelected(geoPoint, searchText)
                            }
                        } else {
                            mapView?.mapCenter?.let { center ->
                                val geoPoint = GeoPoint(center.latitude, center.longitude)
                                onLocationSelected(geoPoint, searchText)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp), // Reducido de 50.dp a 48.dp para ser más compacto
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Confirmar ubicación",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                }

                if (sheetState.isExpanded) {
                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (isLocationSelectionMode) {
                                isLocationSelectionMode = false
                                currentMarker?.let {
                                    mapView?.overlays?.remove(it)
                                    currentMarker = null
                                }
                                mapView?.invalidate()
                            }
                            scope.launch {
                                sheetState.collapse()
                            }
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(
                        color = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Botón para selección manual
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isLocationSelectionMode = true
                                scope.launch {
                                    sheetState.collapse()
                                }
                            }
                            .padding(vertical = 8.dp),
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
        },
        sheetPeekHeight = 180.dp,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetBackgroundColor = Color.Black.copy(alpha = 0.8f),
        sheetElevation = 8.dp,
        sheetGesturesEnabled = true
    ) {
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
                        setMultiTouchControls(false)  // Desactivado por defecto
                        isClickable = false           // Desactivado por defecto
                        setBuiltInZoomControls(false)
                        setHorizontalMapRepetitionEnabled(false)
                        setVerticalMapRepetitionEnabled(false)

                        getCurrentLocation(context)?.let { location ->
                            // Limitar el área de movimiento a Quintana Roo
                            setScrollableAreaLimitLatitude(
                                MapConstants.QUINTANA_ROO_LAT_MAX,
                                MapConstants.QUINTANA_ROO_LAT_MIN,
                                0
                            )
                            setScrollableAreaLimitLongitude(
                                MapConstants.QUINTANA_ROO_LON_MIN,
                                MapConstants.QUINTANA_ROO_LON_MAX,
                                0
                            )

                            // Establecer límites de zoom
                            minZoomLevel = 14.0  // Zoom out máximo
                            maxZoomLevel = 19.0  // Zoom in máximo

                            // Iniciar con un zoom específico
                            controller.setZoom(16.0)  // Zoom inicial
                            controller.setCenter(GeoPoint(location.latitude, location.longitude))

                            // Configurar overlay de ubicación
                            val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                            locationOverlay.enableMyLocation()
                            overlays.add(locationOverlay)
                        }

                        // Limitar el área de movimiento cuando no está en modo manual
                        addMapListener(object : MapListener {
                            override fun onScroll(event: ScrollEvent?): Boolean {
                                if (!isLocationSelectionMode) {
                                    getCurrentLocation(context)?.let { location ->
                                        val currentLocation = GeoPoint(location.latitude, location.longitude)
                                        val distance = currentLocation.distanceToAsDouble(mapCenter)

                                        // Si se aleja más de 1km de la ubicación actual
                                        if (distance > 1000) {
                                            controller.animateTo(currentLocation)
                                        }
                                    }
                                }
                                return true
                            }

                            override fun onZoom(event: ZoomEvent?): Boolean {
                                if (!isLocationSelectionMode) {
                                    // Mantener el zoom entre los límites cuando no está en modo manual
                                    val zoomLevel = zoomLevel
                                    if (zoomLevel < 14.0) controller.setZoom(14.0)
                                    if (zoomLevel > 19.0) controller.setZoom(19.0)
                                }
                                return true
                            }
                        })

                        mapView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Efecto para manejar el modo de selección
            LaunchedEffect(isLocationSelectionMode) {
                mapView?.let { map ->
                    map.setMultiTouchControls(isLocationSelectionMode)
                    map.isClickable = isLocationSelectionMode

                    if (isLocationSelectionMode) {
                        // En modo manual, permitir más libertad
                        map.minZoomLevel = 17.0
                        map.maxZoomLevel = 20.0

                        currentMarker?.let { map.overlays.remove(it) }
                        val marker = Marker(map).apply {
                            isDraggable = true
                            icon = ContextCompat.getDrawable(context, R.drawable.ic_location_marker)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        updateMarkerPosition(map, marker)
                        currentMarker = marker
                        map.overlays.add(marker)
                    } else {
                        // En modo normal, restringir más
                        map.minZoomLevel = 16.0
                        map.maxZoomLevel = 18.0

                        getCurrentLocation(context)?.let { location ->
                            val point = GeoPoint(location.latitude, location.longitude)
                            map.controller.animateTo(point)
                            map.controller.setZoom(16.0)
                            currentMarker?.let { map.overlays.remove(it) }
                            currentMarker = null
                            searchText = obtenerDireccion(context, location.latitude, location.longitude)
                        }
                    }
                    map.invalidate()
                }
            }

            // Listener del mapa
            LaunchedEffect(mapView) {
                mapView?.setMapListener(object : MapListener {
                    override fun onScroll(event: ScrollEvent?): Boolean {
                        if (isLocationSelectionMode) {
                            val currentTime = System.currentTimeMillis()
                            val map = mapView ?: return true
                            val center = map.mapCenter

                            // Actualizar posición del marcador al centro exacto
                            currentMarker?.position = GeoPoint(center.latitude, center.longitude)

                            if (currentTime - lastUpdateTime > debounceTime) {
                                searchText = obtenerDireccion(context, center.latitude, center.longitude)
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

                            // Actualizar posición del marcador al centro exacto
                            currentMarker?.position = GeoPoint(center.latitude, center.longitude)

                            if (currentTime - lastUpdateTime > debounceTime) {
                                searchText = obtenerDireccion(context, center.latitude, center.longitude)
                                lastUpdateTime = currentTime
                            }

                            map.invalidate()
                        }
                        return true
                    }
                })
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
                    IconButton(
                        onClick = {
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
                                getCurrentLocation(context)?.let { location ->
                                    val point = GeoPoint(location.latitude, location.longitude)
                                    searchText = obtenerDireccion(context, location.latitude, location.longitude)
                                    map.controller.animateTo(point)
                                    map.controller.setZoom(20.0)
                                }
                                map.invalidate()
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
}

private fun updateMarkerPosition(map: MapView, marker: Marker) {
    val center = map.mapCenter
    marker.position = GeoPoint(center.latitude, center.longitude)
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