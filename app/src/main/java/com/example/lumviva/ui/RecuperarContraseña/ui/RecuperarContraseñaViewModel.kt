package com.example.lumviva.ui.RecuperarContrasena.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumviva.ui.auth.AuthState
import com.example.lumviva.ui.auth.AuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecuperarContraseñaViewModel(private val authViewModel: AuthViewModel) : ViewModel() {
    private val _state = MutableStateFlow<RecuperarContraseñaState>(RecuperarContraseñaState.Initial)
    val state: StateFlow<RecuperarContraseñaState> = _state

    init {
        viewModelScope.launch {
            authViewModel.authState.collect { authState ->
                _state.value = when (authState) {
                    is AuthState.Loading -> RecuperarContraseñaState.Loading
                    is AuthState.ResetPasswordSent -> RecuperarContraseñaState.Success
                    is AuthState.Error -> RecuperarContraseñaState.Error(authState.message)
                    else -> RecuperarContraseñaState.Initial
                }
            }
        }
    }

    fun recuperarContraseña(email: String) {
        viewModelScope.launch {
            _state.value = RecuperarContraseñaState.Loading
            authViewModel.resetPassword(email)
        }
    }

    class Factory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecuperarContraseñaViewModel::class.java)) {
                return RecuperarContraseñaViewModel(authViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class RecuperarContraseñaState {
    object Initial : RecuperarContraseñaState()
    object Loading : RecuperarContraseñaState()
    object Success : RecuperarContraseñaState()
    data class Error(val message: String) : RecuperarContraseñaState()
}