/*ViewModel de Kotlin para manejar el proceso de inicio de sesión en una aplicación
 Android utilizando Firebase Authentication. La clase LoginViewModel gestiona los
  diferentes estados del inicio de sesión (inicial, cargando, éxito, necesita número de
   teléfono, error), implementa validaciones para el correo electrónico y la contraseña,
    y traduce los mensajes de error de Firebase a mensajes más amigables para el usuario.
     Utiliza corrutinas y StateFlow para manejar de manera reactiva los estados de autenticación,
      y proporciona métodos para iniciar sesión con correo electrónico y cuenta de Google, así como
      para enviar un número de teléfono cuando es requerido. Incluye además una clase Factory para
      la inyección de dependencias del ViewModel.*/



package com.example.lumvida.ui.login.ui // Define el paquete donde se encuentra esta clase.

import androidx.lifecycle.ViewModel // Importa la clase ViewModel para manejar el estado de la interfaz de usuario.
import androidx.lifecycle.ViewModelProvider // Importa la clase para crear instancias de ViewModel.
import androidx.lifecycle.viewModelScope // Importa el scope de corutinas para el ViewModel.
import com.example.lumvida.ui.Auth.ui.AuthState // Importa el estado de autenticación desde AuthState.
import com.example.lumvida.ui.Auth.ui.AuthViewModel // Importa el ViewModel de autenticación.
import com.google.android.gms.auth.api.signin.GoogleSignInAccount // Importa la cuenta de Google para el inicio de sesión.
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow // Importa la clase para flujos de estado mutables.
import kotlinx.coroutines.flow.StateFlow // Importa la interfaz de flujo de estado.
import kotlinx.coroutines.launch // Importa la función para lanzar corutinas.

// Función auxiliar para traducir mensajes de error de Firebase
private fun translateFirebaseError(errorMessage: String): String {
    return when {
        errorMessage.contains("The supplied auth credential is incorrect") ->
            "Las credenciales proporcionadas son incorrectas"
        errorMessage.contains("malformed or has expired") ->
            "Las credenciales proporcionadas son inválidas o han expirado"
        errorMessage.contains("There is no user record") ->
            "No existe una cuenta con este correo electrónico"
        errorMessage.contains("The email address is badly formatted") ->
            "El formato del correo electrónico es inválido"
        errorMessage.contains("The password is invalid") ->
            "La contraseña es incorrecta"
        errorMessage.contains("network error") ->
            "Error de conexión. Por favor, verifica tu conexión a internet"
        errorMessage.contains("INVALID_LOGIN_CREDENTIALS") ->
            "Correo electrónico o contraseña incorrectos"
        else -> "Error de autenticación: $errorMessage"
    }
}

class LoginViewModel(private val authViewModel: AuthViewModel) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState

    init {
        viewModelScope.launch {
            authViewModel.authState.collect { authState ->
                _loginState.value = when (authState) {
                    is AuthState.NeedsPhoneNumber -> LoginState.NeedsPhoneNumber(authState.user)
                    is AuthState.Initial -> LoginState.Initial
                    is AuthState.Loading -> LoginState.Loading
                    is AuthState.Authenticated -> LoginState.Success
                    is AuthState.Unauthenticated -> LoginState.Initial
                    is AuthState.Error -> LoginState.Error(translateFirebaseError(authState.message))
                    is AuthState.ResetPasswordSent -> LoginState.Initial
                }
            }
        }
    }

    fun submitPhoneNumber(user: FirebaseUser, phone: String) {
        when {
            phone.length != 10 -> {
                _loginState.value = LoginState.Error("El número de teléfono debe tener exactamente 10 dígitos")
                return
            }
            !phone.all { it.isDigit() } -> {
                _loginState.value = LoginState.Error("El número de teléfono solo debe contener dígitos")
                return
            }
            else -> {
                authViewModel.updateUserPhone(user, phone)
            }
        }
    }

    fun login(email: String, password: String) {
        if (!isValidEmail(email)) {
            _loginState.value = LoginState.Error("El formato del correo electrónico no es válido")
            return
        }
        if (password.isBlank()) {
            _loginState.value = LoginState.Error("La contraseña no puede estar vacía")
            return
        }
        authViewModel.login(email, password)
    }

    fun loginWithGoogle(account: GoogleSignInAccount) {
        if (account.email == null) {
            _loginState.value = LoginState.Error("No se pudo obtener el correo electrónico de Google")
            return
        }
        authViewModel.loginWithGoogle(account)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
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
    data class NeedsPhoneNumber(val user: FirebaseUser) : LoginState()
    data class Error(val message: String) : LoginState()
}
