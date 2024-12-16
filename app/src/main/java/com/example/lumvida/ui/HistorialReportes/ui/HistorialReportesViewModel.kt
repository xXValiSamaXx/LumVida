/* gestionar la obtención y presentación del historial de reportes de un usuario en una
aplicación Android con Firebase. Sus principales funcionalidades son:

Conexión con Firestore para recuperar reportes
Filtrar reportes por el ID del usuario autenticado
Manejar estados de carga y error
Mapear documentos de Firestore a objetos Reporte
Ordenar los reportes por folio en orden descendente

Características específicas:

Usa StateFlow para manejar el estado de los reportes, la carga y los errores
En el método cargarReportes(), realiza una consulta a Firebase para obtener solo los reportes del usuario actual
Incluye un manejo de errores con logging
Usa una fábrica personalizada para la creación del ViewModel que requiere un AuthViewModel
La clase Reporte define la estructura de datos para cada reporte con propiedades como folio,
dirección, fecha, estado, categoría, comentario y foto*/

package com.example.lumvida.ui.HistorialReportes.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumvida.ui.Auth.ui.AuthState
import com.example.lumvida.ui.Auth.ui.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HistorialReportesViewModel(
    private val authViewModel: AuthViewModel
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    data class Reporte(
        val folio: Int,
        val direccion: String,
        val fecha: Timestamp,
        val estado: String,
        val categoria: String,
        val comentario: String,
        val foto: String? = null
    )

    private val _reportes = MutableStateFlow<List<Reporte>>(emptyList())
    val reportes: StateFlow<List<Reporte>> = _reportes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        cargarReportes()
    }

    fun cargarReportes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val userId = when (val authState = authViewModel.authState.value) {
                    is AuthState.Authenticated -> authState.user.uid
                    else -> return@launch
                }

                val snapshot = db.collection("reportes")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                _reportes.value = snapshot.documents.mapNotNull { doc ->
                    try {
                        Reporte(
                            folio = (doc.getLong("folio") ?: 0).toInt(),
                            direccion = doc.getString("direccion") ?: "",
                            fecha = doc.getTimestamp("fecha") ?: Timestamp.now(),
                            estado = doc.getString("estado") ?: "pendiente",
                            categoria = doc.getString("categoria") ?: "Sin categoría",
                            comentario = doc.getString("comentario") ?: "Sin descripción",
                            foto = doc.getString("foto")
                        )
                    } catch (e: Exception) {
                        Log.e("HistorialReportesViewModel", "Error mapeando reporte", e)
                        null
                    }
                }.sortedByDescending { it.folio }
            } catch (e: Exception) {
                Log.e("HistorialReportesViewModel", "Error cargando reportes", e)
                _error.value = "Error al cargar los reportes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    class Factory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistorialReportesViewModel::class.java)) {
                return HistorialReportesViewModel(authViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}