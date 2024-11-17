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
import com.example.lumvida.R
import com.example.lumvida.network.RetrofitClient
import com.example.lumvida.ui.Categorias.ui.CategoriasViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onLocationSelected: (GeoPoint, String) -> Unit,
    onDismiss: () -> Unit,
    initialLocation: GeoPoint,
    categoriasViewModel: CategoriasViewModel,
    onNavigate: (String) -> Unit = {}
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

    // Función para buscar sugerencias usando Nominatim
    fun getSuggestions(query: String) {
        if (query.length >= 3) {
            scope.launch {
                try {
                    isSearching = true
                    val results = RetrofitClient.nominatimService.searchLocation(
                        query = "$query, Chetumal, Quintana Roo"
                    )
                    searchSuggestions = results.map { it.display_name }
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
                    mapView?.controller?.setZoom(18.0)
                    showSuggestions = false
                }
            } catch (e: Exception) {
                Log.e("MapScreen", "Error searching location", e)
            } finally {
                isSearching = false
            }
        }
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

                    setBuiltInZoomControls(false)
                    setMultiTouchControls(true)

                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                    locationOverlay.enableMyLocation()
                    locationOverlay.enableFollowLocation()
                    overlays.add(locationOverlay)

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
                        categoriasViewModel.categorias.forEach { categoria ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = categoria.icono,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = categoria.titulo,
                                            color = Color.White,
                                            textAlign = TextAlign.Start
                                        )
                                    }
                                },
                                onClick = {
                                    showCategoriesMenu = false
                                    onNavigate("reports/${categoria.titulo}")
                                },
                                modifier = Modifier.fillMaxWidth()
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onNavigate("me") }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person),
                        contentDescription = "Me",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Yo", color = Color.White)
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
                    onClick = { onSearchQueryChange("") }
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