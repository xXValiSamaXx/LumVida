package com.example.lumvida.ui.PerfilUsuario // Define el paquete donde se encuentra esta clase.

import androidx.compose.foundation.isSystemInDarkTheme // Importa la función para detectar si el sistema está en modo oscuro.
import androidx.compose.foundation.layout.* // Importa funciones de diseño para crear layouts.
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.* // Importa la biblioteca Material 3 para usar componentes de UI.
import androidx.compose.runtime.* // Importa funciones de composición y estado de Jetpack Compose.
import androidx.compose.ui.Alignment // Importa la clase para alinear elementos en un contenedor.
import androidx.compose.ui.Modifier // Importa la clase Modifier para modificar la apariencia de los elementos.
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color // Importa la clase Color para usar colores.
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp // Importa la unidad dp para definir tamaños.
import androidx.navigation.NavController // Importa la clase NavController para manejar la navegación.
import com.example.lumvida.ui.theme.* // Importa temas personalizados.

@OptIn(ExperimentalMaterial3Api::class) // Anota que se están utilizando APIs experimentales de Material 3.
@Composable // Indica que esta función es un Composable.
fun PerfilUsuarioScreen(
    navController: NavController, // Recibe el NavController para la navegación.
    viewModel: PerfilUsuarioViewModel, // Recibe el ViewModel para gestionar el estado de la pantalla.
    isDarkTheme: Boolean = isSystemInDarkTheme() // Determina si el tema es oscuro.
) {
    val userName by viewModel.userName.collectAsState() // Recolecta el nombre del usuario desde el ViewModel.
    val userEmail by viewModel.userEmail.collectAsState() // Recolecta el correo electrónico del usuario desde el ViewModel.
    val userPhone by viewModel.userPhone.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var editedName by remember { mutableStateOf("") }
    var editedPhone by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current


    // Actualizar los valores editables cuando cambian los valores originales
    LaunchedEffect(userName, userPhone) {
        editedName = userName
        editedPhone = userPhone
    }

    // Contenedor de fondo que ajusta el tema.
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
                    // Campo Nombre
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

                    // Campo Email (siempre deshabilitado)
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

                    // Campo Teléfono
                    OutlinedTextField(
                        value = if (isEditing) editedPhone else userPhone,
                        onValueChange = { newValue ->
                            // Solo permite números y limita a 10 dígitos
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
        }
    }
}
