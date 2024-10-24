package com.example.lumviva.ui.Auth.data

data class Usuario(
    val uid: String = "",
    val email: String = "",
    val nombre: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
