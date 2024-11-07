package com.example.lumvida.ui.PerfilUsuario // Define el paquete donde se encuentra esta clase.

import androidx.lifecycle.ViewModel // Importa la clase ViewModel de Android para gestionar el ciclo de vida de los datos.
import androidx.lifecycle.ViewModelProvider // Importa la clase ViewModelProvider para crear instancias de ViewModel.
import androidx.lifecycle.viewModelScope // Importa la función viewModelScope para lanzar corutinas dentro del ViewModel.
import com.example.lumvida.ui.Auth.ui.AuthState // Importa el estado de autenticación.
import com.example.lumvida.ui.Auth.ui.AuthViewModel // Importa el ViewModel de autenticación.
import kotlinx.coroutines.flow.MutableStateFlow // Importa MutableStateFlow para gestionar flujos de estado mutable.
import kotlinx.coroutines.flow.StateFlow // Importa StateFlow para gestionar flujos de estado inmutable.
import kotlinx.coroutines.launch // Importa la función launch para iniciar corutinas.

class PerfilUsuarioViewModel(
    private val authViewModel: AuthViewModel // Recibe una instancia de AuthViewModel para interactuar con la autenticación.
) : ViewModel() {

    private val _userName = MutableStateFlow("") // Crea un flujo mutable para el nombre del usuario, inicialmente vacío.
    val userName: StateFlow<String> = _userName // Exponer el flujo de nombre del usuario como StateFlow inmutable.

    private val _userEmail = MutableStateFlow("") // Crea un flujo mutable para el correo del usuario, inicialmente vacío.
    val userEmail: StateFlow<String> = _userEmail // Exponer el flujo de correo del usuario como StateFlow inmutable.

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
