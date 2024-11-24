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
        val estado: String
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

                // Modificamos la consulta para que sea más simple
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
                            estado = doc.getString("estado") ?: "pendiente"
                        )
                    } catch (e: Exception) {
                        Log.e("HistorialReportesViewModel", "Error mapeando reporte", e)
                        null
                    }
                }.sortedByDescending { it.folio } // Ordenamos en memoria en lugar de en la consulta
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