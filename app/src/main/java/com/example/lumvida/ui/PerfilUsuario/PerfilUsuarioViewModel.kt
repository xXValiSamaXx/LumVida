package com.example.lumvida.ui.PerfilUsuario // Define el paquete donde se encuentra esta clase.

import android.util.Log
import androidx.lifecycle.ViewModel // Importa la clase ViewModel de Android para gestionar el ciclo de vida de los datos.
import androidx.lifecycle.ViewModelProvider // Importa la clase ViewModelProvider para crear instancias de ViewModel.
import androidx.lifecycle.viewModelScope // Importa la función viewModelScope para lanzar corutinas dentro del ViewModel.
import com.example.lumvida.ui.Auth.ui.AuthState // Importa el estado de autenticación.
import com.example.lumvida.ui.Auth.ui.AuthViewModel // Importa el ViewModel de autenticación.
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow // Importa MutableStateFlow para gestionar flujos de estado mutable.
import kotlinx.coroutines.flow.StateFlow // Importa StateFlow para gestionar flujos de estado inmutable.
import kotlinx.coroutines.launch // Importa la función launch para iniciar corutinas.
import kotlinx.coroutines.tasks.await

class PerfilUsuarioViewModel(
    private val authViewModel: AuthViewModel // Recibe una instancia de AuthViewModel para interactuar con la autenticación.
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

    class Factory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory { // Clase Factory para crear instancias del ViewModel.
        @Suppress("UNCHECKED_CAST") // Suprime advertencias de cast inseguro.
        override fun <T : ViewModel> create(modelClass: Class<T>): T { // Método para crear el ViewModel.
            return PerfilUsuarioViewModel(authViewModel) as T // Retorna una nueva instancia de PerfilUsuarioViewModel.
        }
    }
}
