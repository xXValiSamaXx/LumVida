package com.example.lumviva.ui.crearcuenta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumviva.ui.Auth.ui.AuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel para gestionar la creación de cuentas de usuario.
class CrearCuentaViewModel(private val authViewModel: AuthViewModel) : ViewModel() {

    // Estado interno del ViewModel que refleja el estado de la creación de cuenta.
    private val _state = MutableStateFlow<CrearCuentaState>(CrearCuentaState.Initial)
    // Exposición del estado como StateFlow, para que la UI observe los cambios.
    val state: StateFlow<CrearCuentaState> = _state

    // Método para crear una cuenta de usuario.
    fun crearCuenta(
        email: String,                 // Correo electrónico del usuario
        password: String,              // Contraseña del usuario
        confirmarPassword: String,     // Contraseña para confirmar
        nombre: String,                // Nombre del usuario
        telefono: String               // Teléfono del usuario
    ) {
        // Lanzar una coroutine para manejar la creación de la cuenta de forma asíncrona.
        viewModelScope.launch {
            _state.value = CrearCuentaState.Loading // Cambiar el estado a "Cargando"
            // Validaciones de entrada del usuario.
            when {
                nombre.isBlank() -> {
                    // Si el nombre está vacío, mostrar un error.
                    _state.value = CrearCuentaState.Error("El nombre no puede estar vacío")
                }
                nombre.length < 2 -> {
                    // Si el nombre tiene menos de 2 caracteres, mostrar un error.
                    _state.value = CrearCuentaState.Error("El nombre debe tener al menos 2 caracteres")
                }
                !isValidEmail(email) -> {
                    // Validar el formato del correo electrónico.
                    _state.value = CrearCuentaState.Error("El correo electrónico no es válido")
                }
                !isValidPhone(telefono) -> {
                    // Validar el formato del teléfono.
                    _state.value = CrearCuentaState.Error("El teléfono debe tener 10 dígitos")
                }
                !isValidPassword(password) -> {
                    // Validar la complejidad de la contraseña.
                    _state.value = CrearCuentaState.Error(
                        """La contraseña debe cumplir con los siguientes requisitos:
                        |• Al menos 8 caracteres
                        |• Al menos una letra mayúscula
                        |• Al menos una letra minúscula
                        |• Al menos un número
                        |• Al menos un carácter especial (@#$%^&+=)""".trimMargin()
                    )
                }
                password != confirmarPassword -> {
                    // Verificar que las contraseñas coinciden.
                    _state.value = CrearCuentaState.Error("Las contraseñas no coinciden")
                }
                else -> {
                    // Si todas las validaciones pasan, proceder a crear la cuenta.
                    try {
                        val userData = mapOf(
                            "nombre" to nombre,             // Datos del usuario a guardar
                            "telefono" to telefono,
                            "provider" to "EMAIL",          // Proveedor de autenticación
                            "createdAt" to System.currentTimeMillis() // Marca de tiempo de creación
                        )
                        // Llamar al método del AuthViewModel para crear el usuario.
                        authViewModel.createUserWithEmailAndPassword(email, password, userData)
                        _state.value = CrearCuentaState.Success // Cambiar el estado a "Éxito"
                    } catch (e: Exception) {
                        // Manejar excepciones durante la creación de la cuenta.
                        _state.value = CrearCuentaState.Error(e.message ?: "Error al crear la cuenta")
                    }
                }
            }
        }
    }

    // Método para validar el formato del correo electrónico.
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() // Usa patrones de Android para validación.
    }

    // Método para validar el formato del teléfono.
    private fun isValidPhone(phone: String): Boolean {
        return phone.length == 10 && phone.all { it.isDigit() } // Verifica longitud y que todos los caracteres son dígitos.
    }

    // Método para validar la complejidad de la contraseña.
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8 && // Mínimo 8 caracteres
                password.any { it.isUpperCase() } && // Al menos una letra mayúscula
                password.any { it.isLowerCase() } && // Al menos una letra minúscula
                password.any { it.isDigit() } && // Al menos un número
                password.any { "@#\$%^&+=".contains(it) } // Al menos un carácter especial
    }
}

// Clase sellada para representar los diferentes estados de creación de cuenta.
sealed class CrearCuentaState {
    object Initial : CrearCuentaState() // Estado inicial
    object Loading : CrearCuentaState() // Estado de carga
    object Success : CrearCuentaState() // Estado de éxito
    data class Error(val message: String) : CrearCuentaState() // Estado de error con un mensaje
}
