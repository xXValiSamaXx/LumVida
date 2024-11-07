package com.example.lumvida.ui.Categorias.ui

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

    // Clase de datos para representar una categoría, con título, ícono y ruta
    data class Categoria(
        val titulo: String,       // Nombre de la categoría, ej. "Bache"
        val icono: ImageVector,   // Icono representativo de la categoría
        val ruta: String          // Ruta asociada a la categoría, utilizada para la navegación
    )

    // Lista privada de categorías disponibles
    private val _categorias = listOf(
        Categoria("Basurero clandestino", Icons.Default.Delete, "basurero_clandestino"),
        Categoria("Bache", Icons.Default.Warning, "baches"),
        Categoria("Semáforo", Icons.Default.Circle, "semaforos"),
        Categoria("Luminaria", Icons.Default.Lightbulb, "luminaria"),
        Categoria("Parque", Icons.Default.Park, "parque"),
        Categoria("Alcantarillado", Icons.Default.Water, "alcantarillado")
    )

    // Expone las categorías como un estado observable, solo de lectura desde fuera
    var categorias by mutableStateOf(_categorias)
        private set // Se hace privado el set para evitar modificaciones externas

    // Variable privada que almacena la fecha actual en una zona horaria específica
    private val currentDate = LocalDate.now(ZoneId.of("America/Cancun"))

    // Formatter que define el formato de fecha en español, ej. "lunes 30 de octubre de 2023"
    private val dateFormatter = DateTimeFormatter.ofPattern(
        "EEEE dd 'de' MMMM 'del' yyyy", Locale("es")
    )

    // Estado observable para almacenar la fecha formateada
    var formattedDate by mutableStateOf(
        currentDate.format(dateFormatter).replaceFirstChar { it.uppercase() }
    )
        private set

    /**
     * Función `navigateToCrearReporte`: Navega a la pantalla de creación de reporte para la
     * categoría seleccionada.
     *
     * @param navController El controlador de navegación usado para dirigir la navegación.
     * @param categoria La categoría seleccionada que contiene la ruta de destino.
     */
    fun navigateToCrearReporte(navController: NavController, categoria: Categoria) {
        // Utiliza el controlador de navegación para ir a la ruta deseada, que incluye el título de la categoría
        navController.navigate("crear_reporte/${categoria.titulo}")
    }

    /**
     * Función `updateDate`: Permite actualizar la fecha formateada, útil en caso de que la fecha
     * cambie y se quiera reflejar el nuevo valor en la interfaz.
     */
    fun updateDate() {
        // Obtiene la fecha actualizada
        val newDate = LocalDate.now(ZoneId.of("America/Cancun"))
        // Formatea la fecha a string con el formato deseado y la primera letra en mayúscula
        formattedDate = newDate.format(dateFormatter).replaceFirstChar { it.uppercase() }
    }
}
