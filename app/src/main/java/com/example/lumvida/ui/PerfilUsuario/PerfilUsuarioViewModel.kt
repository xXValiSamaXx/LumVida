/*gestiona los datos del perfil del usuario, incluyendo la carga, edición y actualización
 de la información personal (nombre, correo y teléfono) desde Firebase Firestore. Utiliza
 flujos (StateFlow) para mantener y exponer el estado de estos datos. Además, gestiona la
 funcionalidad del mapa, permitiendo la descarga de mapas offline y la gestión del caché con OSM.
  También maneja la autenticación y tiene una función para cerrar sesión.*/


package com.example.lumvida.ui.PerfilUsuario // Define el paquete donde se encuentra esta clase.

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel // Importa la clase ViewModel de Android para gestionar el ciclo de vida de los datos.
import androidx.lifecycle.ViewModelProvider // Importa la clase ViewModelProvider para crear instancias de ViewModel.
import androidx.lifecycle.viewModelScope // Importa la función viewModelScope para lanzar corutinas dentro del ViewModel.
import com.example.lumvida.ui.Auth.ui.AuthState // Importa el estado de autenticación.
import com.example.lumvida.ui.Auth.ui.AuthViewModel // Importa el ViewModel de autenticación.
import com.example.lumvida.utils.OSMMapManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow // Importa MutableStateFlow para gestionar flujos de estado mutable.
import kotlinx.coroutines.flow.StateFlow // Importa StateFlow para gestionar flujos de estado inmutable.
import kotlinx.coroutines.launch // Importa la función launch para iniciar corutinas.
import kotlinx.coroutines.tasks.await
import org.osmdroid.views.MapView

class PerfilUsuarioViewModel(
    private val authViewModel: AuthViewModel,
    private val context: Context
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _userName = MutableStateFlow("") // Crea un flujo mutable para el nombre del usuario, inicialmente vacío.
    val userName: StateFlow<String> = _userName // Exponer el flujo de nombre del usuario como StateFlow inmutable.

    private val _userEmail = MutableStateFlow("") // Crea un flujo mutable para el correo del usuario, inicialmente vacío.
    val userEmail: StateFlow<String> = _userEmail // Exponer el flujo de correo del usuario como StateFlow inmutable.

    private val _userPhone = MutableStateFlow("")
    val userPhone: StateFlow<String> = _userPhone

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                when (val currentState = authViewModel.authState.value) {
                    is AuthState.Authenticated -> {
                        val userId = currentState.user.uid
                        val userDoc = db.collection("usuarios").document(userId).get().await()

                        if (userDoc.exists()) {
                            _userName.value = userDoc.getString("nombre") ?: "Usuario"
                            _userEmail.value = userDoc.getString("email") ?: ""
                            _userPhone.value = userDoc.getString("telefono") ?: ""
                        }
                    }
                    else -> {
                        _userName.value = "Usuario"
                        _userEmail.value = ""
                        _userPhone.value = ""
                    }
                }
            } catch (e: Exception) {
                Log.e("PerfilUsuarioViewModel", "Error loading user data", e)
            }
        }
    }

    fun toggleEditing() {
        _isEditing.value = !_isEditing.value
        if (!_isEditing.value) {
            // Si se cancela la edición, recargar los datos originales
            loadUserData()
        }
    }

    fun updateUserData(newName: String, newPhone: String) {
        viewModelScope.launch {
            try {
                // Validar el número de teléfono
                when {
                    newPhone.length != 10 -> {
                        // Manejar el error - podrías agregar un estado para mostrar errores
                        Log.e("PerfilUsuarioViewModel", "El número de teléfono debe tener exactamente 10 dígitos")
                        return@launch
                    }
                    !newPhone.all { it.isDigit() } -> {
                        Log.e("PerfilUsuarioViewModel", "El número de teléfono solo debe contener dígitos")
                        return@launch
                    }
                }

                _isLoading.value = true
                when (val currentState = authViewModel.authState.value) {
                    is AuthState.Authenticated -> {
                        val userId = currentState.user.uid
                        val updates = hashMapOf<String, Any>(
                            "nombre" to newName,
                            "telefono" to newPhone
                        )

                        db.collection("usuarios").document(userId)
                            .update(updates)
                            .await()

                        _userName.value = newName
                        _userPhone.value = newPhone
                        _isEditing.value = false
                    }
                    else -> {
                        // Manejar el caso de no autenticado
                    }
                }
            } catch (e: Exception) {
                Log.e("PerfilUsuarioViewModel", "Error updating user data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    init {
        viewModelScope.launch { // Inicia una corutina dentro del scope del ViewModel.
            // Obtener información del usuario del authState
            when (val currentState = authViewModel.authState.value) { // Obtiene el estado actual de autenticación.
                is AuthState.Authenticated -> { // Si el usuario está autenticado:
                    _userName.value = currentState.user.displayName ?: "Usuario" // Asigna el nombre del usuario o "Usuario" por defecto.
                    _userEmail.value = currentState.user.email ?: "" // Asigna el correo del usuario o vacío por defecto.
                }
                else -> { // Si el usuario no está autenticado:
                    _userName.value = "Usuario" // Asigna "Usuario" como nombre por defecto.
                    _userEmail.value = "" // Asigna vacío al correo.
                }
            }
        }
    }

    fun logout() { // Función para cerrar sesión.
        authViewModel.logout() // Llama al método logout del AuthViewModel.
    }

    private val osmMapManager = OSMMapManager(context)
    private val _downloadProgress = MutableStateFlow<Int?>(null)
    val downloadProgress: StateFlow<Int?> = _downloadProgress

    private val _isDownloadingMap = MutableStateFlow(false)
    val isDownloadingMap: StateFlow<Boolean> = _isDownloadingMap

    private var mapView: MapView? = null

    fun initializeMapView(mapView: MapView) {
        this.mapView = mapView
        osmMapManager.initialize(mapView)
    }

    private val _isMapEnabled = MutableStateFlow(false) // Inicialmente habilitado
    val isMapEnabled: StateFlow<Boolean> = _isMapEnabled


    fun downloadOfflineMap() {
        viewModelScope.launch {
            if (!_isMapEnabled.value) {
                Log.d("PerfilUsuarioViewModel", "La funcionalidad del mapa está deshabilitada.")
                return@launch
            }

            try {
                if (mapView == null) {
                    val tempMapView = MapView(context)
                    tempMapView.controller.setZoom(15.0)
                    initializeMapView(tempMapView)
                }

                _isDownloadingMap.value = true
                osmMapManager.downloadOfflineMap(radius = 10.0)
            } catch (e: Exception) {
                Log.e("PerfilUsuarioViewModel", "Error downloading map", e)
            } finally {
                _isDownloadingMap.value = false
                _downloadProgress.value = null
            }
        }
    }


    fun getCacheSize(): String {
        val bytes = osmMapManager.getCacheSize()
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }

    fun clearMapCache() {
        viewModelScope.launch {
            osmMapManager.clearCache()
        }
    }

    companion object {
        fun provideFactory(
            authViewModel: AuthViewModel,
            context: Context
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PerfilUsuarioViewModel(authViewModel, context) as T
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mapView = null
    }
}
