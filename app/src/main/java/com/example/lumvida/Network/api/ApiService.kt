package com.example.lumvida.network.api

import com.example.lumvida.network.model.LoginRequest
import com.example.lumvida.network.model.InfraestructuraResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("api/NucleoDigital")
    suspend fun getTiposInfraestructura(
        @Body loginRequest: LoginRequest,
        @Header("Authorization") token: String
    ): Response<InfraestructuraResponse>
}