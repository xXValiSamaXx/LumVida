package com.example.lumvida.ui.RecuperarContrasena.ui // Define el paquete donde se encuentra esta clase.

import androidx.lifecycle.ViewModel // Importa la clase ViewModel para la lógica de la vista.
import androidx.lifecycle.ViewModelProvider // Importa la clase para proporcionar instancias de ViewModel.
import androidx.lifecycle.viewModelScope // Importa el scope de corutina para el ViewModel.
import com.example.lumvida.ui.Auth.ui.AuthState // Importa los diferentes estados de autenticación.
import com.example.lumvida.ui.Auth.ui.AuthViewModel // Importa el ViewModel de autenticación.
import kotlinx.coroutines.flow.MutableStateFlow // Importa la clase para crear flujos de estado mutables.
import kotlinx.coroutines.flow.StateFlow // Importa la clase para crear flujos de estado inmutables.
import kotlinx.coroutines.launch // Importa la función para lanzar corutinas.

class RecuperarContraseñaViewModel(private val authViewModel: AuthViewModel) : ViewModel() {
    private val _state = MutableStateFlow<RecuperarContraseñaState>(RecuperarContraseñaState.Initial) // Inicializa un flujo mutable para el estado de recuperación de contraseña.
    val state: StateFlow<RecuperarContraseñaState> = _state // Define un flujo de estado inmutable que expone el estado.

    init {
        // Colecciona el estado de autenticación en un flujo.
        viewModelScope.launch {
            authViewModel.authState.collect { authState ->
                // Cambia el estado de recuperación de contraseña según el estado de autenticación.
                _state.value = when (authState) {
                    is AuthState.Loading -> RecuperarContraseñaState.Loading // Si está cargando, actualiza el estado a Loading.
                    is AuthState.ResetPasswordSent -> RecuperarContraseñaState.Success // Si se ha enviado el correo, actualiza el estado a Success.
                    is AuthState.Error -> RecuperarContraseñaState.Error(authState.message) // Si hay un error, actualiza el estado con el mensaje de error.
                    else -> RecuperarContraseñaState.Initial // Para cualquier otro estado, se considera el estado inicial.
                }
            }
        }
    }

    fun recuperarContraseña(email: String) { // Función para iniciar el proceso de recuperación de contraseña.
        viewModelScope.launch { // Lanza una corutina en el scope del ViewModel.
            when {
                email.isBlank() -> { // Si el correo está vacío:
                    _state.value = RecuperarContraseñaState.Error("El correo electrónico no puede estar vacío") // Actualiza el estado a Error.
                }
                !isValidEmail(email) -> { // Si el correo no es válido:
                    _state.value = RecuperarContraseñaState.Error("El correo electrónico no es válido") // Actualiza el estado a Error.
                }
                else -> { // Si el correo es válido:
                    _state.value = RecuperarContraseñaState.Loading // Actualiza el estado a Loading.
                    authViewModel.resetPassword(email) // Llama a la función de restablecimiento de contraseña en el ViewModel de autenticación.
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean { // Función privada para validar el formato del correo electrónico.
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() // Utiliza patrones de Android para comprobar si el correo es válido.
    }

    class Factory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory { // Factoría para crear instancias del ViewModel.
        @Suppress("UNCHECKED_CAST") // Suprime la advertencia de cast no seguro.
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecuperarContraseñaViewModel::class.java)) { // Verifica si la clase es la correcta.
                return RecuperarContraseñaViewModel(authViewModel) as T // Devuelve una nueva instancia del ViewModel.
            }
            throw IllegalArgumentException("Unknown ViewModel class") // Lanza una excepción si la clase no es reconocida.
        }
    }
}

// Definición de estados posibles para la recuperación de contraseña.
sealed class RecuperarContraseñaState {
    object Initial : RecuperarContraseñaState() // Estado inicial.
    object Loading : RecuperarContraseñaState() // Estado de carga.
    object Success : RecuperarContraseñaState() // Estado de éxito tras enviar el correo.
    data class Error(val message: String) : RecuperarContraseñaState() // Estado de error con un mensaje.
}
