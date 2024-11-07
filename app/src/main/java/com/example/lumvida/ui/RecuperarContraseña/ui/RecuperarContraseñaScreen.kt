package com.example.lumvida.ui.RecuperarContraseña.ui // Define el paquete donde se encuentra esta clase.

import androidx.compose.foundation.isSystemInDarkTheme // Importa la función para verificar si el tema actual es oscuro.
import androidx.compose.foundation.layout.* // Importa funciones para crear layouts flexibles.
import androidx.compose.material.icons.Icons // Importa el objeto Icons para utilizar íconos.
import androidx.compose.material.icons.filled.ArrowBack // Importa el ícono de flecha hacia atrás.
import androidx.compose.material3.* // Importa componentes de Material Design 3.
import androidx.compose.runtime.* // Importa funciones para el estado y el ciclo de vida de Composables.
import androidx.compose.ui.Alignment // Importa la clase para alinear elementos en el layout.
import androidx.compose.ui.Modifier // Importa la clase Modifier para modificar los componentes.
import androidx.compose.ui.graphics.Color // Importa la clase Color para manejar colores.
import androidx.compose.ui.text.style.TextAlign // Importa TextAlign para alinear el texto.
import androidx.compose.ui.unit.dp // Importa dp para definir dimensiones.
import androidx.lifecycle.viewmodel.compose.viewModel // Importa la función viewModel para obtener instancias de ViewModel.
import androidx.navigation.NavController // Importa NavController para manejar la navegación.
import com.example.lumvida.ui.RecuperarContrasena.ui.RecuperarContraseñaViewModel // Importa el ViewModel para recuperar contraseña.
import com.example.lumvida.ui.RecuperarContrasena.ui.RecuperarContraseñaState // Importa el estado del proceso de recuperación de contraseña.
import com.example.lumvida.ui.Auth.ui.AuthViewModel // Importa el ViewModel de autenticación.
import com.example.lumvida.ui.theme.* // Importa los temas definidos en la aplicación.
import kotlinx.coroutines.launch // Importa la función para lanzar corutinas.

@OptIn(ExperimentalMaterial3Api::class) // Indica que se va a utilizar API experimental de Material3.
@Composable
fun RecuperarContraseñaScreen(
    navController: NavController, // Controlador de navegación para manejar la navegación.
    authViewModel: AuthViewModel, // ViewModel para autenticación.
    viewModel: RecuperarContraseñaViewModel = viewModel( // Obtiene una instancia del ViewModel para recuperación de contraseña.
        factory = RecuperarContraseñaViewModel.Factory(authViewModel) // Usa una factoría para crear el ViewModel.
    ),
    isDarkTheme: Boolean = isSystemInDarkTheme() // Verifica si el tema actual es oscuro.
) {
    var email by remember { mutableStateOf("") } // Crea un estado mutable para almacenar el correo electrónico ingresado.
    val state by viewModel.state.collectAsState() // Observa el estado del ViewModel.
    val snackbarHostState = remember { SnackbarHostState() } // Crea un estado para mostrar mensajes tipo snackbar.
    val scope = rememberCoroutineScope() // Crea un scope para lanzar corutinas.
    var showSuccessDialog by remember { mutableStateOf(false) } // Estado para controlar la visibilidad del diálogo de éxito.

    LaunchedEffect(state) { // Efecto lanzado que se activa cuando cambia el estado.
        if (state is RecuperarContraseñaState.Success) { // Si el estado es de éxito:
            showSuccessDialog = true // Muestra el diálogo de éxito.
        }
    }

    if (showSuccessDialog) { // Si se debe mostrar el diálogo de éxito:
        AlertDialog(
            onDismissRequest = { // Acción al cerrar el diálogo.
                showSuccessDialog = false // Oculta el diálogo.
                navController.navigateUp() // Regresa a la pantalla anterior.
            },
            title = { Text( // Título del diálogo.
                "Correo enviado",
                style = MaterialTheme.typography.titleLarge,
                color = if (isDarkTheme) TextPrimary else PrimaryDark // Cambia el color según el tema.
            ) },
            text = { // Contenido del diálogo.
                Text(
                    "Se han enviado las instrucciones de recuperación a tu correo electrónico. " +
                            "Por favor, revisa tu bandeja de entrada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )
            },
            confirmButton = { // Botón de confirmación en el diálogo.
                Button(
                    onClick = { // Acción al hacer clic en el botón.
                        showSuccessDialog = false // Oculta el diálogo.
                        navController.navigateUp() // Regresa a la pantalla anterior.
                    },
                    colors = ButtonDefaults.buttonColors( // Colores del botón.
                        containerColor = Primary,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("Aceptar") // Texto del botón.
                }
            },
            containerColor = if (isDarkTheme) PrimaryDark else Color.White // Color del contenedor del diálogo según el tema.
        )
    }

    BackgroundContainer(isDarkTheme = isDarkTheme) { // Composable para el fondo de la pantalla.
        Scaffold( // Estructura básica para la pantalla.
            containerColor = Color.Transparent, // Color de fondo del Scaffold.
            contentColor = if (isDarkTheme) TextPrimary else PrimaryDark, // Color del contenido según el tema.
            snackbarHost = { SnackbarHost(snackbarHostState) }, // Host para mostrar el snackbar.
            topBar = { // Barra superior de la pantalla.
                TopAppBar(
                    title = { // Título de la barra.
                        Text(
                            "Recuperar contraseña",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (isDarkTheme) TextPrimary else PrimaryDark
                        )
                    },
                    navigationIcon = { // Ícono de navegación (flecha hacia atrás).
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                Icons.Default.ArrowBack, // Ícono de flecha hacia atrás.
                                "Regresar", // Descripción del ícono.
                                tint = if (isDarkTheme) TextPrimary else PrimaryDark // Color del ícono según el tema.
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors( // Colores de la barra superior.
                        containerColor = Color.Transparent,
                        titleContentColor = if (isDarkTheme) TextPrimary else PrimaryDark
                    )
                )
            }
        ) { padding -> // Espacio para el contenido del Scaffold.
            Column( // Contenedor vertical para los elementos de la pantalla.
                modifier = Modifier
                    .fillMaxSize() // Llenar todo el espacio disponible.
                    .padding(padding) // Aplicar el padding del Scaffold.
                    .padding(16.dp), // Padding adicional.
                horizontalAlignment = Alignment.CenterHorizontally, // Alinear horizontalmente al centro.
                verticalArrangement = Arrangement.spacedBy(16.dp) // Espaciado vertical entre elementos.
            ) {
                Text( // Texto de instrucciones.
                    text = "Ingresa tu correo electrónico y te enviaremos las instrucciones para restablecer tu contraseña.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center, // Alinear texto al centro.
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )

                OutlinedTextField( // Campo de texto para ingresar el correo electrónico.
                    value = email, // Valor actual del campo.
                    onValueChange = { email = it.trim() }, // Acción al cambiar el valor.
                    label = { Text("Correo electrónico", color = if (isDarkTheme) TextPrimary else PrimaryDark) }, // Etiqueta del campo.
                    modifier = Modifier.fillMaxWidth(), // Ocupa todo el ancho disponible.
                    enabled = state !is RecuperarContraseñaState.Loading, // Habilita/deshabilita según el estado.
                    singleLine = true, // Permite solo una línea.
                    colors = OutlinedTextFieldDefaults.colors( // Colores del campo de texto.
                        focusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark,
                        unfocusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Secondary,
                        disabledTextColor = if (isDarkTheme) TextPrimary.copy(alpha = 0.6f) else PrimaryDark.copy(alpha = 0.6f)
                    )
                )

                Button( // Botón para enviar las instrucciones.
                    onClick = { viewModel.recuperarContraseña(email) }, // Acción al hacer clic en el botón.
                    modifier = Modifier.fillMaxWidth(), // Ocupa todo el ancho disponible.
                    enabled = email.isNotEmpty() && state !is RecuperarContraseñaState.Loading, // Habilita/deshabilita según el estado.
                    colors = ButtonDefaults.buttonColors( // Colores del botón.
                        containerColor = Primary,
                        contentColor = TextPrimary,
                        disabledContainerColor = Primary.copy(alpha = 0.6f)
                    )
                ) {
                    if (state is RecuperarContraseñaState.Loading) { // Si está en estado de carga:
                        CircularProgressIndicator( // Muestra un indicador de carga.
                            modifier = Modifier.size(24.dp), // Tamaño del indicador.
                            color = TextPrimary
                        )
                    } else { // Si no está en carga:
                        Text("Enviar instrucciones") // Texto del botón.
                    }
                }

                LaunchedEffect(state) { // Efecto lanzado que se activa cuando cambia el estado.
                    if (state is RecuperarContraseñaState.Error) { // Si hay un error en el estado:
                        scope.launch { // Lanza una corutina para mostrar el snackbar.
                            snackbarHostState.showSnackbar(
                                message = (state as RecuperarContraseñaState.Error).message // Muestra el mensaje de error.
                            )
                        }
                    }
                }
            }
        }
    }
}
