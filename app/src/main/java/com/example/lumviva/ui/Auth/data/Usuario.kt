package com.example.lumviva.ui.Auth.data

enum class AuthProvider {
    EMAIL,
    GOOGLE
}

data class Usuario(
    val uid: String = "",
    val email: String = "",
    val nombre: String = "",
    val provider: AuthProvider = AuthProvider.EMAIL,
    val telefono: String = "",
    val createdAt: Long = 0
)