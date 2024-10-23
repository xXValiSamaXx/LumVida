package com.example.lumviva.ui.Reportes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumviva.ui.auth.AuthState
import com.example.lumviva.ui.auth.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportesViewModel(private val authViewModel: AuthViewModel) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    init {
        viewModelScope.launch {
            // Verificar estado inicial
            checkCurrentUser()

            // Observar cambios futuros
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Authenticated -> {
                        _isAuthenticated.value = true
                        updateUserName(state.user)
                    }
                    else -> {
                        _isAuthenticated.value = false
                        _userName.value = ""
                    }
                }
            }
        }
    }

    private fun checkCurrentUser() {
        auth.currentUser?.let { user ->
            _isAuthenticated.value = true
            updateUserName(user)
        } ?: run {
            _isAuthenticated.value = false
            _userName.value = ""
        }
    }

    private fun updateUserName(user: com.google.firebase.auth.FirebaseUser) {
        _userName.value = when {
            !user.displayName.isNullOrEmpty() -> user.displayName!!
            !user.email.isNullOrEmpty() -> user.email!!.substringBefore("@")
            else -> "Usuario"
        }
    }

    fun logout() {
        authViewModel.logout()
    }

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