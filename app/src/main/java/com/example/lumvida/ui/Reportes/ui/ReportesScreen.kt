package com.example.lumvida.ui.Reportes.ui // Define el paquete donde se encuentra esta clase.

import androidx.compose.foundation.isSystemInDarkTheme // Importa la función para verificar si el sistema está en tema oscuro.
import androidx.compose.foundation.layout.* // Importa las funciones para el manejo de layouts.
import androidx.compose.material.icons.Icons // Importa la biblioteca de íconos.
import androidx.compose.material.icons.filled.Person // Importa el ícono de persona.
import androidx.compose.material3.* // Importa los componentes de Material 3.
import androidx.compose.runtime.* // Importa las funciones para el estado y composición.
import androidx.compose.ui.Alignment // Importa la clase para alinear elementos.
import androidx.compose.ui.Modifier // Importa la clase para modificar componentes.
import androidx.compose.ui.graphics.Color // Importa la clase para colores.
import androidx.compose.ui.text.style.TextAlign // Importa la clase para la alineación de texto.
import androidx.compose.ui.unit.dp // Importa la unidad de medida dp.
import androidx.lifecycle.viewmodel.compose.viewModel // Importa la función para obtener ViewModels en composables.
import androidx.navigation.NavController // Importa la clase para controlar la navegación.
import com.example.lumvida.ui.Auth.ui.AuthViewModel // Importa el ViewModel de autenticación.
import com.example.lumvida.ui.theme.* // Importa los temas definidos por el usuario.

@OptIn(ExperimentalMaterial3Api::class) // Indica que se utilizarán API experimentales de Material 3.
@Composable
fun ReportesScreen(
    navController: NavController, // Controlador de navegación.
    authViewModel: AuthViewModel, // ViewModel para manejar la autenticación.
    reportesViewModel: ReportesViewModel = viewModel( // Obtiene una instancia del ViewModel de reportes.
        factory = ReportesViewModel.Factory(authViewModel)
    ),
    isDarkTheme: Boolean = isSystemInDarkTheme() // Verifica si el tema actual es oscuro.
) {
    val userName by reportesViewModel.userName.collectAsState() // Colecciona el nombre del usuario.
    val isAuthenticated by reportesViewModel.isAuthenticated.collectAsState() // Colecciona el estado de autenticación.
    var showLoginDialog by remember { mutableStateOf(false) } // Estado para mostrar el diálogo de inicio de sesión.

    // Muestra un diálogo si se necesita iniciar sesión.
    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false }, // Al cerrar el diálogo, oculta la ventana.
            title = {
                Text(
                    "Iniciar sesión requerido",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark // Define el color según el tema.
                )
            },
            text = {
                Text(
                    "Para ver tus reportes necesitas iniciar sesión.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLoginDialog = false // Oculta el diálogo.
                        navController.navigate("login") // Navega a la pantalla de inicio de sesión.
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = TextPrimary // Define los colores del botón.
                    )
                ) {
                    Text(
                        "Iniciar sesión",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLoginDialog = false }, // Cierra el diálogo sin hacer nada.
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isDarkTheme) TextPrimary else PrimaryDark
                    )
                ) {
                    Text(
                        "Cancelar",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            containerColor = if (isDarkTheme) PrimaryDark else Color.White // Define el color del contenedor según el tema.
        )
    }

    // Contenedor de fondo con el tema adecuado.
    BackgroundContainer(isDarkTheme = isDarkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize() // Toma todo el espacio disponible.
                .padding(16.dp), // Agrega padding de 16dp.
            horizontalAlignment = Alignment.CenterHorizontally // Alinea los elementos en el centro horizontalmente.
        ) {
            // Header con TopAppBar
            TopAppBar(
                title = {
                    Text(
                        text = if (isAuthenticated) "Bienvenido, $userName" else "Bienvenido Invitado", // Mensaje de bienvenida basado en la autenticación.
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isDarkTheme) TextPrimary else PrimaryDark // Define el color según el tema.
                    )
                },
                actions = {
                    if (isAuthenticated) { // Si el usuario está autenticado, muestra el ícono de perfil.
                        IconButton(
                            onClick = { navController.navigate("usuario") } // Navega a la pantalla de usuario.
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Perfil de usuario", // Descripción del ícono.
                                tint = if (isDarkTheme) TextPrimary else PrimaryDark // Define el color del ícono según el tema.
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent // Define el color de fondo del TopAppBar como transparente.
                )
            )

            Spacer(modifier = Modifier.height(32.dp)) // Espaciador para separar visualmente.

            // Contenido principal
            Column(
                modifier = Modifier.weight(1f), // Permite que la columna ocupe el espacio restante.
                horizontalAlignment = Alignment.CenterHorizontally // Alinea los elementos en el centro horizontalmente.
            ) {
                Text(
                    text = "Haz un reporte", // Título principal de la sección.
                    style = MaterialTheme.typography.displaySmall,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark,
                    textAlign = TextAlign.Center // Centra el texto.
                )

                Spacer(modifier = Modifier.height(16.dp)) // Espaciador.

                Text(
                    text = "Si has presenciado o experimentado un problema urbano, como alumbrado público, drenajes obstruidos, mal estado de las calles o situaciones de riesgo, puedes informarnos sobre lo ocurrido.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark,
                    textAlign = TextAlign.Center // Centra el texto.
                )

                Spacer(modifier = Modifier.height(24.dp)) // Espaciador.

                Button(
                    onClick = { navController.navigate("categorias") }, // Navega a la pantalla de categorías.
                    modifier = Modifier
                        .fillMaxWidth() // Ocupa todo el ancho.
                        .height(48.dp), // Define la altura del botón.
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = TextPrimary // Define los colores del botón.
                    )
                ) {
                    Text(
                        text = "Hacer Reporte", // Texto en el botón.
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(40.dp)) // Espaciador.

                Text(
                    text = "Mis Reportes", // Título de la sección de reportes.
                    style = MaterialTheme.typography.displaySmall,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark,
                    textAlign = TextAlign.Center // Centra el texto.
                )

                Spacer(modifier = Modifier.height(16.dp)) // Espaciador.

                Text(
                    text = "Puedes ver y actualizar tus reportes en cualquier momento.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark,
                    textAlign = TextAlign.Center // Centra el texto.
                )

                Spacer(modifier = Modifier.height(24.dp)) // Espaciador.

                Button(
                    onClick = {
                        if (isAuthenticated) { // Si el usuario está autenticado, navega a "mis reportes".
                            navController.navigate("mis_reportes")
                        } else { // Si no está autenticado, muestra el diálogo de inicio de sesión.
                            showLoginDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth() // Ocupa todo el ancho.
                        .height(48.dp), // Define la altura del botón.
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = TextPrimary // Define los colores del botón.
                    )
                ) {
                    Text(
                        text = "Ver mis reportes", // Texto en el botón.
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
