package com.example.lumviva.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumviva.ui.Auth.data.Usuario
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

    fun loginWithGoogle(account: GoogleSignInAccount?) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                if (account == null) {
                    throw Exception("Google Sign-In failed")
                }
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                val result = auth.signInWithCredential(credential).await()
                result.user?.let { user ->
                    // Crear o actualizar el usuario en Firestore para Google Sign-In
                    val usuario = Usuario(
                        uid = user.uid,
                        email = user.email ?: "",
                        nombre = account.displayName ?: "",
                    )
                    db.collection("usuarios")
                        .document(user.uid)
                        .set(usuario)
                        .await()
                    _authState.value = AuthState.Authenticated(user)
                } ?: throw Exception("No se pudo obtener información del usuario")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error en el inicio de sesión con Google")
            }
        }
    }

    fun registerWithProfile(email: String, password: String, nombre: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                // Crear usuario en Authentication
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                result.user?.let { user ->
                    // Crear documento del usuario en Firestore
                    val usuario = Usuario(
                        uid = user.uid,
                        email = email,
                        nombre = nombre
                    )

                    // Guardar en la colección "usuarios"
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