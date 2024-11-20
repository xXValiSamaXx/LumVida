package com.example.lumvida.ui.CrearReporte.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.io.ByteArrayOutputStream
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

    // Función para optimizar el bitmap
    private fun optimizeBitmap(bitmap: Bitmap): Bitmap {
        val maxDimension = 1024

        // Calcular las nuevas dimensiones manteniendo el aspect ratio
        val ratio = maxDimension.toFloat() / Math.max(bitmap.width, bitmap.height)
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()

        // Retornar el bitmap original si ya es más pequeño
        if (ratio >= 1) return bitmap

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    // Función para convertir Url a Base64 con optimizaciones
    private suspend fun convertImageToBase64(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            // 1. Leer la imagen y obtener sus dimensiones originales
            val inputStream = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // 2. Calcular el factor de escala inicial
            val maxDimension = 1024
            var scale = 1
            while ((options.outWidth / scale > maxDimension) ||
                (options.outHeight / scale > maxDimension)) {
                scale *= 2
            }

            // 3. Cargar la imagen con el factor de escala calculado
            val scaledOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
            }
            val scaledInputStream = context.contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(scaledInputStream, null, scaledOptions)
            scaledInputStream?.close()

            if (bitmap == null) throw Exception("No se pudo cargar la imagen")

            // 4. Optimizar el bitmap si aún es necesario
            bitmap = optimizeBitmap(bitmap)

            // 5. Comprimir la imagen
            val outputStream = ByteArrayOutputStream()

            // Intentar primero con calidad 70%
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)

            // Si el tamaño es muy grande, comprimir más
            var compressQuality = 70
            while (outputStream.size() > 750000 && compressQuality > 10) { // 750KB límite seguro
                outputStream.reset() // Limpiar el stream
                compressQuality -= 10 // Reducir calidad en intervalos de 10
                bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, outputStream)
                Log.d("CrearReporteViewModel",
                    "Recomprimiendo imagen con calidad: $compressQuality%, " +
                            "Tamaño: ${outputStream.size() / 1024}KB")
            }

            // 6. Convertir a base64
            val imageBytes = outputStream.toByteArray()
            val base64String = Base64.encodeToString(imageBytes, Base64.DEFAULT)

            // 7. Logging para monitoreo
            Log.d("CrearReporteViewModel", """
                Imagen procesada:
                - Dimensiones originales: ${options.outWidth}x${options.outHeight}
                - Dimensiones finales: ${bitmap.width}x${bitmap.height}
                - Calidad final: $compressQuality%
                - Tamaño final: ${outputStream.size() / 1024}KB
            """.trimIndent())

            // 8. Limpiar recursos
            outputStream.close()
            bitmap.recycle()

            // 9. Retornar el string base64 con el prefijo correcto
            "data:image/jpeg;base64,$base64String"

        } catch (e: Exception) {
            Log.e("CrearReporteViewModel", "Error al convertir imagen a base64", e)
            throw e
        }
    }

    // Modificar la función sendReport para usar la nueva conversión
    fun sendReport(categoria: String, authViewModel: AuthViewModel, context: Context) = viewModelScope.launch {
        try {
            if (!validateFields()) return@launch

            isLoading = true
            errorMessage = null

            val userId = when (val state = authViewModel.authState.value) {
                is AuthState.Authenticated -> state.user.uid
                else -> "anonymous"
            }

            // Convertir imagen a base64 con las nuevas optimizaciones
            val imageBase64 = photoUri?.let { uri ->
                convertImageToBase64(context, uri)
            } ?: throw Exception("No se encontró la imagen")

            // Crear el documento del reporte con la imagen en base64
            val reporte = hashMapOf(
                "userId" to userId,
                "isAnonymous" to (userId == "anonymous"),
                "categoria" to categoria,
                "foto" to imageBase64,
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

            reporteSent = true
            isLoading = false

        } catch (e: Exception) {
            Log.e("CrearReporteViewModel", "Error al enviar reporte", e)
            showError("Error al enviar el reporte: ${e.message}")
            isLoading = false
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