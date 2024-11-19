package com.example.lumvida.network.api

import com.example.lumvida.network.model.NominatimResponse
import com.example.lumvida.network.model.LoginRequest
import com.example.lumvida.network.model.InfraestructuraResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("api/NucleoDigital")
    suspend fun getTiposInfraestructura(
        @Body loginRequest: LoginRequest,
        @Header("Authorization") token: String
    ): Response<InfraestructuraResponse>

    @GET("search")
    suspend fun searchLocation(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("limit") limit: Int = 5
    ): List<NominatimResponse>

    @GET("https://overpass-api.de/api/interpreter")
    suspend fun getStreetGeometry(
        @Query("data") query: String
    ): ResponseBody
}