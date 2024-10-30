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
    val context = LocalContext.current // Obtener el contexto actual
    val scope = rememberCoroutineScope() // Crear un scope de corrutinas para manejar tareas asíncronas
    var mapView by remember { mutableStateOf<MapView?>(null) } // Mantener una referencia al MapView
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) } // Mantener la ubicación actual

    // Estado para los permisos de ubicación
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Configurar OSMDroid al iniciar
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)) // Cargar configuración
            userAgentValue = context.packageName // Establecer el user agent
        }
    }

    // Obtener la ubicación actual cuando se tengan los permisos
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) { // Verificar si se han concedido los permisos
            getCurrentLocation(context)?.let { location -> // Obtener la ubicación actual
                currentLocation = GeoPoint(location.latitude, location.longitude) // Guardar la ubicación
                mapView?.controller?.apply {
                    setCenter(currentLocation) // Centrar el mapa en la ubicación actual
                    setZoom(18.0) // Establecer el nivel de zoom
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) { // Contenedor para el mapa y elementos UI
        // Mapa
        AndroidView(
            factory = { context -> // Crear el MapView
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK) // Establecer la fuente de tiles
                    setMultiTouchControls(true) // Permitir controles multi-touch

                    // Configuración inicial
                    controller.setZoom(18.0) // Establecer el zoom inicial

                    // Overlay para la ubicación actual
                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                    locationOverlay.enableMyLocation() // Habilitar la ubicación del usuario
                    locationOverlay.enableFollowLocation() // Habilitar seguimiento de la ubicación
                    overlays.add(locationOverlay) // Agregar overlay al mapa

                    // Desactivar el desplazamiento del mapa
                    setOnTouchListener { _, _ -> true } // Deshabilitar el desplazamiento del mapa

                    mapView = this // Guardar la referencia al MapView
                }
            },
            modifier = Modifier.fillMaxSize() // Ocupa todo el espacio disponible
        )

        // Botones de zoom (opcionales, ya que el mapa está fijo)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd) // Alinear a la parte inferior derecha
                .padding(16.dp)
                .padding(bottom = 80.dp)
        ) {
            // Botón para hacer zoom
            Button(
                onClick = { mapView?.controller?.zoomIn() }, // Aumentar el zoom
                modifier = Modifier
                    .size(50.dp)
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Text("+", color = Color.Black) // Texto del botón
            }
            // Botón para hacer zoom out
            Button(
                onClick = { mapView?.controller?.zoomOut() }, // Disminuir el zoom
                modifier = Modifier.size(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Text("-", color = Color.Black) // Texto del botón
            }
        }

        // Botones de acción
        Row(
            modifier = Modifier
                .fillMaxWidth() // Ocupar el ancho completo
                .align(Alignment.BottomCenter) // Alinear al centro inferior
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly // Espacio uniforme entre botones
        ) {
            // Botón de cancelar
            Button(
                onClick = onDismiss, // Llamar a la función onDismiss
                modifier = Modifier
                    .weight(1f) // Ocupar un peso igual
                    .padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B0000) // Color de fondo
                ),
                shape = MaterialTheme.shapes.medium // Forma del botón
            ) {
                Text("Cancelar", color = Color.White) // Texto del botón
            }

            // Botón de seleccionar
            Button(
                onClick = {
                    currentLocation?.let { location -> // Verificar si hay una ubicación actual
                        val address = obtenerDireccion(context, location.latitude, location.longitude) // Obtener dirección
                        onLocationSelected(location, address) // Llamar a la función onLocationSelected
                    }
                },
                enabled = currentLocation != null, // Habilitar solo si hay una ubicación actual
                modifier = Modifier
                    .weight(1f) // Ocupar un peso igual
                    .padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue // Color de fondo
                ),
                shape = MaterialTheme.shapes.medium // Forma del botón
            ) {
                Text("Seleccionar", color = Color.White) // Texto del botón
            }
        }

        // Mensaje si no hay permisos
        if (!locationPermissionState.status.isGranted) { // Verificar si no se han concedido permisos
            AlertDialog(
                onDismissRequest = { /* No hacer nada */ }, // Sin acción en el cierre
                title = { Text("Permisos necesarios") }, // Título del diálogo
                text = { Text("Se necesita acceso a la ubicación para mostrar tu posición en el mapa.") }, // Mensaje del diálogo
                confirmButton = {
                    Button(onClick = { locationPermissionState.launchPermissionRequest() }) { // Botón para solicitar permisos
                        Text("Conceder permisos") // Texto del botón
                    }
                }
            )
        }
    }
}

// Función para obtener la ubicación actual
private fun getCurrentLocation(context: Context): Location? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager // Obtener el servicio de ubicación

    // Verificar si se han concedido permisos
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        // Retornar la última ubicación conocida
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }
    return null // Retornar nulo si no hay permisos
}

// Función para obtener la dirección a partir de coordenadas
private fun obtenerDireccion(context: Context, latitude: Double, longitude: Double): String {
    return try {
        val geocoder = android.location.Geocoder(context) // Crear Geocoder
        val addresses = geocoder.getFromLocation(latitude, longitude, 1) // Obtener direcciones
        if (addresses?.isNotEmpty() == true) { // Verificar si hay direcciones
            with(addresses[0]) {
                buildString { // Construir una cadena con la dirección
                    append(getAddressLine(0))
                }
            }
        } else {
            "Lat: $latitude, Lon: $longitude" // Retornar coordenadas si no hay dirección
        }
    } catch (e: Exception) {
        "Lat: $latitude, Lon: $longitude" // Retornar coordenadas en caso de error
    }
}
