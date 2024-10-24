package com.example.lumviva.ui.Auth.data

enum class AuthProvider {
    EMAIL,
    GOOGLE
}

data class Usuario(
    val uid: String = "",
    val email: String = "",
    val nombre: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val provider: AuthProvider = AuthProvider.EMAIL  // Por defecto EMAIL
)