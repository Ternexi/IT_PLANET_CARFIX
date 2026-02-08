// Файл: app/src/main/java/com/example/carfixapplication/api/AuthInterceptor.kt
package com.example.carfixapplication.api

import android.content.Context
import android.content.Intent
import com.example.carfixapplication.App // Убедитесь, что этот импорт правильный
import com.example.carfixapplication.LoginActivity // Импортируем LoginActivity
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val context = App.instance

        val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("USER_TOKEN", null)

        val requestBuilder = originalRequest.newBuilder()

        // Если токен есть, добавляем его в заголовок
        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()
        // Отправляем запрос
        val response = chain.proceed(request)

        // --- САМАЯ ВАЖНАЯ ЧАСТЬ ---
        // Проверяем ответ от сервера
        if (response.code == 401) { // 401 - это код ошибки "Unauthorized"
            // Если токен невалиден (просрочен, неверный):
            // 1. Стираем старый токен
            prefs.edit().remove("USER_TOKEN").apply()

            // 2. Создаем намерение (Intent) для перехода на экран входа
            val intent = Intent(context, LoginActivity::class.java).apply {
                // Эти флаги говорят, что нужно очистить историю экранов и создать новую задачу
                // Это не даст пользователю кнопкой "назад" вернуться на экран, требующий логина
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            // 3. Запускаем LoginActivity
            context.startActivity(intent)
        }

        return response
    }
}
