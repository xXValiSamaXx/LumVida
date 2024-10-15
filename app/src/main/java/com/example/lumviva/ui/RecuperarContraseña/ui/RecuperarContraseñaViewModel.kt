package com.example.lumviva.ui.RecuperarContrasena.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RecuperarContraseñaViewModel : ViewModel() {
    private val _state = MutableStateFlow<RecuperarContraseñaState>(RecuperarContraseñaState.Initial)
    val state: StateFlow<RecuperarContraseñaState> = _state

    fun recuperarContraseña(email: String) {
        // Aquí iría la lógica para enviar el correo de recuperación
        // Por ahora, solo actualizamos el estado
        _state.value = RecuperarContraseñaState.Success
    }
}

sealed class RecuperarContraseñaState {
    object Initial : RecuperarContraseñaState()
    object Loading : RecuperarContraseñaState()
    object Success : RecuperarContraseñaState()
    data class Error(val message: String) : RecuperarContraseñaState()
}