package com.example.lumviva.ui.CrearReporte.ui

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lumviva.ui.Auth.ui.AuthViewModel
import com.example.lumviva.ui.Auth.ui.AuthState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CrearReporteScreen(
    navController: NavController,
    viewModel: CrearReporteViewModel,
    authViewModel: AuthViewModel,
    categoria: String = "nombre de la selección"
) {
    val context = LocalContext.current
    val systemUiController = rememberSystemUiController()
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Permisos de cámara
    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

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

    // Launcher de la cámara actualizado
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                viewModel.onPhotoCaptured(uri)
            }
        }
    }

    // Function to handle photo capture
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

    @Composable
    fun getArticulo(categoria: String): String {
        return when (categoria.lowercase()) {
            "luminaria" -> "la "
            else -> "el "
        }
    }

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF8B0000))
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Reportar $categoria",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sección de Foto
            Text(
                text = "Adjuntar evidencia del reporte",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Foto",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0))
                    .clickable {
                        if (cameraPermissionState.status.isGranted) {
                            handlePhotoCapture()
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    }
            ) {
                if (viewModel.hasPhoto && viewModel.photoUri != null) {
                    AsyncImage(
                        model = viewModel.photoUri,
                        contentDescription = "Foto capturada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawLine(
                            color = Color.Gray,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height),
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color.Gray,
                            start = Offset(size.width, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Sección de Ubicación
            Text(
                text = "¿Dónde se encuentra ubicado ${getArticulo(categoria)}${categoria.lowercase()}?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Dirección",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            OutlinedTextField(
                value = viewModel.direccion,
                onValueChange = { /* No permitimos cambios manuales */ },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color(0xFF8B0000)
                ),
                placeholder = {
                    Text("Ubicación del maps ejemplo Othon P.Blanco 123, Bosque, Ciudad chetumal Q.ROO")
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
                    containerColor = Color(0xFF8B0000)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "Buscar ubicación en el mapa",
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Sección de Comentario
            Text(
                text = "¿Por qué deseas reportar?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Comentario",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            OutlinedTextField(
                value = viewModel.comentario,
                onValueChange = { viewModel.onComentarioChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color(0xFF8B0000)
                ),
                placeholder = {
                    Text("Describe el reporte a detalle del porque estas reportando.")
                },
                shape = RoundedCornerShape(8.dp)
            )

            // Botón de enviar reporte
            Button(
                onClick = {
                    viewModel.sendReport(categoria, authViewModel)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B0000)
                ),
                shape = RoundedCornerShape(24.dp),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Enviar reporte",
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialogs
    if (viewModel.showErrorDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissErrorDialog() },
            title = { Text("Atención") },
            text = {
                Text(
                    text = viewModel.errorMessage ?: "",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissErrorDialog() }) {
                    Text("Aceptar")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
        )
    }

    if (viewModel.reporteSent) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    "¡Éxito!",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    "Tu reporte ha sido enviado correctamente",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = { },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
        )

        LaunchedEffect(Unit) {
            delay(1500L)
            navController.navigate("categorias") {
                popUpTo("categorias") { inclusive = true }
            }
        }
    }

    if (viewModel.showMap) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissMap() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            ),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            content = {
                MapScreen(
                    onLocationSelected = { point, address ->
                        viewModel.onLocationSelected(point, address)
                    },
                    onDismiss = { viewModel.onDismissMap() },
                    initialLocation = viewModel.getChetumalCenter()
                )
            }
        )
    }
}