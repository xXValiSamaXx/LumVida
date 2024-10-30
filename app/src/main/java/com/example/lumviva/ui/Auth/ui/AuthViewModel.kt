package com.example.lumviva.ui.Auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumviva.ui.Auth.data.AuthProvider
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

// ViewModel para gestionar la autenticación de usuarios en Firebase
class AuthViewModel : ViewModel() {

    // Instancia de FirebaseAuth para realizar operaciones de autenticación
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Instancia de FirebaseFirestore para gestionar la base de datos en Firestore
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Variable de estado que gestiona los diferentes estados de autenticación utilizando un flujo reactivo
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)

    // Exposición de la variable de estado como un flujo inmutable
    val authState: StateFlow<AuthState> = _authState

    // Bloque de inicialización que llama a un método para comprobar el estado de autenticación
    init {
        checkAuthState()
    }

    // Método privado que verifica el estado actual de autenticación del usuario
    private fun checkAuthState() {
        auth.currentUser?.let { user ->
            // Si hay un usuario autenticado, se actualiza el estado a "Authenticated"
            _authState.value = AuthState.Authenticated(user)
        } ?: run {
            // Si no hay usuario autenticado, el estado cambia a "Unauthenticated"
            _authState.value = AuthState.Unauthenticated
        }
    }

    // Método para registrar un nuevo usuario con correo electrónico y contraseña
    // Además, guarda datos adicionales del usuario en Firestore
    fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        userData: Map<String, Any>
    ) {
        // Se lanza una nueva corutina para realizar las operaciones de autenticación de forma asíncrona
        viewModelScope.launch {
            try {
                // Se actualiza el estado a "Loading" mientras se realiza el registro
                _authState.value = AuthState.Loading

                // Se crea el usuario con correo y contraseña en Firebase
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                // Si el usuario fue creado exitosamente, se guarda su información en Firestore
                result.user?.let { user ->
                    val usuario = Usuario(
                        uid = user.uid,
                        email = email,
                        nombre = userData["nombre"] as String, // Se obtiene el nombre del Map de datos adicionales
                        provider = AuthProvider.EMAIL, // Se define el proveedor de autenticación
                        telefono = userData["telefono"] as String, // Se obtiene el teléfono del Map de datos adicionales
                        createdAt = userData["createdAt"] as Long // Se obtiene la fecha de creación del Map de datos adicionales
                    )

                    // Se guarda el usuario en Firestore dentro de la colección "usuarios"
                    db.collection("usuarios")
                        .document(user.uid)
                        .set(usuario)
                        .await()

                    // Se actualiza el estado a "Authenticated" si todo fue exitoso
                    _authState.value = AuthState.Authenticated(user)
                } ?: throw Exception("No se pudo obtener información del usuario")
            } catch (e: Exception) {
                // Si ocurre un error, se actualiza el estado a "Error"
                _authState.value = AuthState.Error(e.message ?: "Error al registrar")
            }
        }
    }

    // Método para iniciar sesión con Google y manejar la información adicional del usuario
    fun loginWithGoogle(account: GoogleSignInAccount?) {
        viewModelScope.launch {
            try {
                // Se actualiza el estado a "Loading" mientras se realiza el inicio de sesión
                _authState.value = AuthState.Loading

                // Si la cuenta de Google es nula, se lanza una excepción
                if (account == null) {
                    throw Exception("Google Sign-In failed")
                }

                // Se obtiene la credencial de autenticación de Google
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                // Se autentica al usuario en Firebase con las credenciales de Google
                val result = auth.signInWithCredential(credential).await()

                // Si el inicio de sesión es exitoso, se guarda la información del usuario en Firestore
                result.user?.let { user ->
                    val usuario = Usuario(
                        uid = user.uid,
                        email = user.email ?: "", // Se obtiene el email del usuario
                        nombre = account.displayName ?: "", // Se obtiene el nombre de la cuenta de Google
                        provider = AuthProvider.GOOGLE, // Se define el proveedor de autenticación como Google
                        telefono = "", // El teléfono es opcional para Google
                        createdAt = System.currentTimeMillis() // Se registra el momento de creación de la cuenta
                    )

                    // Se guarda el usuario en Firestore dentro de la colección "usuarios"
                    db.collection("usuarios")
                        .document(user.uid)
                        .set(usuario)
                        .await()

                    // Se actualiza el estado a "Authenticated"
                    _authState.value = AuthState.Authenticated(user)
                } ?: throw Exception("No se pudo obtener información del usuario")
            } catch (e: Exception) {
                // Si ocurre un error, se actualiza el estado a "Error"
                _authState.value = AuthState.Error(e.message ?: "Error en el inicio de sesión con Google")
            }
        }
    }

    // Método para iniciar sesión con correo electrónico y contraseña
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                // Se actualiza el estado a "Loading" mientras se realiza el inicio de sesión
                _authState.value = AuthState.Loading

                // Se intenta iniciar sesión con las credenciales proporcionadas
                auth.signInWithEmailAndPassword(email, password).await()

                // Se verifica el estado de autenticación después de iniciar sesión
                checkAuthState()
            } catch (e: Exception) {
                // Si ocurre un error, se actualiza el estado a "Error"
                _authState.value = AuthState.Error(e.message ?: "Error de autenticación")
            }
        }
    }

    // Método para cerrar sesión
    fun logout() {
        auth.signOut() // Se cierra la sesión de Firebase
        _authState.value = AuthState.Unauthenticated // Se actualiza el estado a "Unauthenticated"
    }

    // Método para restablecer la contraseña del usuario mediante correo electrónico
    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                // Se actualiza el estado a "Loading" mientras se envía el correo de restablecimiento
                _authState.value = AuthState.Loading

                // Se envía el correo para restablecer la contraseña
                auth.sendPasswordResetEmail(email).await()

                // Se actualiza el estado a "ResetPasswordSent" si el correo fue enviado exitosamente
                _authState.value = AuthState.ResetPasswordSent
            } catch (e: Exception) {
                // Si ocurre un error, se actualiza el estado a "Error"
                _authState.value = AuthState.Error(e.message ?: "Error al enviar el correo de restablecimiento")
            }
        }
    }
}

// Definición de los diferentes estados de autenticación que se gestionan en la vista
sealed class AuthState {
    object Initial : AuthState() // Estado inicial antes de realizar cualquier operación
    object Loading : AuthState() // Estado mientras se realiza una operación
    data class Authenticated(val user: FirebaseUser) : AuthState() // Estado cuando el usuario está autenticado
    object Unauthenticated : AuthState() // Estado cuando no hay usuario autenticado
    object ResetPasswordSent : AuthState() // Estado cuando se ha enviado el correo de restablecimiento de contraseña
    data class Error(val message: String) : AuthState() // Estado cuando ocurre un error, con el mensaje correspondiente
}
