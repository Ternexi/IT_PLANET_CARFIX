package com.example.carfixapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.carfixapplication.api.ApiService
import com.example.carfixapplication.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    // Объявляем View-элементы
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var goToRegisterButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Находим View-элементы по их ID из activity_login.xml
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.signInButton) // ID вашей главной кнопки
        goToRegisterButton = findViewById(R.id.signUpText) // ID текста для перехода на регистрацию

        // 2. Устанавливаем слушатель на кнопку "Войти"
        loginButton.setOnClickListener {
            performLogin()
        }

        // 3. Устанавливаем слушатель на текст "Зарегистрироваться"
        goToRegisterButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin() {
        // 4. Собираем данные из полей
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Проверяем, что поля не пустые
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите email и пароль", Toast.LENGTH_SHORT).show()
            return
        }

        // 5. Создаем объект для отправки на сервер
        val loginRequest = ApiService.LoginRequest(email = email, password = password)

        // 6. Вызываем API-метод для входа
        RetrofitClient.api.loginUser(loginRequest).enqueue(object : Callback<ApiService.LoginResponse> {
            override fun onResponse(call: Call<ApiService.LoginResponse>, response: Response<ApiService.LoginResponse>) {
                if (response.isSuccessful) {
                    // Сервер вернул успешный ответ
                    val loginResponse = response.body()
                    val token = loginResponse?.token

                    if (token != null) {
                        // 7. ЕСЛИ ТОКЕН ПОЛУЧЕН - СОХРАНЯЕМ ЕГО
                        saveAuthToken(token)
                        Toast.makeText(this@LoginActivity, "Вход выполнен успешно!", Toast.LENGTH_SHORT).show()

                        // 8. Переходим на главный экран
                        goToMainActivity()
                    } else {
                        // Успешный ответ, но токена нет - странная ситуация
                        Toast.makeText(this@LoginActivity, "Ошибка: токен не получен", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    // Сервер вернул ошибку (неверный логин/пароль)
                    Toast.makeText(this@LoginActivity, "Ошибка: неверный email или пароль", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ApiService.LoginResponse>, t: Throwable) {
                // Ошибка сети (нет интернета, сервер выключен)
                Toast.makeText(this@LoginActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    /**
     * Сохраняет токен авторизации в SharedPreferences.
     * Это "запомнит" пользователя в приложении.
     */
    private fun saveAuthToken(token: String) {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("USER_TOKEN", token).apply()
    }

    /**
     * Запускает MainActivity и очищает стек, чтобы пользователь не мог вернуться
     * на экран входа кнопкой "Назад".
     */
    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
