package com.example.lumviva.ui.Inicio.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumviva.ui.Auth.ui.AuthState
import com.example.lumviva.ui.Auth.ui.AuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel para la pantalla de inicio
class InicioViewModel(private val authViewModel: AuthViewModel) : ViewModel() {
    // Flujo mutable que indica si el usuario está autenticado
    private val _isAuthenticated = MutableStateFlow(false)
    // Flujo inmutable que permite observar el estado de autenticación
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    // Inicialización del ViewModel
    init {
        viewModelScope.launch {
            // Observa el estado de autenticación del AuthViewModel
            authViewModel.authState.collect { state ->
                // Actualiza el estado de autenticación basado en el estado del AuthViewModel
                _isAuthenticated.value = state is AuthState.Authenticated
            }
        }
    }

    // Fábrica para crear instancias del InicioViewModel
    class Factory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // Verifica si el modelo de vista solicitado es InicioViewModel
            if (modelClass.isAssignableFrom(InicioViewModel::class.java)) {
                // Devuelve una nueva instancia de InicioViewModel
                return InicioViewModel(authViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class") // Lanza una excepción si no coincide
        }
    }
}
