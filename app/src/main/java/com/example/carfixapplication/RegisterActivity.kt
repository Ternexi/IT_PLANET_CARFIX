package com.example.carfixapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.carfixapplication.api.ApiService
import com.example.carfixapplication.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var fullnameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var continueButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // 1. Все наши View-компоненты по их ID
        fullnameEditText = findViewById(R.id.fullnameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        continueButton = findViewById(R.id.continueButton)

        // 2. Слушатель нажатия на кнопку "Продолжить"
        continueButton.setOnClickListener {
            // Вызываем функцию, которая выполнит регистрацию
            performRegistration()
        }
    }

    private fun performRegistration() {
        // 3. Собираем текст из полей ввода
        val fullname = fullnameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // 4. Простая проверка, что поля не пустые
        if (fullname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        // 5. Создаем объект с данными для отправки на сервер
        val request = ApiService.RegistrationRequest(fullname = fullname, email = email, password = password)

        // 6. Вызываем API-метод
        RetrofitClient.api.registerUser(request).enqueue(object :
            Callback<ApiService.RegistrationResponse> {

            override fun onResponse(
                call: Call<ApiService.RegistrationResponse>,
                response: Response<ApiService.RegistrationResponse>
            ) {
                if (response.isSuccessful) {
                    // Сервер успешно обработал запрос
                    Toast.makeText(
                        this@RegisterActivity,
                        "Регистрация прошла успешно!",
                        Toast.LENGTH_LONG
                    ).show()
                    // Закрываем экран регистрации и возвращаемся на экран входа
                    finish()
                } else {
                    // Сервер вернул ошибку (например, email уже занят)
                    val errorMsg =
                        response.errorBody()?.string() ?: "Неизвестная ошибка регистрации"
                    Toast.makeText(this@RegisterActivity, "Ошибка: $errorMsg", Toast.LENGTH_LONG)
                        .show()
                }
            }

            override fun onFailure(call: Call<ApiService.RegistrationResponse>, t: Throwable) {
                // Ошибка сети (нет интернета, сервер недоступен)
                Toast.makeText(
                    this@RegisterActivity,
                    "Ошибка сети: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}