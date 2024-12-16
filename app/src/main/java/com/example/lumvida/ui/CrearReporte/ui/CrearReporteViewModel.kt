/*organiza el flujo de creación de un reporte mediante funciones clave.
Permite capturar o subir imágenes, obtener la ubicación actual o manual, validar los
datos ingresados, generar un folio único y enviar la información a Firebase. Además,
 incluye funciones para guardar búsquedas recientes, filtrar por categorías, verificar
 conexión a internet y manejar errores. Su objetivo es simplificar el proceso, garantizando
  que los reportes sean precisos y completos antes de almacenarlos en la base de datos.*/

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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumvida.data.model.RecentSearch
import com.example.lumvida.data.repository.SearchHistoryRepository
import com.example.lumvida.network.model.NominatimResponse
import com.example.lumvida.ui.Auth.ui.AuthState
import com.example.lumvida.ui.Auth.ui.AuthViewModel
import com.example.lumvida.utils.NetworkUtils
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.io.ByteArrayOutputStream
import java.util.UUID

class CrearReporteViewModel(
    private val searchHistoryRepository: SearchHistoryRepository? = null
) : ViewModel() {
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

    //anonimo

    var nombreCompleto by mutableStateOf("")
        private set

    var telefono by mutableStateOf("")
        private set

    var folio by mutableStateOf(0)
        private set

    fun onNombreCompletoChange(newValue: String) {
        nombreCompleto = newValue
    }

    fun onTelefonoChange(newValue: String) {
        telefono = newValue
    }

    fun dismissReporteSent() {
        reporteSent = false
    }

    //Ubicacion Quintana Roo
    private val chetumalCenter = GeoPoint(18.5001889, -88.296146)

    // Añade las nuevas propiedades y funciones para el historial de búsquedas
    val recentSearches = searchHistoryRepository?.recentSearches
        ?.map { entities ->
            entities.map { entity ->
                RecentSearch(
                    name = entity.name,
                    address = entity.address,
                    timestamp = entity.timestamp,
                    lat = entity.latitude,  // Añadir la latitud
                    lon = entity.longitude  // Añadir la longitud
                )
            }
        }
        ?.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )

    fun addToRecentSearches(suggestion: NominatimResponse) {
        viewModelScope.launch {
            searchHistoryRepository?.addSearch(
                name = suggestion.displayName.split(",").first(),
                address = suggestion.displayName,
                lat = suggestion.lat.toDouble(),
                lon = suggestion.lon.toDouble()
            )
        }
    }

    fun removeFromRecentSearches(search: RecentSearch) {
        viewModelScope.launch {
            searchHistoryRepository?.removeSearch(search) // Cambiado para usar RecentSearch directamente
        }
    }

    class CrearReporteViewModelFactory(
        private val searchHistoryRepository: SearchHistoryRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CrearReporteViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CrearReporteViewModel(searchHistoryRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private suspend fun obtenerUltimoFolio(): Int {
        return try {
            val reportesRef = firestore.collection("reportes")
                .orderBy("folio", Query.Direction.DESCENDING)
                .limit(1)

            val snapshot = reportesRef.get().await()
            if (snapshot.isEmpty) {
                100000 // Folio inicial si no hay reportes
            } else {
                val ultimoFolio = snapshot.documents[0].getLong("folio") ?: 100000
                ultimoFolio.toInt()
            }
        } catch (e: Exception) {
            Log.e("CrearReporteViewModel", "Error obteniendo último folio", e)
            100000 // Folio por defecto en caso de error
        }
    }

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

    private fun optimizeBitmap(bitmap: Bitmap): Bitmap {
        val maxDimension = 1024

        val ratio = maxDimension.toFloat() / Math.max(bitmap.width, bitmap.height)
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()

        if (ratio >= 1) return bitmap

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private suspend fun convertImageToBase64(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            val maxDimension = 1024
            var scale = 1
            while ((options.outWidth / scale > maxDimension) ||
                (options.outHeight / scale > maxDimension)) {
                scale *= 2
            }

            val scaledOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
            }
            val scaledInputStream = context.contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(scaledInputStream, null, scaledOptions)
            scaledInputStream?.close()

            if (bitmap == null) throw Exception("No se pudo cargar la imagen")

            bitmap = optimizeBitmap(bitmap)

            val outputStream = ByteArrayOutputStream()

            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)

            var compressQuality = 70
            while (outputStream.size() > 750000 && compressQuality > 10) {
                outputStream.reset()
                compressQuality -= 10
                bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, outputStream)
                Log.d("CrearReporteViewModel",
                    "Recomprimiendo imagen con calidad: $compressQuality%, " +
                            "Tamaño: ${outputStream.size() / 1024}KB")
            }

            val imageBytes = outputStream.toByteArray()
            val base64String = Base64.encodeToString(imageBytes, Base64.DEFAULT)

            Log.d("CrearReporteViewModel", """
                Imagen procesada:
                - Dimensiones originales: ${options.outWidth}x${options.outHeight}
                - Dimensiones finales: ${bitmap.width}x${bitmap.height}
                - Calidad final: $compressQuality%
                - Tamaño final: ${outputStream.size() / 1024}KB
            """.trimIndent())

            outputStream.close()
            bitmap.recycle()

            "data:image/jpeg;base64,$base64String"

        } catch (e: Exception) {
            Log.e("CrearReporteViewModel", "Error al convertir imagen a base64", e)
            throw e
        }
    }

    fun sendReport(categoria: String, authViewModel: AuthViewModel, context: Context) = viewModelScope.launch {
        try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                showError("No hay conexión a Internet. Por favor, verifica tu conexión e intenta nuevamente.")
                return@launch
            }

            if (!validateFields()) return@launch

            isLoading = true
            errorMessage = null

            NetworkUtils.executeWithConnection(
                context = context,
                onNoConnection = {
                    showError("Se perdió la conexión a Internet. Por favor, intenta nuevamente.")
                    isLoading = false
                }
            ) {
                val userId = when (val state = authViewModel.authState.value) {
                    is AuthState.Authenticated -> state.user.uid
                    else -> "anonymous"
                }

                val ultimoFolio = obtenerUltimoFolio()
                val nuevoFolio = ultimoFolio + 1
                folio = nuevoFolio

                val imageBase64 = photoUri?.let { uri ->
                    convertImageToBase64(context, uri)
                } ?: throw Exception("No se encontró la imagen")

                val reporte = hashMapOf(
                    "folio" to nuevoFolio,
                    "userId" to userId,
                    "isAnonymous" to (userId == "anonymous"),
                    "nombreCompleto" to if (userId == "anonymous") nombreCompleto else "",
                    "telefono" to if (userId == "anonymous") telefono else "",
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

                firestore.collection("reportes")
                    .add(reporte)
                    .await()

                reporteSent = true
                isLoading = false
            }
        } catch (e: Exception) {
            Log.e("CrearReporteViewModel", "Error al enviar reporte", e)
            showError("Error al enviar el reporte: ${e.message}")
            isLoading = false
        }
    }

    data class ReporteMap(
        val id: String,
        val folio: Int,
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

    private var _selectedCategory = mutableStateOf<String?>(null)
    val selectedCategory: String? get() = _selectedCategory.value

    private var _filteredReportes = mutableStateOf<List<ReporteMap>>(emptyList())
    val filteredReportes: List<ReporteMap> get() = _filteredReportes.value

    fun updateSelectedCategory(categoria: String?) {
        _selectedCategory.value = categoria
        filterReportes()
    }

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
                    folio = (doc.getLong("folio") ?: 0).toInt(),
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
                            folio = (doc.getLong("folio") ?: 0).toInt(),
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
                    filterReportes()
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        reportesListener?.remove()
    }
}