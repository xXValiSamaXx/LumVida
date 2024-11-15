package com.example.lumvida.ui.Auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumvida.ui.Auth.data.AuthProvider
import com.example.lumvida.ui.Auth.data.Usuario
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        auth.currentUser?.let { user ->
            _authState.value = AuthState.Authenticated(user)
        } ?: run {
            _authState.value = AuthState.Unauthenticated
        }
    }

    // Método actualizado para manejar el registro con todos los campos
    fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        userData: Map<String, Any>
    ) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                result.user?.let { user ->
                    val usuario = Usuario(
                        uid = user.uid,
                        email = email,
                        nombre = userData["nombre"] as String,
                        provider = AuthProvider.EMAIL,
                        telefono = userData["telefono"] as String,
                        createdAt = userData["createdAt"] as Long
                    )

                    // Guardar en Firestore con todos los campos
                    db.collection("usuarios")
                        .document(user.uid)
                        .set(usuario)
                        .await()

                    _authState.value = AuthState.Authenticated(user)
                } ?: throw Exception("No se pudo obtener información del usuario")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al registrar")
            }
        }
    }

    fun loginWithGoogle(account: GoogleSignInAccount?) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                // Validación más estricta de la cuenta
                if (account == null) {
                    _authState.value = AuthState.Error("No se pudo obtener la cuenta de Google")
                    return@launch
                }

                if (account.idToken == null) {
                    _authState.value = AuthState.Error("No se pudo obtener el token de autenticación")
                    return@launch
                }

                // Crear credencial
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                try {
                    // Intentar autenticación con Firebase
                    val result = auth.signInWithCredential(credential).await()

                    result.user?.let { user ->
                        // Verificar si el usuario ya existe
                        val userDoc = db.collection("usuarios").document(user.uid).get().await()

                        if (!userDoc.exists()) {
                            // Crear nuevo usuario
                            val usuario = Usuario(
                                uid = user.uid,
                                email = account.email ?: "",
                                nombre = account.displayName ?: "",
                                provider = AuthProvider.GOOGLE,
                                telefono = "",
                                createdAt = System.currentTimeMillis()
                            )

                            db.collection("usuarios")
                                .document(user.uid)
                                .set(usuario)
                                .await()
                        }

                        _authState.value = AuthState.Authenticated(user)
                    } ?: throw Exception("No se pudo obtener información del usuario")

                } catch (e: Exception) {
                    _authState.value = AuthState.Error("Error al autenticar con Firebase: ${e.message}")
                }

            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error en el inicio de sesión con Google: ${e.message}")
            }
        }
    }

    // Los demás métodos permanecen igual
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.signInWithEmailAndPassword(email, password).await()
                checkAuthState()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error de autenticación")
            }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.ResetPasswordSent
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al enviar el correo de restablecimiento")
            }
        }
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    object Unauthenticated : AuthState()
    object ResetPasswordSent : AuthState()
    data class Error(val message: String) : AuthState()
}