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
// Composable que representa la pantalla para crear un reporte.
@Composable
fun CrearReporteScreen(
    navController: NavController,           // Controlador de navegación para la pantalla
    viewModel: CrearReporteViewModel,        // ViewModel para gestionar la lógica del reporte
    authViewModel: AuthViewModel,            // ViewModel de autenticación
    categoria: String = "nombre de la selección" // Categoría del reporte, con valor predeterminado
) {
    val context = LocalContext.current // Obtener el contexto actual
    val systemUiController = rememberSystemUiController() // Controlador para UI del sistema
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) } // URI de la foto capturada

    // Permisos de cámara
    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA // Estado del permiso de cámara
    )

    // Preparar URI para la foto
    val getNewPhotoUri = {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider", // Proveedor de archivos
            File(
                context.getExternalFilesDir(null), // Directorio para almacenar la foto
                "photo_${System.currentTimeMillis()}.jpg" // Nombre de archivo con timestamp
            )
        )
    }

    // Launcher de la cámara actualizado
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture() // Lanzador para tomar foto
    ) { success -> // Callback cuando se toma la foto
        if (success) {
            currentPhotoUri?.let { uri -> // Si la foto fue capturada
                viewModel.onPhotoCaptured(uri) // Enviar URI de la foto al ViewModel
            }
        }
    }

    // Función para manejar la captura de foto
    val handlePhotoCapture = {
        try {
            currentPhotoUri = getNewPhotoUri() // Obtener nuevo URI para la foto
            currentPhotoUri?.let { uri -> // Si el URI no es nulo
                cameraLauncher.launch(uri) // Lanzar la cámara
            }
        } catch (e: Exception) {
            e.printStackTrace() // Manejo de excepciones
        }
    }

    // Función para obtener artículo basado en la categoría
    @Composable
    fun getArticulo(categoria: String): String {
        return when (categoria.lowercase()) {
            "luminaria" -> "la " // Ajuste de artículo para categoría "luminaria"
            else -> "el " // Por defecto
        }
    }

    // Configuración del color de la barra de estado
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true // Iconos oscuros en la barra de estado
        )
    }

    // Estructura principal de la pantalla
    Column(
        modifier = Modifier
            .fillMaxSize() // Llenar el espacio disponible
            .background(Color.White) // Fondo blanco
            .statusBarsPadding() // Padding para la barra de estado
            .verticalScroll(rememberScrollState()) // Habilitar scroll vertical
    ) {
        // Encabezado
        Column(
            modifier = Modifier
                .fillMaxWidth() // Ancho completo
                .background(Color(0xFF8B0000)) // Fondo de color rojo oscuro
                .padding(vertical = 16.dp), // Padding vertical
            horizontalAlignment = Alignment.CenterHorizontally // Alinear al centro
        ) {
            Text(
                text = "Reportar $categoria", // Título de la pantalla
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize() // Llenar el espacio disponible
                .padding(horizontal = 16.dp) // Padding horizontal
                .padding(top = 16.dp), // Padding superior
            verticalArrangement = Arrangement.spacedBy(8.dp) // Espaciado vertical
        ) {
            // Sección de Foto
            Text(
                text = "Adjuntar evidencia del reporte", // Instrucción para el usuario
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Foto",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp) // Padding superior
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth() // Ancho completo
                    .height(200.dp) // Altura fija
                    .clip(RoundedCornerShape(8.dp)) // Bordes redondeados
                    .background(Color(0xFFE0E0E0)) // Fondo gris claro
                    .clickable { // Manejo del clic para capturar foto
                        if (cameraPermissionState.status.isGranted) {
                            handlePhotoCapture() // Capturar foto si el permiso está concedido
                        } else {
                            cameraPermissionState.launchPermissionRequest() // Solicitar permiso
                        }
                    }
            ) {
                // Mostrar foto capturada si existe
                if (viewModel.hasPhoto && viewModel.photoUri != null) {
                    AsyncImage(
                        model = viewModel.photoUri, // URI de la foto
                        contentDescription = "Foto capturada", // Descripción de contenido
                        modifier = Modifier.fillMaxSize(), // Llenar el espacio
                        contentScale = ContentScale.Crop // Escalar la imagen
                    )
                } else {
                    // Dibujo de líneas cruzadas si no hay foto
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawLine(
                            color = Color.Gray,
                            start = Offset(0f, 0f), // Inicio de la línea
                            end = Offset(size.width, size.height), // Fin de la línea
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color.Gray,
                            start = Offset(size.width, 0f), // Línea cruzada
                            end = Offset(0f, size.height),
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Sección de Ubicación
            Text(
                text = "¿Dónde se encuentra ubicado ${getArticulo(categoria)}${categoria.lowercase()}?", // Pregunta sobre la ubicación
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp) // Padding superior
            )
            Text(
                text = "Dirección",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp) // Padding superior
            )
            OutlinedTextField(
                value = viewModel.direccion, // Dirección del ViewModel
                onValueChange = { /* No permitimos cambios manuales */ }, // Sin cambios manuales
                modifier = Modifier.fillMaxWidth(), // Ancho completo
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color(0xFF8B0000) // Color del borde al tener el foco
                ),
                placeholder = {
                    Text("Ubicación del maps ejemplo Othon P.Blanco 123, Bosque, Ciudad chetumal Q.ROO") // Texto de ayuda
                },
                readOnly = true, // Campo de solo lectura
                shape = RoundedCornerShape(8.dp) // Bordes redondeados
            )

            // Botón para buscar ubicación en el mapa
            Button(
                onClick = { viewModel.onMapClick() }, // Acción al hacer clic
                modifier = Modifier
                    .fillMaxWidth() // Ancho completo
                    .padding(vertical = 8.dp), // Padding vertical
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B0000) // Color del fondo del botón
                ),
                shape = RoundedCornerShape(24.dp) // Bordes redondeados
            ) {
                Text(
                    text = "Buscar ubicación en el mapa", // Texto del botón
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp) // Padding vertical
                )
            }

            // Sección de Comentario
            Text(
                text = "¿Por qué deseas reportar?", // Pregunta para el comentario
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp) // Padding superior
            )
            Text(
                text = "Comentario",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp) // Padding superior
            )
            OutlinedTextField(
                value = viewModel.comentario, // Comentario del ViewModel
                onValueChange = { viewModel.onComentarioChange(it) }, // Cambiar comentario en el ViewModel
                modifier = Modifier
                    .fillMaxWidth() // Ancho completo
                    .height(100.dp), // Altura fija
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color(0xFF8B0000) // Color del borde al tener el foco
                ),
                placeholder = {
                    Text("Describe el reporte a detalle del porque estas reportando.") // Texto de ayuda
                },
                shape = RoundedCornerShape(8.dp) // Bordes redondeados
            )

            // Botón de enviar reporte
            Button(
                onClick = {
                    viewModel.sendReport(categoria, authViewModel) // Enviar reporte al ViewModel
                },
                modifier = Modifier
                    .fillMaxWidth() // Ancho completo
                    .padding(vertical = 16.dp), // Padding vertical
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B0000) // Color del fondo del botón
                ),
                shape = RoundedCornerShape(24.dp), // Bordes redondeados
                enabled = !viewModel.isLoading // Deshabilitar botón si se está cargando
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White, // Indicador de progreso blanco
                        modifier = Modifier.size(24.dp) // Tamaño del indicador
                    )
                } else {
                    Text(
                        text = "Enviar reporte", // Texto del botón
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp) // Padding vertical
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // Espacio en blanco al final
        }
    }

    // Diálogos de error
    if (viewModel.showErrorDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissErrorDialog() }, // Acción al cerrar el diálogo
            title = { Text("Atención") }, // Título del diálogo
            text = {
                Text(
                    text = viewModel.errorMessage ?: "", // Mensaje de error
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissErrorDialog() }) { // Botón para aceptar el error
                    Text("Aceptar")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface, // Color de fondo del diálogo
            titleContentColor = MaterialTheme.colorScheme.onSurface, // Color del título
            textContentColor = MaterialTheme.colorScheme.onSurface // Color del texto
        )
    }

    // Diálogo de éxito
    if (viewModel.reporteSent) {
        AlertDialog(
            onDismissRequest = { }, // No hay acción al cerrar
            title = {
                Text(
                    "¡Éxito!", // Título del diálogo
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    "Tu reporte ha sido enviado correctamente", // Mensaje de éxito
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = { }, // Sin botón de confirmación
            containerColor = MaterialTheme.colorScheme.surface, // Color de fondo del diálogo
            titleContentColor = MaterialTheme.colorScheme.onSurface, // Color del título
            textContentColor = MaterialTheme.colorScheme.onSurface // Color del texto
        )

        LaunchedEffect(Unit) { // Efecto que se lanza al enviar el reporte
            delay(1500L) // Esperar 1.5 segundos
            navController.navigate("categorias") { // Navegar a la pantalla de categorías
                popUpTo("categorias") { inclusive = true } // Limpiar la pila de navegación
            }
        }
    }

    // Diálogo del mapa
    if (viewModel.showMap) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissMap() }, // Acción al cerrar el diálogo del mapa
            properties = DialogProperties(
                usePlatformDefaultWidth = false // No usar el ancho predeterminado de la plataforma
            ),
            modifier = Modifier
                .fillMaxSize() // Llenar el espacio
                .padding(16.dp), // Padding del diálogo
            content = {
                MapScreen(
                    onLocationSelected = { point, address -> // Acción al seleccionar una ubicación
                        viewModel.onLocationSelected(point, address) // Enviar ubicación al ViewModel
                    },
                    onDismiss = { viewModel.onDismissMap() }, // Acción al cerrar el mapa
                    initialLocation = viewModel.getChetumalCenter() // Ubicación inicial del mapa
                )
            }
        )
    }
}
