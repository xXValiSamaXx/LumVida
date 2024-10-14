package com.example.lumviva.ui.login.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String) {
        // Implementar lógica de inicio de sesión
        // Actualizar _loginState según el resultado
    }

    fun loginWithGoogle() {
        // Implementar lógica de inicio de sesión con Google
        // Actualizar _loginState según el resultado
    }
}

sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}