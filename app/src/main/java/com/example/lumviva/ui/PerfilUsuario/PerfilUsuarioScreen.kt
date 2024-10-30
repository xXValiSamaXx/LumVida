package com.example.lumviva.ui.PerfilUsuario // Define el paquete donde se encuentra esta clase.

import androidx.compose.foundation.isSystemInDarkTheme // Importa la función para detectar si el sistema está en modo oscuro.
import androidx.compose.foundation.layout.* // Importa funciones de diseño para crear layouts.
import androidx.compose.material3.* // Importa la biblioteca Material 3 para usar componentes de UI.
import androidx.compose.runtime.* // Importa funciones de composición y estado de Jetpack Compose.
import androidx.compose.ui.Alignment // Importa la clase para alinear elementos en un contenedor.
import androidx.compose.ui.Modifier // Importa la clase Modifier para modificar la apariencia de los elementos.
import androidx.compose.ui.graphics.Color // Importa la clase Color para usar colores.
import androidx.compose.ui.text.style.TextAlign // Importa la clase para alinear texto.
import androidx.compose.ui.unit.dp // Importa la unidad dp para definir tamaños.
import androidx.navigation.NavController // Importa la clase NavController para manejar la navegación.
import com.example.lumviva.ui.theme.* // Importa temas personalizados.

@OptIn(ExperimentalMaterial3Api::class) // Anota que se están utilizando APIs experimentales de Material 3.
@Composable // Indica que esta función es un Composable.
fun PerfilUsuarioScreen(
    navController: NavController, // Recibe el NavController para la navegación.
    viewModel: PerfilUsuarioViewModel, // Recibe el ViewModel para gestionar el estado de la pantalla.
    isDarkTheme: Boolean = isSystemInDarkTheme() // Determina si el tema es oscuro.
) {
    val userName by viewModel.userName.collectAsState() // Recolecta el nombre del usuario desde el ViewModel.
    val userEmail by viewModel.userEmail.collectAsState() // Recolecta el correo electrónico del usuario desde el ViewModel.

    // Contenedor de fondo que ajusta el tema.
    BackgroundContainer(isDarkTheme = isDarkTheme) {
        Column( // Crea una columna para apilar elementos verticalmente.
            modifier = Modifier
                .fillMaxSize() // Hace que la columna llene todo el tamaño disponible.
                .padding(16.dp), // Aplica un relleno de 16 dp.
            horizontalAlignment = Alignment.CenterHorizontally // Alinea los elementos horizontalmente al centro.
        ) {
            TopAppBar( // Crea una barra de navegación en la parte superior.
                title = { // Define el título de la barra.
                    Text( // Muestra el texto del título.
                        text = "Perfil de Usuario", // Texto a mostrar.
                        style = MaterialTheme.typography.titleLarge, // Aplica un estilo de título grande.
                        color = if (isDarkTheme) TextPrimary else PrimaryDark // Cambia el color según el tema.
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors( // Establece los colores de la TopAppBar.
                    containerColor = Color.Transparent // Hace que el fondo sea transparente.
                )
            )

            Spacer(modifier = Modifier.height(32.dp)) // Espaciador vertical de 32 dp.

            // Información del usuario
            Card( // Crea una tarjeta para mostrar información del usuario.
                modifier = Modifier
                    .fillMaxWidth() // Hace que la tarjeta llene todo el ancho disponible.
                    .padding(16.dp), // Aplica un relleno de 16 dp a la tarjeta.
                colors = CardDefaults.cardColors( // Establece los colores de la tarjeta.
                    containerColor = if (isDarkTheme) PrimaryDark else Color.White // Cambia el color de fondo según el tema.
                )
            ) {
                Column( // Crea otra columna dentro de la tarjeta.
                    modifier = Modifier
                        .padding(16.dp) // Aplica un relleno de 16 dp.
                        .fillMaxWidth(), // Hace que la columna llene todo el ancho disponible.
                    horizontalAlignment = Alignment.CenterHorizontally // Alinea los elementos horizontalmente al centro.
                ) {
                    Text( // Muestra el nombre del usuario.
                        text = userName, // Texto a mostrar, que es el nombre del usuario.
                        style = MaterialTheme.typography.headlineMedium, // Aplica un estilo de encabezado mediano.
                        color = if (isDarkTheme) TextPrimary else PrimaryDark // Cambia el color según el tema.
                    )

                    Spacer(modifier = Modifier.height(8.dp)) // Espaciador vertical de 8 dp.

                    Text( // Muestra el correo electrónico del usuario.
                        text = userEmail, // Texto a mostrar, que es el correo del usuario.
                        style = MaterialTheme.typography.bodyLarge, // Aplica un estilo de cuerpo grande.
                        color = if (isDarkTheme) TextPrimary else PrimaryDark // Cambia el color según el tema.
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp)) // Espaciador vertical de 32 dp.

            // Botones de acciones
            Button( // Crea un botón para editar el perfil.
                onClick = { /* TODO: Implementar edición de perfil */ }, // Acción al hacer clic (pendiente de implementación).
                modifier = Modifier
                    .fillMaxWidth() // Hace que el botón llene todo el ancho disponible.
                    .height(48.dp), // Establece una altura de 48 dp.
                colors = ButtonDefaults.buttonColors( // Establece los colores del botón.
                    containerColor = Primary, // Color de fondo del botón.
                    contentColor = TextPrimary // Color del texto del botón.
                )
            ) {
                Text( // Texto del botón.
                    text = "Editar Perfil", // Texto a mostrar.
                    style = MaterialTheme.typography.labelLarge // Aplica un estilo de etiqueta grande.
                )
            }

            Spacer(modifier = Modifier.height(16.dp)) // Espaciador vertical de 16 dp.

            Button( // Crea un botón para cerrar sesión.
                onClick = { // Acción al hacer clic.
                    viewModel.logout() // Llama a la función de cierre de sesión en el ViewModel.
                    navController.navigate("inicio") { // Navega a la pantalla de inicio.
                        popUpTo(0) { inclusive = true } // Limpia la pila de navegación.
                    }
                },
                modifier = Modifier
                    .fillMaxWidth() // Hace que el botón llene todo el ancho disponible.
                    .height(48.dp), // Establece una altura de 48 dp.
                colors = ButtonDefaults.buttonColors( // Establece los colores del botón.
                    containerColor = Color.Red, // Usa Color.Red para el fondo del botón.
                    contentColor = Color.White // Color del texto del botón.
                )
            ) {
                Text( // Texto del botón.
                    text = "Cerrar Sesión", // Texto a mostrar.
                    style = MaterialTheme.typography.labelLarge // Aplica un estilo de etiqueta grande.
                )
            }
        }
    }
}
