package com.example.lumvida.ui.PerfilUsuario // Define el paquete donde se encuentra esta clase.

import androidx.compose.foundation.isSystemInDarkTheme // Importa la función para detectar si el sistema está en modo oscuro.
import androidx.compose.foundation.layout.* // Importa funciones de diseño para crear layouts.
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.* // Importa la biblioteca Material 3 para usar componentes de UI.
import androidx.compose.runtime.* // Importa funciones de composición y estado de Jetpack Compose.
import androidx.compose.ui.Alignment // Importa la clase para alinear elementos en un contenedor.
import androidx.compose.ui.Modifier // Importa la clase Modifier para modificar la apariencia de los elementos.
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color // Importa la clase Color para usar colores.
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp // Importa la unidad dp para definir tamaños.
import androidx.navigation.NavController // Importa la clase NavController para manejar la navegación.
import com.example.lumvida.ui.components.MapView
import com.example.lumvida.ui.theme.* // Importa temas personalizados.
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioScreen(
    navController: NavController,
    viewModel: PerfilUsuarioViewModel,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userPhone by viewModel.userPhone.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isDownloadingMap by viewModel.isDownloadingMap.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    var showOfflineMapDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedPhone by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(userName, userPhone) {
        editedName = userName
        editedPhone = userPhone
    }

    BackgroundContainer(isDarkTheme = isDarkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Perfil de Usuario",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isDarkTheme) TextPrimary else PrimaryDark,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Mi Perfil",
                style = MaterialTheme.typography.headlineMedium,
                color = if (isDarkTheme) TextPrimary else PrimaryDark,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) PrimaryDark else Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = if (isEditing) editedName else userName,
                        onValueChange = { editedName = it },
                        label = { Text("Nombre") },
                        enabled = isEditing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = if (isDarkTheme) TextPrimary else PrimaryDark
                        )
                    )

                    OutlinedTextField(
                        value = userEmail,
                        onValueChange = { },
                        label = { Text("Correo electrónico") },
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = if (isDarkTheme) TextPrimary else PrimaryDark
                        )
                    )

                    OutlinedTextField(
                        value = if (isEditing) editedPhone else userPhone,
                        onValueChange = { newValue ->
                            if (newValue.length <= 10) {
                                editedPhone = newValue.filter { it.isDigit() }
                            }
                        },
                        label = { Text("Teléfono (10 dígitos)") },
                        enabled = isEditing,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                keyboardController?.hide()
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = if (isDarkTheme) TextPrimary else PrimaryDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isEditing) {
                        viewModel.updateUserData(editedName, editedPhone)
                    } else {
                        viewModel.toggleEditing()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = TextPrimary
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TextPrimary
                    )
                } else {
                    Text(
                        text = if (isEditing) "Guardar cambios" else "Editar Perfil",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.logout()
                    navController.navigate("inicio") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Cerrar Sesión",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showOfflineMapDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isDarkTheme) TextPrimary else PrimaryDark
                )
            ) {
                val context = LocalContext.current  // Obtener el contexto aquí

                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Mapa Offline",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Descargar mapa offline",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }

    if (showOfflineMapDialog) {
        OfflineMapDialog(
            onDismiss = { showOfflineMapDialog = false },
            onConfirm = {
                viewModel.downloadOfflineMap()
                showOfflineMapDialog = false
            },
            isDarkTheme = isDarkTheme,
            isDownloading = isDownloadingMap,
            progress = downloadProgress,
            cacheSize = viewModel.getCacheSize()
        )
    }
}


@Composable
private fun OfflineMapDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isDarkTheme: Boolean,
    isDownloading: Boolean,
    progress: Int?,
    cacheSize: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Descargar mapa offline",
                style = MaterialTheme.typography.titleLarge,
                color = if (isDarkTheme) TextPrimary else PrimaryDark
            )
        },
        text = {
            Column {
                Text(
                    text = "Se descargará el mapa del área cercana a tu ubicación actual (10km a la redonda) para uso sin conexión. " +
                            "Esto puede consumir varios MB de almacenamiento.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tamaño actual en caché: $cacheSize",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )

                if (isDownloading && progress != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = progress / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Descargando: $progress%",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDownloading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = TextPrimary
                )
            ) {
                Text(if (isDownloading) "Descargando..." else "Descargar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDownloading
            ) {
                Text(
                    "Cancelar",
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )
            }
        },
        containerColor = if (isDarkTheme) PrimaryDark else Color.White
    )
}
