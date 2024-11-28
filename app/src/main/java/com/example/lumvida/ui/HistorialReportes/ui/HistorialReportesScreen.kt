package com.example.lumvida.ui.HistorialReportes.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lumvida.ui.Auth.ui.AuthViewModel
import com.example.lumvida.ui.theme.BackgroundContainer
import com.example.lumvida.ui.theme.Primary
import com.example.lumvida.ui.theme.PrimaryDark
import com.example.lumvida.ui.theme.TextPrimary
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.clip
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialReportesScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    viewModel: HistorialReportesViewModel = viewModel(
        factory = HistorialReportesViewModel.Factory(authViewModel)
    )
) {
    val reportes by viewModel.reportes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()

    var selectedReporte by remember { mutableStateOf<HistorialReportesViewModel.Reporte?>(null) }

    BackgroundContainer(isDarkTheme = isDarkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .statusBarsPadding()
        ) {
            // Título centrado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Mis Reportes",
                    style = MaterialTheme.typography.headlineLarge,
                    color = if (isDarkTheme) TextPrimary else Primary,
                    textAlign = TextAlign.Center
                )
            }

            // Encabezados de la tabla
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (isDarkTheme) PrimaryDark.copy(alpha = 0.7f) else TextPrimary.copy(alpha = 0.9f),
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Folio",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        color = if (isDarkTheme) TextPrimary else Primary
                    )
                    Text(
                        text = "Fecha",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        color = if (isDarkTheme) TextPrimary else Primary
                    )
                    Text(
                        text = "Estado",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        color = if (isDarkTheme) TextPrimary else Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = if (isDarkTheme) TextPrimary else Primary)
                }
            } else if (error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else if (reportes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tienes reportes registrados",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = if (isDarkTheme) TextPrimary else Primary
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(reportes) { reporte ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReporte = reporte },
                            color = if (isDarkTheme)
                                PrimaryDark.copy(alpha = 0.5f)
                            else
                                TextPrimary.copy(alpha = 0.8f),
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = reporte.folio.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                    color = if (isDarkTheme) TextPrimary else Primary
                                )
                                Text(
                                    text = formatDate(reporte.fecha),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                    color = if (isDarkTheme) TextPrimary else Primary
                                )
                                Text(
                                    text = reporte.estado,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                    color = if (isDarkTheme) TextPrimary else Primary
                                )
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Modal de detalles
        ReporteDetalleDialog(
            reporte = selectedReporte,
            onDismiss = { selectedReporte = null }
        )
    }
}

@Composable
private fun ReporteImagen(foto: String) {
    var imageOrientation by remember { mutableStateOf<ImageOrientation?>(null) }

    // Efecto para determinar la orientación cuando el componente se monta
    LaunchedEffect(foto) {
        withContext(Dispatchers.IO) {
            try {
                val imageBytes = Base64.decode(
                    foto.substringAfter("base64,"),
                    Base64.DEFAULT
                )
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()

                imageOrientation = ImageOrientation(
                    width = bitmap.width,
                    height = bitmap.height,
                    isLandscape = bitmap.width > bitmap.height,
                    ratio = ratio
                )
            } catch (e: Exception) {
                Log.e("ReporteImagen", "Error decodificando imagen", e)
            }
        }
    }

    // Mostrar la imagen una vez que tenemos la orientación
    imageOrientation?.let { orientation ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (!orientation.isLandscape) 300.dp else 200.dp),
            contentAlignment = Alignment.Center
        ) {
            val imageBytes = Base64.decode(
                foto.substringAfter("base64,"),
                Base64.DEFAULT
            )
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Imagen del reporte",
                modifier = Modifier
                    .let {
                        if (!orientation.isLandscape) {
                            it.width(200.dp)
                                .height(300.dp)
                        } else {
                            it.fillMaxWidth()
                                .height(200.dp)
                        }
                    }
                    .clip(MaterialTheme.shapes.medium),
                contentScale = if (!orientation.isLandscape) ContentScale.Fit else ContentScale.Crop
            )
        }
    }
}

data class ImageOrientation(
    val width: Int,
    val height: Int,
    val isLandscape: Boolean,
    val ratio: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReporteDetalleDialog(
    reporte: HistorialReportesViewModel.Reporte?,
    onDismiss: () -> Unit
) {
    if (reporte != null) {
        BasicAlertDialog(
            onDismissRequest = onDismiss,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Detalles del Reporte",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Imagen con orientación automática
                    if (!reporte.foto.isNullOrEmpty()) {
                        ReporteImagen(foto = reporte.foto)
                    }

                    // Categoría
                    DetalleItem(
                        titulo = "Categoría",
                        contenido = reporte.categoria
                    )

                    // Ubicación
                    DetalleItem(
                        titulo = "Ubicación",
                        contenido = reporte.direccion
                    )

                    // Descripción
                    DetalleItem(
                        titulo = "Descripción",
                        contenido = reporte.comentario
                    )

                    // Botón de cerrar
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleItem(
    titulo: String,
    contenido: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = contenido,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatDate(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX"))
    return formatter.format(date)
}