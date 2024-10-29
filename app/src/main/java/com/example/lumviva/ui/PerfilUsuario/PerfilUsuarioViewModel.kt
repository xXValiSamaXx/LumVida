package com.example.lumviva.ui.PerfilUsuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumviva.ui.Auth.ui.AuthState
import com.example.lumviva.ui.Auth.ui.AuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PerfilUsuarioViewModel(
    private val authViewModel: AuthViewModel
) : ViewModel() {

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail

    init {
        viewModelScope.launch {
            // Obtener información del usuario del authState
            when (val currentState = authViewModel.authState.value) {
                is AuthState.Authenticated -> {
                    _userName.value = currentState.user.displayName ?: "Usuario"
                    _userEmail.value = currentState.user.email ?: ""
                }
                else -> {
                    _userName.value = "Usuario"
                    _userEmail.value = ""
                }
            }
        }
    }

    fun logout() {
        authViewModel.logout()
    }

    class Factory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PerfilUsuarioViewModel(authViewModel) as T
        }
    }
}
