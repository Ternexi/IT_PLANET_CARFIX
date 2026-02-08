// Файл: RetrofitClient.kt
package com.example.carfixapplication.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://192.168.31.238:3000/"

    // Логгер
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }


    // OkHttpClient который включает  логгер и перехватчик токена
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor()) // Перехватчик
        .addInterceptor(loggingInterceptor) // Логгер лучше ставить после, чтобы видеть финальный запрос
        .build()


    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // <--- Используем наш OkHttpClient с токеном
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
