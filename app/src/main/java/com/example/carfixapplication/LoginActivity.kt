package com.example.carfixapplication

import android.content.Context
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

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var goToRegisterButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Находим View-элементы по их ID
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.signInButton)
        goToRegisterButton = findViewById(R.id.singUp)

        // 2. Устанавливаем слушатель на кнопку "Войти"
        loginButton.setOnClickListener {
            performLogin()
        }

        goToRegisterButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите email и пароль", Toast.LENGTH_SHORT).show()
            return
        }

        val loginRequest = ApiService.LoginRequest(email = email, password = password)

        RetrofitClient.api.loginUser(loginRequest).enqueue(object : Callback<ApiService.LoginResponse> {
            override fun onResponse(call: Call<ApiService.LoginResponse>, response: Response<ApiService.LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val token = loginResponse?.token

                    if (token != null) {
                        saveAuthToken(token)
                        Toast.makeText(this@LoginActivity, "Вход выполнен успешно!", Toast.LENGTH_SHORT).show()
                        goToMainActivity()
                    } else {
                        Toast.makeText(this@LoginActivity, "Ошибка: токен не получен", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Ошибка: неверный email или пароль", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ApiService.LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun saveAuthToken(token: String) {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("USER_TOKEN", token).apply()
    }
    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
