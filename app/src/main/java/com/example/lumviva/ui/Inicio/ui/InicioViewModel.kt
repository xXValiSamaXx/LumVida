package com.example.lumviva.ui.Inicio.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumviva.ui.Auth.ui.AuthState
import com.example.lumviva.ui.Auth.ui.AuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InicioViewModel(private val authViewModel: AuthViewModel) : ViewModel() {
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    init {
        viewModelScope.launch {
            authViewModel.authState.collect { state ->
                _isAuthenticated.value = state is AuthState.Authenticated
            }
        }
    }

    class Factory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InicioViewModel::class.java)) {
                return InicioViewModel(authViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}