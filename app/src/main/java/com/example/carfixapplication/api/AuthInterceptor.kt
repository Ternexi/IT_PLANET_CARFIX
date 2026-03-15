package com.example.carfixapplication.api

import android.content.Context
import com.example.carfixapplication.App
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        // Берем токен из памяти
        val prefs = App.instance.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("USER_TOKEN", null)

        val requestBuilder = originalRequest.newBuilder()

        // Добавляем заголовок только если токен есть
        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        // Просто отправляем запрос дальше без лишних действий
        return chain.proceed(requestBuilder.build())
    }
}