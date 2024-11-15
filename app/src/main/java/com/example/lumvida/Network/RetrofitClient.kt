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

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()

            // Construir la URL correctamente
            val newUrl = originalRequest.url.newBuilder()
                .build()

            val newRequest = originalRequest.newBuilder()
                .url(newUrl)
                .build()

            val response = chain.proceed(newRequest)

            // Log de la respuesta
            response.body?.let { responseBody ->
                val bodyString = responseBody.string()
                Log.d("RetrofitClient", "Response Body: $bodyString")

                // Recrear el body ya que .string() lo consume
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
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(ApiService::class.java)
}