package com.example.lumviva.ui.Reportes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumviva.ui.auth.AuthState
import com.example.lumviva.ui.auth.AuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportesViewModel(private val authViewModel: AuthViewModel) : ViewModel() {

    private val _userName = MutableStateFlow("Invitado")
    val userName: StateFlow<String> = _userName

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    init {
        viewModelScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Authenticated -> {
                        _userName.value = state.user.displayName ?: state.user.email ?: "Usuario"
                        _isAuthenticated.value = true
                    }
                    else -> {
                        _userName.value = "Invitado"
                        _isAuthenticated.value = false
                    }
                }
            }
        }
    }

    fun logout() {
        authViewModel.logout()
    }

    // Aquí puedes añadir más funciones relacionadas con la pantalla de Reportes
    // Por ejemplo, funciones para obtener la lista de reportes, crear un nuevo reporte, etc.

    class Factory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReportesViewModel::class.java)) {
                return ReportesViewModel(authViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}