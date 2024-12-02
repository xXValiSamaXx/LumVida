package com.example.lumvida.ui.crearcuenta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumvida.ui.Auth.ui.AuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CrearCuentaViewModel(private val authViewModel: AuthViewModel) : ViewModel() {
    private val _state = MutableStateFlow<CrearCuentaState>(CrearCuentaState.Initial)
    val state: StateFlow<CrearCuentaState> = _state

    fun crearCuenta(
        email: String,
        password: String,
        confirmarPassword: String,
        nombre: String,
        telefono: String
    ) {
        viewModelScope.launch {
            _state.value = CrearCuentaState.Loading

            // Lista para acumular todos los errores
            val errores = mutableListOf<String>()

            // Validación de campos vacíos
            when {
                nombre.isBlank() -> {
                    errores.add("El campo nombre es obligatorio")
                }
                email.isBlank() -> {
                    errores.add("El campo correo electrónico es obligatorio")
                }
                telefono.isBlank() -> {
                    errores.add("El campo teléfono es obligatorio")
                }
                password.isBlank() -> {
                    errores.add("El campo contraseña es obligatorio")
                }
                confirmarPassword.isBlank() -> {
                    errores.add("Debe confirmar la contraseña")
                }
            }

            // Si hay campos vacíos, mostrar esos errores primero
            if (errores.isNotEmpty()) {
                _state.value = CrearCuentaState.Error(errores.joinToString("\\n"))
                return@launch
            }

            // Validaciones de formato y contenido
            when {
                nombre.length < 2 -> {
                    errores.add("El nombre debe tener al menos 2 caracteres")
                }
                !isValidEmail(email) -> {
                    errores.add("El formato del correo electrónico no es válido")
                }
                !isValidPhone(telefono) -> {
                    errores.add("El teléfono debe tener exactamente 10 dígitos")
                }
                !isValidPassword(password) -> {
                    errores.add("""La contraseña debe cumplir con:
                        |• Al menos 8 caracteres
                        |• Al menos una letra mayúscula
                        |• Al menos una letra minúscula
                        |• Al menos un número
                        |• Al menos un carácter especial (@#$%^&+=)""".trimMargin())
                }
                password != confirmarPassword -> {
                    errores.add("Las contraseñas no coinciden")
                }
            }

            // Si hay errores de validación, mostrarlos
            if (errores.isNotEmpty()) {
                _state.value = CrearCuentaState.Error(errores.joinToString("\\n"))
                return@launch
            }

            // Si no hay errores, proceder con la creación de la cuenta
            try {
                val userData = mapOf(
                    "nombre" to nombre,
                    "telefono" to telefono,
                    "provider" to "EMAIL",
                    "createdAt" to System.currentTimeMillis()
                )
                authViewModel.createUserWithEmailAndPassword(email, password, userData)
                _state.value = CrearCuentaState.Success
            } catch (e: Exception) {
                _state.value = CrearCuentaState.Error(translateFirebaseError(e.message ?: "Error al crear la cuenta"))
            }
        }
    }

    private fun translateFirebaseError(errorMessage: String): String {
        return when {
            errorMessage.contains("email already in use") ->
                "Este correo electrónico ya está registrado"
            errorMessage.contains("weak password") ->
                "La contraseña es demasiado débil"
            errorMessage.contains("invalid email") ->
                "El formato del correo electrónico no es válido"
            errorMessage.contains("network error") ->
                "Error de conexión. Por favor, verifica tu conexión a internet"
            else -> "Error al crear la cuenta: $errorMessage"
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.length == 10 && phone.all { it.isDigit() }
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8 && // Mínimo 8 caracteres
                password.any { it.isUpperCase() } && // Al menos una mayúscula
                password.any { it.isLowerCase() } && // Al menos una minúscula
                password.any { it.isDigit() } && // Al menos un número
                password.any { "@#\$%^&+=".contains(it) } // Al menos un carácter especial
    }
}

sealed class CrearCuentaState {
    object Initial : CrearCuentaState()
    object Loading : CrearCuentaState()
    object Success : CrearCuentaState()
    data class Error(val message: String) : CrearCuentaState()
}