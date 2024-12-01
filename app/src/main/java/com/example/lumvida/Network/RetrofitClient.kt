package com.example.lumvida.network

import android.util.Log
import com.example.lumvida.network.api.ApiService
import com.example.lumvida.network.model.NominatimResponse
import com.google.gson.GsonBuilder
import okhttp3.ConnectionSpec
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import java.util.Arrays

object RetrofitClient {
    private const val BASE_URL = "http://comedatos.qroo.gob.mx/"
    private const val NOMINATIM_BASE_URL = "http://nominatim.openstreetmap.org/"

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    // DNS personalizado que soporta tanto IPv4 como IPv6
    private val customDns = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            return try {
                val addresses = Dns.SYSTEM.lookup(hostname)
                addresses.shuffled() // Mezclar para alternar entre IPv4 e IPv6
            } catch (e: Exception) {
                Log.e("RetrofitClient", "Error DNS: ${e.message}")
                listOf(InetAddress.getByName(hostname))
            }
        }
    }

    // Configuración mejorada de OkHttpClient
    private fun createOkHttpClient(timeout: Long = 5L): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val newUrl = originalRequest.url.newBuilder().build()

                val newRequest = originalRequest.newBuilder()
                    .url(newUrl)
                    .header("User-Agent", "LumVida-Android-App")
                    .header("Connection", "keep-alive")
                    .header("Accept", "*/*")
                    .build()

                try {
                    val response = chain.proceed(newRequest)

                    response.body?.let { responseBody ->
                        val bodyString = responseBody.string()
                        Log.d("RetrofitClient", "Response Body: $bodyString")

                        val newResponseBody = okhttp3.ResponseBody.create(
                            responseBody.contentType(),
                            bodyString
                        )

                        return@addInterceptor response.newBuilder()
                            .body(newResponseBody)
                            .build()
                    }

                    response
                } catch (e: Exception) {
                    Log.e("RetrofitClient", "Error en la conexión: ${e.message}")
                    throw e
                }
            }
            .addInterceptor(loggingInterceptor)
            .connectionSpecs(Arrays.asList(
                ConnectionSpec.CLEARTEXT,  // Añadir esto para permitir HTTP
                ConnectionSpec.MODERN_TLS,
                ConnectionSpec.COMPATIBLE_TLS
            ))
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .dns(customDns)
            .build()
    }

    private val apiRetrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(createOkHttpClient(10L)) // Aumentado el timeout para la API principal
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val nominatimRetrofit = Retrofit.Builder()
        .baseUrl(NOMINATIM_BASE_URL)
        .client(createOkHttpClient(15L)) // Timeout específico para Nominatim
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val overpassRetrofit = Retrofit.Builder()
        .baseUrl("https://overpass-api.de/")
        .client(createOkHttpClient(30L)) // Timeout más largo para Overpass
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val overpassService: ApiService = overpassRetrofit.create(ApiService::class.java)
    val apiService: ApiService = apiRetrofit.create(ApiService::class.java)
    val nominatimService: ApiService = nominatimRetrofit.create(ApiService::class.java)

    // Función para verificar el estado de la conexión
    fun isOnline(context: android.content.Context): Boolean {
        val connectivityManager =
            context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager

        val networkCapabilities = connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)
        }

        return networkCapabilities?.let {
            when {
                it.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> true
                it.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                it.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } ?: false
    }

    // Función para obtener el tipo de conexión
    fun getConnectionType(context: android.content.Context): String {
        val connectivityManager =
            context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager

        val networkCapabilities = connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)
        }

        return networkCapabilities?.let {
            when {
                it.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                it.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> "Datos móviles"
                it.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                else -> "Desconocido"
            }
        } ?: "Sin conexión"
    }

    // Crear una interfaz específica para Nominatim
    interface NominatimService {
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
    }

}