package com.example.lumvida.ui.Auth.data

// Definición de un enum para los proveedores de autenticación
enum class AuthProvider {
    EMAIL,   // Autenticación mediante correo electrónico
    GOOGLE   // Autenticación mediante Google
}

// Definición de la clase de datos "Usuario" para representar a un usuario en el sistema
data class Usuario(
    val uid: String = "",                  // Identificador único del usuario, inicializado como una cadena vacía
    val email: String = "",                // Correo electrónico del usuario, inicializado como una cadena vacía
    val nombre: String = "",               // Nombre del usuario, inicializado como una cadena vacía
    val provider: AuthProvider = AuthProvider.EMAIL, // Proveedor de autenticación, por defecto es EMAIL
    val telefono: String = "",             // Número de teléfono del usuario, inicializado como una cadena vacía
    val createdAt: Long = 0                // Fecha de creación en formato de tiempo (timestamp), inicializado como 0
)
