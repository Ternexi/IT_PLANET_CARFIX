package com.example.carfixapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.carfixapplication.api.ApiService
import com.example.carfixapplication.api.*
import com.example.carfixapplication.api.RetrofitClient
import kotlinx.coroutines.launch
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

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.signInButton)
        goToRegisterButton = findViewById(R.id.singUp)

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

        val loginRequest = LoginRequest(email = email, password = password)

        lifecycleScope.launch {
            try {

                val response = RetrofitClient.api.loginUser(loginRequest)

                if (response.isSuccessful) {
                    val loginResponse = response.body()

                    if (loginResponse?.token != null && loginResponse.user != null) {

                        val token = loginResponse.token
                        val id = loginResponse.user.id
                        val name = loginResponse.user.name
                        val email = loginResponse.user.email

                        saveUserData(token, id, name, email)


                        Toast.makeText(this@LoginActivity, "Вход выполнен успешно!", Toast.LENGTH_SHORT).show()
                        goToMainActivity()
                    } else {
                        Toast.makeText(this@LoginActivity, "Ошибка: токен не получен", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Ошибка: неверный email или пароль", Toast.LENGTH_LONG).show()
                }
            }catch (e: Exception) {
                Log.e("API_ERROR", e.message ?: "Ошибка сети")
                Toast.makeText(this@LoginActivity, "Ошибка сети: ${e.message}", Toast.LENGTH_LONG).show()

            }
        }
    }

    private fun saveUserData(token: String, id: String, name: String, email: String) {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply{
            putString("USER_TOKEN", token)
            putString("USER_ID", id)
            putString("USER_NAME", name)
            putString("USER_EMAIL", email)
            apply()
        }
    }
    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
