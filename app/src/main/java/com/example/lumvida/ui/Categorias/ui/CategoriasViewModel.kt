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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class CategoriasViewModel : ViewModel() {

    data class Categoria(
        val titulo: String,
        val icono: ImageVector,
        val ruta: String
    )

    private var _categorias = mutableStateOf<List<Categoria>>(emptyList())
    var categorias by _categorias

    private val authToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI0IiwianRpIjoiOGFmYjhjZmI0MmZhZjBkMGFiNThiZmM2Nzc1YmI3OTkwZDUxNmZlNmY2MDY4YmQ4N2EwOGY4NmZiNTAwNWJiN2JhYTQxZTZiM2FiZWNlZDEiLCJpYXQiOjE3MzEwMjQ1NjMuNTMyMjcyLCJuYmYiOjE3MzEwMjQ1NjMuNTMyMjgsImV4cCI6MTc2MjU2MDU2My4yNTcxNzIsInN1YiI6IjM2Iiwic2NvcGVzIjpbXX0.ZjVSqn3OEVlaIWqPXChfiqPmJulxHJFAkZRA7cqurgcCnHm-24olZcOtzRTu25CQa4OHRvceA-pmrYPOBzPxZWivMe7hbRxtRk-o50vJqgsHBxFsLGkNUAx_PkGQgBPGsFuF6AUpePcRhriqeTytqVWlwUiEP7-w0PT0BDFlGtWguVGrKUVQP0wCqvrBkTb8HYUaZQZjLmH_CS3las0MR8_I398ZAfBvo9UnXwaCNvN_CLimW7gBbp93MGRe0uZ_8Cpd1aRI_5KBHcyLF-uTcKucK3qLt-PR83WBlsI_e9mEPQLkhFPeuaXdUSWX0Jls_9rrnVycOfIjq3ju3B4i8gJfbNU_ptnAdX0YXjyttgh9Hyp1_8rZtlH07fyKg9D8rnEF5AjgnuKDoAM8VCW29E1a7PBMGpHdf5DjqXLt_f_SzW6zS7DzUewU6-CP4stNa_G6Zro4WYLYyFCKZCIG9OQyCSDd9e5EvWNu9SPQqqjB3F7fnW4QGRVBFhsWrfM54fn0QCHV61n3HFV0erjFzdApMc0m-835pK76wcTy24za3uSp7UyYI3GrNLEYwu9ZVxMbVKv5Tx3wklWTU6Cub02WqjETS8bZ36BPXS0DfnlDW-dgiTr5nEuMJo81tdqcXBs8Po--yGFn3OitwuX0UBLz2-3L1k4VyTBZjQyU498"

    init {
        cargarCategorias()
        // Inicializar con categorías por defecto en caso de fallo de la API
        initializeDefaultCategorias()
    }

    private fun initializeDefaultCategorias() {
        _categorias.value = listOf(
            Categoria("Baches", Icons.Default.Warning, "baches"),
            Categoria("Alumbrado público", Icons.Default.Lightbulb, "alumbrado"),
            Categoria("Alcantarillado", Icons.Default.Water, "alcantarillado"),
            Categoria("Residuos acumulados", Icons.Default.Delete, "residuos")
        )
    }

    private fun cargarCategorias() {
        viewModelScope.launch {
            try {
                val loginRequest = LoginRequest(
                    email = "L21390304@chetumal.tecnm.mx",
                    password = "SDCHDd3pwV2NYv5"
                )

                val response = RetrofitClient.apiService.login(loginRequest, authToken)

                if (response.isSuccessful) {
                    val tiposInfraestructura = response.body()?.tipos ?: emptyList()

                    // Filtrar y mapear solo los tipos que necesitamos
                    val categoriasFiltered = tiposInfraestructura
                        .filter { tipo ->
                            tipo.nombre.lowercase() in listOf(
                                "baches",
                                "alumbrado público",
                                "alcantarillado",
                                "residuos acumulados"
                            )
                        }
                        .map { tipo ->
                            val (icono, ruta) = when (tipo.nombre.lowercase()) {
                                "baches" -> Pair(Icons.Default.Warning, "baches")
                                "alumbrado público" -> Pair(Icons.Default.Lightbulb, "alumbrado")
                                "alcantarillado" -> Pair(Icons.Default.Water, "alcantarillado")
                                "residuos acumulados" -> Pair(Icons.Default.Delete, "residuos")
                                else -> Pair(Icons.Default.Info, "otros")
                            }
                            Categoria(tipo.nombre, icono, ruta)
                        }

                    if (categoriasFiltered.isNotEmpty()) {
                        _categorias.value = categoriasFiltered
                    }
                } else {
                    Log.e("CategoriasViewModel", "Error en la respuesta: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("CategoriasViewModel", "Error al cargar categorías", e)
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
}