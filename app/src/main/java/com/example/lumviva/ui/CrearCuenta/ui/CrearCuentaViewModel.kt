package com.example.lumviva.ui.crearcuenta.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CrearCuentaViewModel : ViewModel() {
    private val _state = MutableStateFlow<CrearCuentaState>(CrearCuentaState.Initial)
    val state: StateFlow<CrearCuentaState> = _state

    fun crearCuenta(nombreCompleto: String, email: String, password: String) {
        // Aquí iría la lógica para crear la cuenta
        // Por ahora, solo actualizamos el estado
        _state.value = CrearCuentaState.Success
    }
}

sealed class CrearCuentaState {
    object Initial : CrearCuentaState()
    object Loading : CrearCuentaState()
    object Success : CrearCuentaState()
    data class Error(val message: String) : CrearCuentaState()
}