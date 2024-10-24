package com.example.lumviva.ui.Reportes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumviva.ui.Auth.ui.AuthState
import com.example.lumviva.ui.Auth.ui.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ReportesViewModel(private val authViewModel: AuthViewModel) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
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
                        getUserNameFromFirestore(state.user.uid)
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
            viewModelScope.launch {
                getUserNameFromFirestore(user.uid)
            }
        } ?: run {
            _isAuthenticated.value = false
            _userName.value = ""
        }
    }

    private suspend fun getUserNameFromFirestore(uid: String) {
        try {
            val userDoc = db.collection("usuarios").document(uid).get().await()
            if (userDoc.exists()) {
                val nombre = userDoc.getString("nombre")
                if (!nombre.isNullOrEmpty()) {
                    _userName.value = nombre
                    return
                }
            }

            // Fallback a displayName o email si no se encuentra el nombre en Firestore
            auth.currentUser?.let { user ->
                _userName.value = when {
                    !user.displayName.isNullOrEmpty() -> user.displayName!!
                    !user.email.isNullOrEmpty() -> user.email!!.substringBefore("@")
                    else -> "Usuario"
                }
            }
        } catch (e: Exception) {
            // En caso de error, usar el fallback
            auth.currentUser?.let { user ->
                _userName.value = when {
                    !user.displayName.isNullOrEmpty() -> user.displayName!!
                    !user.email.isNullOrEmpty() -> user.email!!.substringBefore("@")
                    else -> "Usuario"
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
            if (modelClass.isAssignableFrom(ReportesViewModel::class.java)) {
                return ReportesViewModel(authViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}