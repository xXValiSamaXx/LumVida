package com.example.lumvida.network.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class InfraestructuraResponse(
    val tipos: List<TipoInfraestructura>
)

data class TipoInfraestructura(
    val id: Int,
    val nombre: String
)