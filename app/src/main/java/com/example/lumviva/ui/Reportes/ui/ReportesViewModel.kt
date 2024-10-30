package com.example.lumviva.ui.Reportes.ui // Define el paquete donde se encuentra esta clase.

import androidx.lifecycle.ViewModel // Importa la clase ViewModel para manejar la lógica de la interfaz de usuario.
import androidx.lifecycle.ViewModelProvider // Importa la clase para crear instancias de ViewModels.
import androidx.lifecycle.viewModelScope // Importa la extensión para ejecutar corutinas en el contexto del ViewModel.
import com.example.lumviva.ui.Auth.ui.AuthState // Importa el estado de autenticación.
import com.example.lumviva.ui.Auth.ui.AuthViewModel // Importa el ViewModel de autenticación.
import com.google.firebase.auth.FirebaseAuth // Importa la clase para autenticación de Firebase.
import com.google.firebase.firestore.FirebaseFirestore // Importa la clase para Firestore de Firebase.
import kotlinx.coroutines.flow.MutableStateFlow // Importa la clase para flujos mutables.
import kotlinx.coroutines.flow.StateFlow // Importa la clase para flujos de estado.
import kotlinx.coroutines.launch // Importa la función para lanzar corutinas.
import kotlinx.coroutines.tasks.await // Importa la función para convertir tareas de Firebase en corutinas.

class ReportesViewModel(private val authViewModel: AuthViewModel) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance() // Obtiene la instancia de FirebaseAuth.
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance() // Obtiene la instancia de Firestore.
    private val _userName = MutableStateFlow("") // Crea un flujo mutable para el nombre de usuario.
    val userName: StateFlow<String> = _userName // Crea un flujo de estado para el nombre de usuario.

    private val _isAuthenticated = MutableStateFlow(false) // Crea un flujo mutable para el estado de autenticación.
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated // Crea un flujo de estado para la autenticación.

    init {
        viewModelScope.launch { // Inicia una corutina en el contexto del ViewModel.
            // Verificar estado inicial
            checkCurrentUser() // Comprueba si hay un usuario autenticado al inicio.

            // Observar cambios futuros
            authViewModel.authState.collect { state -> // Escucha los cambios en el estado de autenticación.
                when (state) {
                    is AuthState.Authenticated -> { // Si el usuario está autenticado.
                        _isAuthenticated.value = true // Actualiza el estado de autenticación.
                        getUserNameFromFirestore(state.user.uid) // Obtiene el nombre de usuario desde Firestore.
                    }
                    else -> { // Si el usuario no está autenticado.
                        _isAuthenticated.value = false // Actualiza el estado de autenticación.
                        _userName.value = "" // Limpia el nombre de usuario.
                    }
                }
            }
        }
    }

    private fun checkCurrentUser() { // Verifica si hay un usuario actualmente autenticado.
        auth.currentUser?.let { user -> // Si hay un usuario autenticado.
            _isAuthenticated.value = true // Actualiza el estado de autenticación.
            viewModelScope.launch { // Inicia una corutina para obtener el nombre de usuario.
                getUserNameFromFirestore(user.uid) // Obtiene el nombre de usuario desde Firestore.
            }
        } ?: run { // Si no hay un usuario autenticado.
            _isAuthenticated.value = false // Actualiza el estado de autenticación.
            _userName.value = "" // Limpia el nombre de usuario.
        }
    }

    private suspend fun getUserNameFromFirestore(uid: String) { // Función para obtener el nombre de usuario desde Firestore.
        try {
            val userDoc = db.collection("usuarios").document(uid).get().await() // Obtiene el documento del usuario desde Firestore.
            if (userDoc.exists()) { // Verifica si el documento existe.
                val nombre = userDoc.getString("nombre") // Intenta obtener el nombre del documento.
                if (!nombre.isNullOrEmpty()) { // Si el nombre no está vacío.
                    _userName.value = nombre // Actualiza el nombre de usuario.
                    return // Sale de la función.
                }
            }

            // Fallback a displayName o email si no se encuentra el nombre en Firestore
            auth.currentUser?.let { user -> // Si hay un usuario autenticado.
                _userName.value = when { // Asigna el nombre de usuario según las prioridades.
                    !user.displayName.isNullOrEmpty() -> user.displayName!! // Usa displayName si existe.
                    !user.email.isNullOrEmpty() -> user.email!!.substringBefore("@") // Usa el correo (sin dominio) si no hay displayName.
                    else -> "Usuario" // Usa un nombre por defecto si no hay ninguno.
                }
            }
        } catch (e: Exception) { // Captura cualquier excepción durante la obtención de datos.
            // En caso de error, usar el fallback
            auth.currentUser?.let { user -> // Si hay un usuario autenticado.
                _userName.value = when { // Asigna el nombre de usuario según las prioridades.
                    !user.displayName.isNullOrEmpty() -> user.displayName!! // Usa displayName si existe.
                    !user.email.isNullOrEmpty() -> user.email!!.substringBefore("@") // Usa el correo (sin dominio) si no hay displayName.
                    else -> "Usuario" // Usa un nombre por defecto si no hay ninguno.
                }
            }
        }
    }

    fun logout() { // Función para cerrar sesión.
        authViewModel.logout() // Llama a la función de cerrar sesión en el ViewModel de autenticación.
    }

    class Factory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory { // Clase para crear instancias del ViewModel.
        @Suppress("UNCHECKED_CAST") // Suprime advertencias de cast.
        override fun <T : ViewModel> create(modelClass: Class<T>): T { // Función para crear el ViewModel.
            if (modelClass.isAssignableFrom(ReportesViewModel::class.java)) { // Verifica si el modelo es de tipo ReportesViewModel.
                return ReportesViewModel(authViewModel) as T // Retorna la instancia del ViewModel.
            }
            throw IllegalArgumentException("Unknown ViewModel class") // Lanza una excepción si el modelo no es reconocido.
        }
    }
}
