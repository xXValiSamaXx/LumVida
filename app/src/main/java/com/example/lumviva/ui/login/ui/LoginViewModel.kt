package com.example.lumviva.ui.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumviva.ui.auth.AuthState
import com.example.lumviva.ui.auth.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val authViewModel: AuthViewModel) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState

    init {
        viewModelScope.launch {
            authViewModel.authState.collect { authState ->
                _loginState.value = when (authState) {
                    is AuthState.Initial -> LoginState.Initial
                    is AuthState.Loading -> LoginState.Loading
                    is AuthState.Authenticated -> LoginState.Success
                    is AuthState.Unauthenticated -> LoginState.Initial
                    is AuthState.Error -> LoginState.Error(authState.message)
                    AuthState.ResetPasswordSent -> TODO()
                }
            }
        }
    }

    fun login(email: String, password: String) {
        authViewModel.login(email, password)
    }

    fun loginWithGoogle(account: GoogleSignInAccount) {
        authViewModel.loginWithGoogle(account)
    }

    class Factory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(authViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}