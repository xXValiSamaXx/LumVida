package com.example.lumviva.ui.crearcuenta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumviva.ui.auth.AuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CrearCuentaViewModel(private val authViewModel: AuthViewModel) : ViewModel() {
    private val _state = MutableStateFlow<CrearCuentaState>(CrearCuentaState.Initial)
    val state: StateFlow<CrearCuentaState> = _state

    fun crearCuenta(email: String, password: String, confirmarPassword: String) {
        viewModelScope.launch {
            _state.value = CrearCuentaState.Loading
            when {
                !isValidEmail(email) -> {
                    _state.value = CrearCuentaState.Error("El correo electrónico no es válido")
                }
                !isValidPassword(password) -> {
                    _state.value = CrearCuentaState.Error("La contraseña debe tener al menos 8 caracteres y contener al menos un carácter especial")
                }
                password != confirmarPassword -> {
                    _state.value = CrearCuentaState.Error("Las contraseñas no coinciden")
                }
                else -> {
                    authViewModel.register(email, password)
                    // El resultado se manejará observando authViewModel.authState
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
        return password.matches(passwordPattern.toRegex())
    }
}

sealed class CrearCuentaState {
    object Initial : CrearCuentaState()
    object Loading : CrearCuentaState()
    data class Error(val message: String) : CrearCuentaState()
}