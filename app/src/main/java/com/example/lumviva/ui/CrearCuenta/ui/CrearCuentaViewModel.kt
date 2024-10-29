package com.example.lumviva.ui.crearcuenta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumviva.ui.Auth.ui.AuthViewModel
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
            when {
                nombre.isBlank() -> {
                    _state.value = CrearCuentaState.Error("El nombre no puede estar vacío")
                }
                nombre.length < 2 -> {
                    _state.value = CrearCuentaState.Error("El nombre debe tener al menos 2 caracteres")
                }
                !isValidEmail(email) -> {
                    _state.value = CrearCuentaState.Error("El correo electrónico no es válido")
                }
                !isValidPhone(telefono) -> {
                    _state.value = CrearCuentaState.Error("El teléfono debe tener 10 dígitos")
                }
                !isValidPassword(password) -> {
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
                    _state.value = CrearCuentaState.Error("Las contraseñas no coinciden")
                }
                else -> {
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
                        _state.value = CrearCuentaState.Error(e.message ?: "Error al crear la cuenta")
                    }
                }
            }
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