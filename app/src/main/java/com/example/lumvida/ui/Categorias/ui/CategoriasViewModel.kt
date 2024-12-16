/*El código define un ViewModel en Android llamado CategoriasViewModel,
 que gestiona el estado de las categorías de reportes en la aplicación.
 Inicialmente, establece un conjunto de categorías predeterminadas (como "Bacheo",
 "Alumbrado Público", "Drenajes Obstruidos" y "Basura Acumulada"). Luego, utiliza
 una llamada a la API para obtener categorías adicionales, actualizando la lista de
  categorías solo si la respuesta es exitosa. Maneja errores como fallos de conexión
   o tiempo de espera, y asegura que las categorías por defecto estén disponibles en
    caso de problemas. Además, el ViewModel gestiona la fecha actual en formato personalizado
    y ofrece funciones para navegar entre pantallas y comprobar la conexión a Internet.*/

package com.example.lumvida.ui.Categorias.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.lumvida.network.RetrofitClient
import com.example.lumvida.network.model.LoginRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.coroutines.cancellation.CancellationException

@RequiresApi(Build.VERSION_CODES.O)
class CategoriasViewModel : ViewModel() {

    data class Categoria(
        val titulo: String,
        val icono: ImageVector,
        val ruta: String
    )

    private var _categorias = mutableStateOf<List<Categoria>>(emptyList())
    var categorias by _categorias

    private var _isLoading = mutableStateOf(false)
    var isLoading by _isLoading

    private var _error = mutableStateOf<String?>(null)
    var error by _error

    private var _isConnected = mutableStateOf(false)
    var isConnected by _isConnected

    // Categorías por defecto
    private val defaultCategorias = listOf(
        Categoria("Bacheo", Icons.Default.Warning, "baches"),
        Categoria("Alumbrado Público", Icons.Default.Lightbulb, "alumbrado"),
        Categoria("Drenajes Obstruidos", Icons.Default.Water, "alcantarillado"),
        Categoria("Basura Acumulada", Icons.Default.Delete, "residuos")
    )

    // Job para controlar las llamadas a la API
    private var apiJob: Job? = null

    init {
        // Establecer categorías por defecto inmediatamente
        _categorias.value = defaultCategorias
        cargarCategorias()
    }

    fun cargarCategorias() {
        // Cancelar job anterior si existe
        apiJob?.cancel()

        apiJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Usar withTimeout para limitar el tiempo de espera a 250ms
                withTimeout(250) {
                    val loginRequest = LoginRequest(
                        email = "L21390304@chetumal.tecnm.mx",
                        password = "SDCHDd3pwV2NYv5"
                    )

                    val token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI0IiwianRpIjoiOGFmYjhjZmI0MmZhZjBkMGFiNThiZmM2Nzc1YmI3OTkwZDUxNmZlNmY2MDY4YmQ4N2EwOGY4NmZiNTAwNWJiN2JhYTQxZTZiM2FiZWNlZDEiLCJpYXQiOjE3MzEwMjQ1NjMuNTMyMjcyLCJuYmYiOjE3MzEwMjQ1NjMuNTMyMjgsImV4cCI6MTc2MjU2MDU2My4yNTcxNzIsInN1YiI6IjM2Iiwic2NvcGVzIjpbXX0.ZjVSqn3OEVlaIWqPXChfiqPmJulxHJFAkZRA7cqurgcCnHm-24olZcOtzRTu25CQa4OHRvceA-pmrYPOBzPxZWivMe7hbRxtRk-o50vJqgsHBxFsLGkNUAx_PkGQgBPGsFuF6AUpePcRhriqeTytqVWlwUiEP7-w0PT0BDFlGtWguVGrKUVQP0wCqvrBkTb8HYUaZQZjLmH_CS3las0MR8_I398ZAfBvo9UnXwaCNvN_CLimW7gBbp93MGRe0uZ_8Cpd1aRI_5KBHcyLF-uTcKucK3qLt-PR83WBlsI_e9mEPQLkhFPeuaXdUSWX0Jls_9rrnVycOfIjq3ju3B4i8gJfbNU_ptnAdX0YXjyttgh9Hyp1_8rZtlH07fyKg9D8rnEF5AjgnuKDoAM8VCW29E1a7PBMGpHdf5DjqXLt_f_SzW6zS7DzUewU6-CP4stNa_G6Zro4WYLYyFCKZCIG9OQyCSDd9e5EvWNu9SPQqqjB3F7fnW4QGRVBFhsWrfM54fn0QCHV61n3HFV0erjFzdApMc0m-835pK76wcTy24za3uSp7UyYI3GrNLEYwu9ZVxMbVKv5Tx3wklWTU6Cub02WqjETS8bZ36BPXS0DfnlDW-dgiTr5nEuMJo81tdqcXBs8Po--yGFn3OitwuX0UBLz2-3L1k4VyTBZjQyU498"

                    val response = RetrofitClient.apiService.getTiposInfraestructura(loginRequest, token)

                    if (response.isSuccessful) {
                        val infraestructuras = response.body()?.datosTablas?.comedatosPreticor ?: emptyList()

                        val categoriasFiltered = infraestructuras
                            .filter { tipo ->
                                tipo.tipoInfraestructura.lowercase() in listOf(
                                    "bacheo",
                                    "alumbrado público",
                                    "drenajes obstruidos",
                                    "basura acumulada"
                                )
                            }
                            .map { tipo ->
                                val (icono, ruta) = when (tipo.tipoInfraestructura.lowercase()) {
                                    "bacheo" -> Pair(Icons.Default.Warning, "baches")
                                    "alumbrado público" -> Pair(Icons.Default.Lightbulb, "alumbrado")
                                    "drenajes obstruidos" -> Pair(Icons.Default.Water, "alcantarillado")
                                    "basura acumulada" -> Pair(Icons.Default.Delete, "residuos")
                                    else -> Pair(Icons.Default.Info, "otros")
                                }
                                Categoria(tipo.tipoInfraestructura, icono, ruta)
                            }

                        // Solo actualizar si tenemos categorías válidas
                        if (categoriasFiltered.isNotEmpty()) {
                            _categorias.value = categoriasFiltered
                        }
                        _isConnected.value = true
                    } else {
                        // Si hay error en la respuesta, mantener las categorías por defecto
                        Log.w("CategoriasViewModel", "Error en la respuesta: ${response.code()}")
                        _isConnected.value = false
                    }
                }
            } catch (e: Exception) {
                when (e) {
                    is TimeoutCancellationException -> {
                        // Timeout de la API - usar categorías por defecto
                        Log.w("CategoriasViewModel", "Timeout al cargar categorías de la API")
                        _isConnected.value = false
                    }
                    is CancellationException -> {
                        // Ignorar cancelaciones intencionales
                    }
                    is UnknownHostException -> {
                        // Sin conexión a internet
                        Log.w("CategoriasViewModel", "Sin conexión a Internet")
                        _isConnected.value = false
                    }
                    else -> {
                        // Otros errores
                        Log.e("CategoriasViewModel", "Error al cargar categorías: ${e.message}", e)
                        _isConnected.value = false
                    }
                }
                // No mostrar error al usuario ya que tenemos las categorías por defecto
                _error.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val currentDate = LocalDate.now(ZoneId.of("America/Cancun"))
    private val dateFormatter = DateTimeFormatter.ofPattern(
        "EEEE dd 'de' MMMM 'del' yyyy", Locale("es")
    )

    var formattedDate by mutableStateOf(
        currentDate.format(dateFormatter).replaceFirstChar { it.uppercase() }
    )
        private set

    fun navigateToCrearReporte(navController: NavController, categoria: Categoria) {
        navController.navigate("crear_reporte/${categoria.titulo}")
    }

    fun updateDate() {
        val newDate = LocalDate.now(ZoneId.of("America/Cancun"))
        formattedDate = newDate.format(dateFormatter).replaceFirstChar { it.uppercase() }
    }

    fun checkNetworkConnection(context: android.content.Context) {
        _isConnected.value = RetrofitClient.isOnline(context)
        if (_isConnected.value) {
            cargarCategorias()
        }
    }

    fun recargarCategorias() {
        cargarCategorias()
    }

    override fun onCleared() {
        super.onCleared()
        apiJob?.cancel()
    }
}