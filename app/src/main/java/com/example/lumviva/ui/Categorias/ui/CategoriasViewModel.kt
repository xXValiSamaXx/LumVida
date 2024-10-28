package com.example.lumviva.ui.Categorias.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class CategoriasViewModel : ViewModel() {

    // Data class para representar una categoría
    data class Categoria(
        val titulo: String,
        val icono: ImageVector,
        val ruta: String
    )

    // Lista de categorías disponibles
    private val _categorias = listOf(
        Categoria("Basurero clandestino", Icons.Default.Delete, "basurero_clandestino"),
        Categoria("Bache", Icons.Default.Warning, "baches"),
        Categoria("Semáforo", Icons.Default.Circle, "semaforos"),
        Categoria("Luminaria", Icons.Default.Lightbulb, "luminaria"),
        Categoria("Parque", Icons.Default.Park, "parque"),
        Categoria("Alcantarillado", Icons.Default.Water, "alcantarillado")
    )

    // Exponer las categorías como un estado público
    var categorias by mutableStateOf(_categorias)
        private set

    // Estado para la fecha formateada
    private val currentDate = LocalDate.now(ZoneId.of("America/Cancun"))
    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM 'del' yyyy", Locale("es"))
    var formattedDate by mutableStateOf(currentDate.format(dateFormatter).replaceFirstChar { it.uppercase() })
        private set

    // Función para navegar a la pantalla de crear reporte con la categoría seleccionada
    fun navigateToCrearReporte(navController: NavController, categoria: Categoria) {
        navController.navigate("crear_reporte/${categoria.titulo}")
    }

    // Función para actualizar la fecha (si es necesario)
    fun updateDate() {
        val newDate = LocalDate.now(ZoneId.of("America/Cancun"))
        formattedDate = newDate.format(dateFormatter).replaceFirstChar { it.uppercase() }
    }
}