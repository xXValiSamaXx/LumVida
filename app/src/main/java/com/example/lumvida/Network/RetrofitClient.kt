package com.example.lumvida.network

import android.util.Log
import com.example.lumvida.network.api.ApiService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://comedatos.qroo.gob.mx/"
    private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val newUrl = originalRequest.url.newBuilder().build()

            val newRequest = originalRequest.newBuilder()
                .url(newUrl)
                .header("User-Agent", "LumVida-Android-App") // Requerido para Nominatim
                .build()

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
        }
        .addInterceptor(loggingInterceptor)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    private val apiRetrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val nominatimRetrofit = Retrofit.Builder()
        .baseUrl(NOMINATIM_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: ApiService = apiRetrofit.create(ApiService::class.java)
    val nominatimService: ApiService = nominatimRetrofit.create(ApiService::class.java)
}