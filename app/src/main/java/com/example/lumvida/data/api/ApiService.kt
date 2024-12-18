/*utiliza Retrofit para realizar solicitudes HTTP en una app Android. Contiene métodos
 para interactuar con una API, como enviar datos (POST) y obtener respuestas, buscar ubicaciones
  (GET) y recuperar información de geometría de calles. Cada método especifica la ruta del servidor,
 los parámetros y el tipo de respuesta esperada. Sirve para conectar la aplicación con servicios
  externos de forma sencilla y ordenada.*/

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
import retrofit2.http.QueryMap

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
        @Query("limit") limit: Int = 5
    ): List<NominatimResponse>

    @GET("search")
    suspend fun searchLocationWithParams(
        @QueryMap params: Map<String, String>
    ): List<NominatimResponse>

    @GET("http://overpass-api.de/api/interpreter")
    suspend fun getStreetGeometry(
        @Query("data") query: String
    ): ResponseBody
}