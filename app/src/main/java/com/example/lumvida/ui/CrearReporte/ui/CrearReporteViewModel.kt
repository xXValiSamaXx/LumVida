package com.example.lumvida.ui.CrearReporte.ui

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumvida.ui.Auth.ui.AuthState
import com.example.lumvida.ui.Auth.ui.AuthViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.util.GeoPoint
import java.util.UUID

// Clase ViewModel para crear un reporte
class CrearReporteViewModel : ViewModel() {
    // Referencias a Firestore y Storage de Firebase
    private val firestore = Firebase.firestore
    private val storage = Firebase.storage.reference

    // Propiedades observables del ViewModel
    var photoUri: Uri? by mutableStateOf(null) // URI de la foto capturada
        private set

    var showCamera by mutableStateOf(false) // Estado de la cámara
        private set

    var hasPhoto by mutableStateOf(false) // Indicador de si se ha capturado una foto
        private set

    var showMap by mutableStateOf(false) // Estado del mapa
        private set

    var selectedLocation by mutableStateOf<GeoPoint?>(null) // Ubicación seleccionada
        private set

    var direccion by mutableStateOf("") // Dirección del reporte
        private set

    var comentario by mutableStateOf("") // Comentario del reporte
        private set

    var showErrorDialog by mutableStateOf(false) // Estado del diálogo de error
        private set

    var errorMessage by mutableStateOf<String?>(null) // Mensaje de error
        private set

    var isLoading by mutableStateOf(false) // Estado de carga
        private set

    var reporteSent by mutableStateOf(false) // Indicador de si el reporte fue enviado
        private set

    // Centro de Chetumal (coordenadas geográficas)
    private val chetumalCenter = GeoPoint(18.5001889, -88.296146)

    // Función para mostrar un error
    fun showError(message: String) {
        errorMessage = message // Asignar mensaje de error
        showErrorDialog = true // Mostrar diálogo de error
    }

    // Función para cerrar el diálogo de error
    fun dismissErrorDialog() {
        showErrorDialog = false // Ocultar diálogo de error
        errorMessage = null // Limpiar mensaje de error
    }

    // Función para actualizar el comentario
    fun onComentarioChange(newComentario: String) {
        comentario = newComentario // Actualizar comentario
        Log.d("CrearReporteViewModel", "Comentario actualizado: $newComentario") // Log del cambio
    }

    // Función para manejar la foto capturada
    fun onPhotoCaptured(uri: Uri) {
        photoUri = uri // Guardar URI de la foto
        hasPhoto = true // Indicar que se ha capturado una foto
        showCamera = false // Ocultar cámara
        Log.d("CrearReporteViewModel", "Foto capturada: $uri") // Log de la foto capturada
    }

    // Función para mostrar la cámara
    fun onCameraClick() {
        showCamera = true // Cambiar estado para mostrar cámara
    }

    // Función para ocultar la cámara
    fun onDismissCamera() {
        showCamera = false // Cambiar estado para ocultar cámara
    }

    // Función para mostrar el mapa
    fun onMapClick() {
        showMap = true // Cambiar estado para mostrar mapa
    }

    // Función para ocultar el mapa
    fun onDismissMap() {
        showMap = false // Cambiar estado para ocultar mapa
    }

    // Función para manejar la ubicación seleccionada en el mapa
    fun onLocationSelected(location: GeoPoint, address: String) {
        Log.d("CrearReporteViewModel", "Ubicación seleccionada: Lat=${location.latitude}, Lon=${location.longitude}")
        Log.d("CrearReporteViewModel", "Dirección: $address")
        selectedLocation = location // Guardar ubicación seleccionada
        direccion = address // Guardar dirección
        showMap = false // Ocultar mapa
    }

    // Función para obtener el centro de Chetumal
    fun getChetumalCenter() = chetumalCenter

    // Función para limpiar la foto
    fun clearPhoto() {
        photoUri = null // Limpiar URI de la foto
        hasPhoto = false // Indicar que no hay foto
    }

    // Función para validar campos antes de enviar el reporte
    private fun validateFields(): Boolean {
        errorMessage = null // Limpiar mensaje de error

        // Verificar si hay una foto
        if (!hasPhoto || photoUri == null) {
            showError("Por favor, toma una foto") // Mostrar error si no hay foto
            Log.w("CrearReporteViewModel", "Validación fallida: No hay foto") // Log de validación
            return false // Retornar false
        }

        // Verificar si hay dirección
        if (direccion.isEmpty()) {
            showError("Por favor, selecciona una ubicación") // Mostrar error si no hay dirección
            Log.w("CrearReporteViewModel", "Validación fallida: No hay dirección") // Log de validación
            return false // Retornar false
        }

        // Verificar si hay comentario
        if (comentario.isEmpty()) {
            showError("Por favor, describe el motivo del reporte") // Mostrar error si no hay comentario
            Log.w("CrearReporteViewModel", "Validación fallida: No hay comentario") // Log de validación
            return false // Retornar false
        }

        Log.d("CrearReporteViewModel", "Validación exitosa de todos los campos") // Log de validación exitosa
        return true // Retornar true si todos los campos son válidos
    }

    // Función para enviar el reporte
    fun sendReport(categoria: String, authViewModel: AuthViewModel) = viewModelScope.launch {
        try {
            if (!validateFields()) return@launch // Validar campos antes de continuar

            isLoading = true // Indicar que se está cargando
            errorMessage = null // Limpiar mensaje de error
            Log.d("CrearReporteViewModel", "Iniciando envío de reporte para categoría: $categoria") // Log de envío

            // Obtener userId (puede ser anónimo o autenticado)
            val userId = when (val state = authViewModel.authState.value) {
                is AuthState.Authenticated -> state.user.uid // Obtener ID del usuario autenticado
                else -> "anonymous"  // Usuario anónimo
            }

            // Subir foto
            val photoId = UUID.randomUUID().toString() // Generar ID único para la foto
            val photoRef = storage.child("reportes/${photoId}.jpg") // Referencia en Firebase Storage
            Log.d("CrearReporteViewModel", "Subiendo foto con ID: $photoId") // Log de subida de foto

            photoUri?.let { uri -> // Verificar que el URI de la foto no sea nulo
                photoRef.putFile(uri).await() // Subir foto
                Log.d("CrearReporteViewModel", "Foto subida exitosamente") // Log de éxito
            }

            // Obtener URL de la foto
            val photoUrl = photoRef.downloadUrl.await().toString() // Obtener URL de descarga de la foto
            Log.d("CrearReporteViewModel", "URL de la foto: $photoUrl") // Log de URL de la foto

            // Crear documento del reporte
            val reporte = hashMapOf(
                "userId" to userId, // ID del usuario
                "isAnonymous" to (userId == "anonymous"),  // Indicador si es anónimo
                "categoria" to categoria, // Categoría del reporte
                "foto" to photoUrl, // URL de la foto
                "direccion" to direccion, // Dirección del reporte
                "comentario" to comentario, // Comentario del reporte
                "fecha" to com.google.firebase.Timestamp.now(), // Fecha actual
                "estado" to "pendiente", // Estado del reporte
                "ubicacion" to hashMapOf( // Ubicación del reporte
                    "latitud" to selectedLocation?.latitude,
                    "longitud" to selectedLocation?.longitude
                )
            )

            // Guardar en Firestore
            firestore.collection("reportes") // Referencia a la colección "reportes"
                .add(reporte) // Agregar el reporte
                .await() // Esperar a que se complete

            Log.d("CrearReporteViewModel", "Reporte guardado exitosamente") // Log de éxito
            Log.d("CrearReporteViewModel", "Datos del reporte: $reporte") // Log de datos del reporte

            reporteSent = true // Indicar que el reporte fue enviado
            isLoading = false // Cambiar estado de carga

        } catch (e: Exception) {
            Log.e("CrearReporteViewModel", "Error al enviar reporte", e) // Log de error
            showError("Error al enviar el reporte: ${e.message}") // Mostrar mensaje de error
            isLoading = false // Cambiar estado de carga
        }
    }

    data class ReporteMap(
        val id: String,
        val categoria: String,
        val latitud: Double,
        val longitud: Double,
        val estado: String,
        val direccion: String,
        val foto: String,
        val comentario: String,
        val fecha: com.google.firebase.Timestamp
    )

    var reportes by mutableStateOf<List<ReporteMap>>(emptyList())
        private set

    // Cambiamos la forma de manejar la categoría seleccionada
    private var _selectedCategory = mutableStateOf<String?>(null)
    val selectedCategory: String? get() = _selectedCategory.value

    // Lista filtrada de reportes
    private var _filteredReportes = mutableStateOf<List<ReporteMap>>(emptyList())
    val filteredReportes: List<ReporteMap> get() = _filteredReportes.value

    // Función para establecer la categoría seleccionada y filtrar reportes
    fun updateSelectedCategory(categoria: String?) {
        _selectedCategory.value = categoria
        filterReportes()
    }

    // Función para filtrar reportes
    private fun filterReportes() {
        _filteredReportes.value = if (_selectedCategory.value == null) {
            reportes
        } else {
            reportes.filter { it.categoria.equals(_selectedCategory.value, ignoreCase = true) }
        }
    }

    fun obtenerReportes() = viewModelScope.launch {
        try {
            val reportesSnapshot = firestore.collection("reportes").get().await()
            reportes = reportesSnapshot.documents.mapNotNull { doc ->
                val ubicacion = doc.get("ubicacion") as? Map<*, *>
                val latitud = (ubicacion?.get("latitud") as? Double) ?: return@mapNotNull null
                val longitud = (ubicacion?.get("longitud") as? Double) ?: return@mapNotNull null

                ReporteMap(
                    id = doc.id,
                    categoria = doc.getString("categoria") ?: "",
                    latitud = latitud,
                    longitud = longitud,
                    estado = doc.getString("estado") ?: "",
                    direccion = doc.getString("direccion") ?: "",
                    foto = doc.getString("foto") ?: "",
                    comentario = doc.getString("comentario") ?: "",
                    fecha = doc.getTimestamp("fecha") ?: com.google.firebase.Timestamp.now()
                )
            }
            // Actualizar también los reportes filtrados
            filterReportes()
        } catch (e: Exception) {
            Log.e("CrearReporteViewModel", "Error obteniendo reportes", e)
        }
    }

    private var reportesListener: ListenerRegistration? = null

    fun iniciarEscuchaReportes() {
        reportesListener = firestore.collection("reportes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CrearReporteViewModel", "Error escuchando reportes", error)
                    return@addSnapshotListener
                }

                snapshot?.let { querySnapshot ->
                    reportes = querySnapshot.documents.mapNotNull { doc ->
                        val ubicacion = doc.get("ubicacion") as? Map<*, *>
                        val latitud = (ubicacion?.get("latitud") as? Double) ?: return@mapNotNull null
                        val longitud = (ubicacion?.get("longitud") as? Double) ?: return@mapNotNull null

                        ReporteMap(
                            id = doc.id,
                            categoria = doc.getString("categoria") ?: "",
                            latitud = latitud,
                            longitud = longitud,
                            estado = doc.getString("estado") ?: "",
                            direccion = doc.getString("direccion") ?: "",
                            foto = doc.getString("foto") ?: "",
                            comentario = doc.getString("comentario") ?: "",
                            fecha = doc.getTimestamp("fecha") ?: com.google.firebase.Timestamp.now()
                        )
                    }
                    // Actualizar reportes filtrados después de cada actualización
                    filterReportes()
                }
            }
    }

    // limpiar el listener
    override fun onCleared() {
        super.onCleared()
        reportesListener?.remove()
    }
}