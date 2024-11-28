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

class LoginViewModel(private val authViewModel: AuthViewModel) : ViewModel() { // Define la clase LoginViewModel, que extiende ViewModel.
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial) // Inicializa el flujo de estado de inicio de sesión en "Initial".
    val loginState: StateFlow<LoginState> = _loginState // Exponer el flujo de estado de inicio de sesión como un StateFlow.

    init { // Bloque de inicialización que se ejecuta al crear una instancia de LoginViewModel.
        viewModelScope.launch { // Inicia una corutina en el scope del ViewModel.
            authViewModel.authState.collect { authState -> // Recolecta el estado de autenticación desde el AuthViewModel.
                _loginState.value = when (authState) { // Actualiza el estado de inicio de sesión basado en el estado de autenticación.
                    is AuthState.NeedsPhoneNumber -> LoginState.NeedsPhoneNumber(authState.user)
                    is AuthState.Initial -> LoginState.Initial // Si el estado es "Initial", asigna "Initial" al estado de inicio de sesión.
                    is AuthState.Loading -> LoginState.Loading // Si el estado es "Loading", asigna "Loading".
                    is AuthState.Authenticated -> LoginState.Success // Si el usuario está autenticado, asigna "Success".
                    is AuthState.Unauthenticated -> LoginState.Initial // Si no está autenticado, asigna "Initial".
                    is AuthState.Error -> LoginState.Error(authState.message) // Si hay un error, asigna el mensaje de error.
                    is AuthState.ResetPasswordSent -> LoginState.Initial // Si se envió un correo de restablecimiento de contraseña, asigna "Initial".
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

    // Función para manejar el inicio de sesión con correo y contraseña.
    fun login(email: String, password: String) {
        if (!isValidEmail(email)) { // Verifica si el correo electrónico es válido.
            _loginState.value = LoginState.Error("El correo electrónico no es válido") // Si no es válido, asigna un mensaje de error.
            return // Sale de la función.
        }
        if (password.isBlank()) { // Verifica si la contraseña está vacía.
            _loginState.value = LoginState.Error("La contraseña no puede estar vacía") // Asigna un mensaje de error si está vacía.
            return // Sale de la función.
        }
        authViewModel.login(email, password) // Llama a la función de inicio de sesión del AuthViewModel con las credenciales.
    }

    // Función para manejar el inicio de sesión con Google.
    fun loginWithGoogle(account: GoogleSignInAccount) {
        if (account.email == null) { // Verifica si se obtuvo el correo electrónico de la cuenta de Google.
            _loginState.value = LoginState.Error("No se pudo obtener el correo electrónico de Google") // Asigna un mensaje de error si no se obtuvo.
            return // Sale de la función.
        }
        authViewModel.loginWithGoogle(account) // Llama a la función de inicio de sesión del AuthViewModel con la cuenta de Google.
    }

    // Función para validar el formato del correo electrónico.
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() // Usa una expresión regular para validar el correo electrónico.
    }

    // Clase interna para crear instancias del LoginViewModel.
    class Factory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST") // Suprime la advertencia de tipo no comprobado.
        override fun <T : ViewModel> create(modelClass: Class<T>): T { // Sobrescribe la función create para proporcionar una instancia de LoginViewModel.
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) { // Verifica si el modelo solicitado es LoginViewModel.
                return LoginViewModel(authViewModel) as T // Devuelve la instancia de LoginViewModel.
            }
            throw IllegalArgumentException("Unknown ViewModel class") // Lanza una excepción si la clase de modelo no es válida.
        }
    }
}

// Clase sellada que representa los diferentes estados de inicio de sesión.
sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class NeedsPhoneNumber(val user: FirebaseUser) : LoginState()
    data class Error(val message: String) : LoginState()
}
