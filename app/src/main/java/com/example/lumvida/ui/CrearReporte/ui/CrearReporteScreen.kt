package com.example.lumvida.ui.CrearReporte.ui

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lumvida.R
import com.example.lumvida.ui.Auth.ui.AuthViewModel
import com.example.lumvida.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import java.io.File
import androidx.core.content.FileProvider
import com.example.lumvida.ui.Categorias.ui.CategoriasViewModel
import com.example.lumvida.ui.Reportes.ui.MapaIncidenciasScreen
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CrearReporteScreen(
    navController: NavController,
    viewModel: CrearReporteViewModel,
    authViewModel: AuthViewModel,
    categoriasViewModel: CategoriasViewModel,
    categoria: String = "nombre de la selección",
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val context = LocalContext.current
    val systemUiController = rememberSystemUiController()
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePickerDialog by remember { mutableStateOf(false) }

    // Launcher para la galería
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onPhotoCaptured(it)
        }
    }

    // Preparar URI para la foto
    val getNewPhotoUri = {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(
                context.getExternalFilesDir(null),
                "photo_${System.currentTimeMillis()}.jpg"
            )
        )
    }

    // Launcher de la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                viewModel.onPhotoCaptured(uri)
            }
        }
    }

    // Función para manejar la captura de foto
    val handlePhotoCapture = {
        try {
            currentPhotoUri = getNewPhotoUri()
            currentPhotoUri?.let { uri ->
                cameraLauncher.launch(uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Diálogo de selección de imagen
    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = {
                Text(
                    "Seleccionar imagen",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )
            },
            text = {
                Column {
                    Button(
                        onClick = {
                            showImagePickerDialog = false
                            handlePhotoCapture()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text("Tomar foto con la cámara")
                    }

                    Button(
                        onClick = {
                            showImagePickerDialog = false
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text("Seleccionar de la galería")
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                TextButton(
                    onClick = { showImagePickerDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isDarkTheme) TextPrimary else PrimaryDark
                    )
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Función para obtener artículo basado en la categoría
    @Composable
    fun getArticulo(categoria: String): String {
        return when (categoria.lowercase()) {
            "bacheo" -> "el bache"
            "drenajes obstruidos" -> "el drenaje obstruido"
            "basura acumulada" -> "la "
            else -> "el "
        }
    }

    BackgroundContainer(isDarkTheme = isDarkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Encabezado
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDarkTheme) Primary else Primary)
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Reportar $categoria",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isDarkTheme) TextPrimary else TextPrimary,
                    textAlign = TextAlign.Center
                )
            }

            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Adjuntar evidencia del reporte",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )

                Text(
                    text = "Foto",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0E0E0))
                        .clickable { showImagePickerDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.hasPhoto && viewModel.photoUri != null) {
                        AsyncImage(
                            model = viewModel.photoUri,
                            contentDescription = "Foto capturada",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.iconocamara),
                            contentDescription = "Tomar foto",
                            modifier = Modifier.size(96.dp),
                            tint = if (isDarkTheme) TextPrimary else PrimaryDark
                        )
                    }
                }

                Text(
                    text = "¿Dónde se encuentra ubicado " +
                            (if (getArticulo(categoria).length > 3)
                                getArticulo(categoria)
                            else getArticulo(categoria) + categoria.lowercase()) + "?",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Text(
                    text = "Dirección",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark,
                    modifier = Modifier.padding(top = 8.dp)
                )

                OutlinedTextField(
                    value = viewModel.direccion,
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = if (isDarkTheme) Secondary else Secondary,
                        focusedBorderColor = if (isDarkTheme) Primary else Primary,
                        unfocusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark,
                        focusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark
                    ),
                    placeholder = {
                        Text(
                            "Ubicación del maps ejemplo Othon P.Blanco 123, Bosque, Ciudad chetumal Q.ROO",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isDarkTheme) TextPrimary else PrimaryDark
                            )
                        )
                    },
                    readOnly = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Button(
                    onClick = { viewModel.onMapClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Buscar ubicación en el mapa",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Text(
                    text = "¿Por qué deseas reportar?",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Text(
                    text = "Comentario",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark,
                    modifier = Modifier.padding(top = 8.dp)
                )

                OutlinedTextField(
                    value = viewModel.comentario,
                    onValueChange = { viewModel.onComentarioChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = if (isDarkTheme) Secondary else Secondary,
                        focusedBorderColor = if (isDarkTheme) Primary else Primary,
                        unfocusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark,
                        focusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark
                    ),
                    placeholder = {
                        Text(
                            "Describe el reporte a detalle del porque estas reportando.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isDarkTheme) TextPrimary else PrimaryDark
                            )
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                )

                Button(
                    onClick = { viewModel.sendReport(categoria, authViewModel, context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = TextPrimary,
                        disabledContainerColor = Secondary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            color = TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Enviar reporte",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Diálogos
    if (viewModel.showErrorDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissErrorDialog() },
            title = {
                Text(
                    "Atención",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )
            },
            text = {
                Text(
                    text = viewModel.errorMessage ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissErrorDialog() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = TextPrimary
                    )
                ) {
                    Text(
                        "Aceptar",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        )
    }

    if (viewModel.reporteSent) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    "¡Éxito!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )
            },
            text = {
                Text(
                    "Tu reporte ha sido enviado correctamente",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )
            },
            confirmButton = { }
        )

        LaunchedEffect(Unit) {
            delay(1500L)
            navController.navigate("categorias") {
                popUpTo("categorias") { inclusive = true }
            }
        }
    }

    if (viewModel.showMap) {
        MapaReporte(
            onLocationSelected = { point, address ->
                viewModel.onLocationSelected(point, address)
            },
            onDismiss = { viewModel.onDismissMap() }
        )
    }
}