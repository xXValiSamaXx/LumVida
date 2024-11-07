package com.example.lumvida.ui.Inicio.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.lumvida.R
import com.example.lumvida.ui.theme.BackgroundContainer
import com.example.lumvida.ui.theme.Primary
import com.example.lumvida.ui.theme.PrimaryDark
import com.example.lumvida.ui.theme.TextPrimary

@Composable
fun InicioScreen(
    navController: NavController, // Controlador de navegación para cambiar de pantallas
    isDarkTheme: Boolean = isSystemInDarkTheme() // Determina si el tema oscuro está activado
) {
    // Configurar el fondo del contenedor de acuerdo al tema
    BackgroundContainer(isDarkTheme = isDarkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize() // Ocupa todo el espacio disponible
                .padding(16.dp), // Espaciado interior
            horizontalAlignment = Alignment.CenterHorizontally, // Alinear elementos horizontalmente al centro
            verticalArrangement = Arrangement.SpaceBetween // Espaciar elementos verticalmente
        ) {
            // Logo principal en la parte superior
            val logoImageRes = if (isDarkTheme) R.drawable.logo_blanco else R.drawable.logo_rojo // Seleccionar logo según el tema
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current) // Cargar la imagen de manera asíncrona
                        .data(logoImageRes) // Establecer el recurso de imagen
                        .build()
                ),
                contentDescription = "Logo LumViva", // Descripción del contenido para accesibilidad
                modifier = Modifier
                    .fillMaxWidth(0.7f) // El logo ocupará el 70% del ancho del contenedor
                    .padding(vertical = 32.dp), // Espaciado vertical para el logo
                contentScale = ContentScale.Fit // Ajustar la imagen al contenedor
            )

            // Espacio entre el logo y el siguiente elemento
            Spacer(modifier = Modifier.weight(1f))

            // Título de la aplicación
            Text(
                text = "LumViva", // Título de la aplicación
                style = MaterialTheme.typography.displayMedium, // Estilo de texto
                color = if (isDarkTheme) TextPrimary else PrimaryDark // Color del texto según el tema
            )

            // Espacio entre el título y el siguiente elemento
            Spacer(modifier = Modifier.weight(1f))

            // Descripción de la aplicación
            Text(
                text = "LumViva es una app que permite reportar problemas urbanos como baches o fallas en el alumbrado, directamente desde tu móvil. Conecta a los ciudadanos con las autoridades para soluciones rápidas y eficaces. ¡Haz tu ciudad mejor con LumViva!", // Descripción de la aplicación
                style = MaterialTheme.typography.bodyLarge, // Estilo de texto
                textAlign = TextAlign.Center, // Alinear el texto al centro
                modifier = Modifier.padding(vertical = 16.dp), // Espaciado vertical para la descripción
                color = if (isDarkTheme) TextPrimary else PrimaryDark // Color del texto según el tema
            )

            // Botón para continuar
            Button(
                onClick = {
                    navController.navigate("reportes") { // Navegar a la pantalla de reportes
                        popUpTo("inicio") { inclusive = true } // Limpiar la pila de navegación hasta "inicio"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth() // Ocupa todo el ancho disponible
                    .padding(top = 16.dp), // Espaciado superior
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary, // Color de fondo del botón
                    contentColor = TextPrimary // Color del texto del botón
                )
            ) {
                Text(
                    "Continuar", // Texto del botón
                    style = MaterialTheme.typography.labelLarge // Estilo de texto
                )
            }
        }
    }
}
