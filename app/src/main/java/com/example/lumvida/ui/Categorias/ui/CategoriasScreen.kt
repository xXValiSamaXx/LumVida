package com.example.lumvida.ui.Categorias.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun OptionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Botón personalizado que ejecuta una acción al hacer clic
    Button(
        onClick = onClick, // Acción a ejecutar cuando se presiona el botón
        modifier = modifier
            .width(110.dp) // Define el ancho del botón
            .height(100.dp), // Define la altura del botón
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent // Hace que el fondo del botón sea transparente
        ),
        contentPadding = PaddingValues(4.dp) // Define el padding del contenido dentro del botón
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, // Alineación horizontal centrada
            verticalArrangement = Arrangement.Center // Alineación vertical centrada
        ) {
            // Ícono que se muestra en el botón
            Icon(
                imageVector = icon, // Ícono proporcionado como parámetro
                contentDescription = null, // Sin descripción para accesibilidad
                tint = Color.White, // Color del ícono en blanco
                modifier = Modifier.size(40.dp) // Tamaño del ícono
            )
            Spacer(modifier = Modifier.height(4.dp)) // Espacio entre el ícono y el texto
            // Texto que se muestra en el botón
            Text(
                text = text, // Texto proporcionado como parámetro
                color = Color.White, // Color del texto en blanco
                fontSize = 14.sp, // Tamaño de la fuente
                textAlign = TextAlign.Center, // Texto centrado
                maxLines = 2, // Máximo de líneas que puede ocupar el texto
                lineHeight = 16.sp // Altura de línea del texto
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CategoriasScreen(
    navController: NavController,
    viewModel: CategoriasViewModel
) {
    // Controlador de la UI del sistema para cambiar el color de la barra de estado
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White, // Color de la barra de estado en blanco
            darkIcons = true // Usa íconos oscuros en la barra de estado
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize() // Hace que la columna ocupe todo el tamaño disponible
            .background(Color(0xFF8B0000)) // Fondo rojo oscuro para la pantalla
    ) {
        Header(navController, viewModel) // Muestra el encabezado de la pantalla

        Box(
            modifier = Modifier
                .fillMaxSize() // Box ocupa todo el espacio disponible
                .padding(horizontal = 24.dp), // Espacio horizontal de 24.dp
            contentAlignment = Alignment.Center // Centra el contenido dentro del Box
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, // Alinea el contenido en el centro
                verticalArrangement = Arrangement.spacedBy(48.dp) // Espacio de 48.dp entre los elementos
            ) {
                // Título principal de la pantalla
                Text(
                    text = "¿Qué deseas reportar?", // Texto que se muestra
                    fontSize = 28.sp, // Tamaño de la fuente
                    fontWeight = FontWeight.Bold, // Texto en negrita
                    color = Color.White, // Texto de color blanco
                    textAlign = TextAlign.Center // Texto centrado
                )

                CategoriaGrid(navController, viewModel) // Muestra el grid de categorías
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CategoriaGrid(
    navController: NavController,
    viewModel: CategoriasViewModel
) {
    // Grid vertical que muestra una lista de categorías en un diseño de rejilla
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // 3 columnas en la rejilla
        horizontalArrangement = Arrangement.SpaceBetween, // Espacio entre columnas
        verticalArrangement = Arrangement.spacedBy(40.dp), // Espacio entre filas de 40.dp
        modifier = Modifier.fillMaxWidth() // El grid ocupa todo el ancho disponible
    ) {
        items(viewModel.categorias.size) { index -> // Recorre las categorías del ViewModel
            val categoria = viewModel.categorias[index] // Obtiene la categoría actual
            OptionButton(
                text = categoria.titulo, // Texto del botón de categoría
                icon = categoria.icono, // Ícono de la categoría
                onClick = {
                    // Navega a la pantalla de creación de reporte para la categoría seleccionada
                    viewModel.navigateToCrearReporte(navController, categoria)
                }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun Header(
    navController: NavController,
    viewModel: CategoriasViewModel
) {
    // Encabezado de la pantalla
    Column(
        modifier = Modifier
            .fillMaxWidth() // El encabezado ocupa todo el ancho disponible
            .background(Color.White) // Fondo blanco
            .padding(vertical = 16.dp) // Espaciado vertical de 16.dp
            .statusBarsPadding() // Alinea el contenido del encabezado debajo de la barra de estado
    ) {
        // Título del encabezado
        Text(
            text = "La Ciudad hoy", // Texto del título
            modifier = Modifier.fillMaxWidth(), // Ocupa todo el ancho
            textAlign = TextAlign.Center, // Centrado
            fontSize = 18.sp, // Tamaño de fuente
            color = Color.Black // Color negro para el texto
        )

        // Fecha formateada desde el ViewModel
        Text(
            text = viewModel.formattedDate, // Fecha proporcionada por el ViewModel
            modifier = Modifier.fillMaxWidth(), // Ocupa todo el ancho
            textAlign = TextAlign.Center, // Centrado
            fontSize = 16.sp, // Tamaño de fuente
            color = Color.Gray // Texto de color gris
        )
    }
}
