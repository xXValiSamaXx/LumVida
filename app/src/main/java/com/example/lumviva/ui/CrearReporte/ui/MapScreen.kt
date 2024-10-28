package com.example.lumviva.ui.CrearReporte.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    onLocationSelected: (GeoPoint, String) -> Unit,
    onDismiss: () -> Unit,
    initialLocation: GeoPoint
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }

    // Estado para los permisos de ubicación
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Configurar OSMDroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue = context.packageName
        }
    }

    // Obtener ubicación actual cuando se tengan los permisos
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            getCurrentLocation(context)?.let { location ->
                currentLocation = GeoPoint(location.latitude, location.longitude)
                mapView?.controller?.apply {
                    setCenter(currentLocation)
                    setZoom(18.0) // Zoom más cercano
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)

                    // Configuración inicial
                    controller.setZoom(18.0)

                    // Overlay de ubicación actual
                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                    locationOverlay.enableMyLocation()
                    locationOverlay.enableFollowLocation()
                    overlays.add(locationOverlay)

                    // Desactivar el desplazamiento del mapa
                    setOnTouchListener { _, _ -> true }

                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Botones de zoom (opcionales ya que el mapa está fijo)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 80.dp)
        ) {
            Button(
                onClick = { mapView?.controller?.zoomIn() },
                modifier = Modifier
                    .size(50.dp)
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Text("+", color = Color.Black)
            }
            Button(
                onClick = { mapView?.controller?.zoomOut() },
                modifier = Modifier.size(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Text("-", color = Color.Black)
            }
        }

        // Botones de acción
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B0000)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Cancelar", color = Color.White)
            }

            Button(
                onClick = {
                    currentLocation?.let { location ->
                        val address = obtenerDireccion(context, location.latitude, location.longitude)
                        onLocationSelected(location, address)
                    }
                },
                enabled = currentLocation != null,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Seleccionar", color = Color.White)
            }
        }

        // Mensaje si no hay permisos
        if (!locationPermissionState.status.isGranted) {
            AlertDialog(
                onDismissRequest = { /* No hacer nada */ },
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
}

// Función para obtener la ubicación actual
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

// Función para obtener la dirección a partir de coordenadas
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