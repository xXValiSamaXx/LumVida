package com.example.lumviva.ui.CrearReporte.ui

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumviva.ui.Auth.ui.AuthState
import com.example.lumviva.ui.Auth.ui.AuthViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.util.GeoPoint
import java.util.UUID

class CrearReporteViewModel: ViewModel() {
    private val firestore = Firebase.firestore
    private val storage = Firebase.storage.reference

    var photoUri: Uri? by mutableStateOf(null)
        private set

    var showCamera by mutableStateOf(false)
        private set

    var hasPhoto by mutableStateOf(false)
        private set

    var showMap by mutableStateOf(false)
        private set

    var selectedLocation by mutableStateOf<GeoPoint?>(null)
        private set

    var direccion by mutableStateOf("")
        private set

    var comentario by mutableStateOf("")
        private set

    var showErrorDialog by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var reporteSent by mutableStateOf(false)
        private set

    // Centro de Chetumal
    private val chetumalCenter = GeoPoint(18.5001889, -88.296146)

    fun showError(message: String) {
        errorMessage = message
        showErrorDialog = true
    }

    fun dismissErrorDialog() {
        showErrorDialog = false
        errorMessage = null
    }

    fun onComentarioChange(newComentario: String) {
        comentario = newComentario
        Log.d("CrearReporteViewModel", "Comentario actualizado: $newComentario")
    }

    fun onPhotoCaptured(uri: Uri) {
        photoUri = uri
        hasPhoto = true
        showCamera = false
        Log.d("CrearReporteViewModel", "Foto capturada: $uri")
    }

    fun onCameraClick() {
        showCamera = true
    }

    fun onDismissCamera() {
        showCamera = false
    }

    fun onMapClick() {
        showMap = true
    }

    fun onDismissMap() {
        showMap = false
    }

    fun onLocationSelected(location: GeoPoint, address: String) {
        Log.d("CrearReporteViewModel", "Ubicación seleccionada: Lat=${location.latitude}, Lon=${location.longitude}")
        Log.d("CrearReporteViewModel", "Dirección: $address")
        selectedLocation = location
        direccion = address
        showMap = false
    }

    fun getChetumalCenter() = chetumalCenter

    fun clearPhoto() {
        photoUri = null
        hasPhoto = false
    }

    private fun validateFields(): Boolean {
        errorMessage = null

        if (!hasPhoto || photoUri == null) {
            showError("Por favor, toma una foto")
            Log.w("CrearReporteViewModel", "Validación fallida: No hay foto")
            return false
        }

        if (direccion.isEmpty()) {
            showError("Por favor, selecciona una ubicación")
            Log.w("CrearReporteViewModel", "Validación fallida: No hay dirección")
            return false
        }

        if (comentario.isEmpty()) {
            showError("Por favor, describe el motivo del reporte")
            Log.w("CrearReporteViewModel", "Validación fallida: No hay comentario")
            return false
        }

        Log.d("CrearReporteViewModel", "Validación exitosa de todos los campos")
        return true
    }

    fun sendReport(categoria: String, authViewModel: com.example.lumviva.ui.Auth.ui.AuthViewModel) = viewModelScope.launch {
        try {
            if (!validateFields()) return@launch

            isLoading = true
            errorMessage = null
            Log.d("CrearReporteViewModel", "Iniciando envío de reporte para categoría: $categoria")

            // Obtener userId (puede ser anónimo o autenticado)
            val userId = when (val state = authViewModel.authState.value) {
                is AuthState.Authenticated -> state.user.uid
                else -> "anonymous"  // Usuario anónimo
            }

            // Subir foto
            val photoId = UUID.randomUUID().toString()
            val photoRef = storage.child("reportes/${photoId}.jpg")
            Log.d("CrearReporteViewModel", "Subiendo foto con ID: $photoId")

            photoUri?.let { uri ->
                photoRef.putFile(uri).await()
                Log.d("CrearReporteViewModel", "Foto subida exitosamente")
            }

            // Obtener URL de la foto
            val photoUrl = photoRef.downloadUrl.await().toString()
            Log.d("CrearReporteViewModel", "URL de la foto: $photoUrl")

            // Crear documento del reporte
            val reporte = hashMapOf(
                "userId" to userId,
                "isAnonymous" to (userId == "anonymous"),  // Indicador si es anónimo
                "categoria" to categoria,
                "foto" to photoUrl,
                "direccion" to direccion,
                "comentario" to comentario,
                "fecha" to com.google.firebase.Timestamp.now(),
                "estado" to "pendiente",
                "ubicacion" to hashMapOf(
                    "latitud" to selectedLocation?.latitude,
                    "longitud" to selectedLocation?.longitude
                )
            )

            // Guardar en Firestore
            firestore.collection("reportes")
                .add(reporte)
                .await()

            Log.d("CrearReporteViewModel", "Reporte guardado exitosamente")
            Log.d("CrearReporteViewModel", "Datos del reporte: $reporte")

            reporteSent = true
            isLoading = false

        } catch (e: Exception) {
            Log.e("CrearReporteViewModel", "Error al enviar reporte", e)
            showError("Error al enviar el reporte: ${e.message}")
            isLoading = false
        }
    }
}