package com.example.lumvida.ui.Reportes.ui // Define el paquete donde se encuentra esta clase.

import androidx.lifecycle.ViewModel // Importa la clase ViewModel para manejar la lógica de la interfaz de usuario.
import androidx.lifecycle.ViewModelProvider // Importa la clase para crear instancias de ViewModels.
import androidx.lifecycle.viewModelScope // Importa la extensión para ejecutar corutinas en el contexto del ViewModel.
import com.example.lumvida.ui.Auth.ui.AuthState // Importa el estado de autenticación.
import com.example.lumvida.ui.Auth.ui.AuthViewModel // Importa el ViewModel de autenticación.
import com.google.firebase.auth.FirebaseAuth // Importa la clase para autenticación de Firebase.
import com.google.firebase.firestore.FirebaseFirestore // Importa la clase para Firestore de Firebase.
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow // Importa la clase para flujos mutables.
import kotlinx.coroutines.flow.StateFlow // Importa la clase para flujos de estado.
import kotlinx.coroutines.launch // Importa la función para lanzar corutinas.
import kotlinx.coroutines.tasks.await // Importa la función para convertir tareas de Firebase en corutinas.

class ReportesViewModel(private val authViewModel: AuthViewModel) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userNameCache = mutableMapOf<String, String>()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    init {
        viewModelScope.launch(Dispatchers.IO) {
            checkCurrentUser()
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
            viewModelScope.launch(Dispatchers.IO) {
                getUserNameFromFirestore(user.uid)
            }
        } ?: run {
            _isAuthenticated.value = false
            _userName.value = ""
        }
    }

    private suspend fun getUserNameFromFirestore(uid: String) {
        userNameCache[uid]?.let {
            _userName.value = it
            return
        }

        try {
            val userDoc = db.collection("usuarios").document(uid)
                .get(Source.CACHE)
                .await()

            val nombre = userDoc.getString("nombre")
            if (!nombre.isNullOrEmpty()) {
                _userName.value = nombre
                userNameCache[uid] = nombre
                return
            }

            val networkDoc = db.collection("usuarios").document(uid)
                .get(Source.SERVER)
                .await()

            networkDoc.getString("nombre")?.let {
                _userName.value = it
                userNameCache[uid] = it
            } ?: setFallbackName()

        } catch (e: Exception) {
            setFallbackName()
        }
    }

    private fun setFallbackName() {
        auth.currentUser?.let { user ->
            val name = user.displayName
                ?: user.email?.substringBefore("@")
                ?: "Usuario"
            _userName.value = name
            user.uid?.let { userNameCache[it] = name }
        }
    }

    fun logout() = authViewModel.logout()

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