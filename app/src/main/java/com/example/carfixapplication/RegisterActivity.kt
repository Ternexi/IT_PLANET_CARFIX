package com.example.carfixapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.carfixapplication.api.ApiService
import com.example.carfixapplication.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import com.example.carfixapplication.api.*
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var fullnameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var continueButton: Button

    private lateinit var goToLoginButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        fullnameEditText = findViewById(R.id.fullnameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        continueButton = findViewById(R.id.continueButton)
        goToLoginButton = findViewById(R.id.signInButtonRegister)


        continueButton.setOnClickListener {
            performRegistration()
        }

        goToLoginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performRegistration() {
        val fullname = fullnameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (fullname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val request = RegistrationRequest(fullname = fullname, email = email, password = password)

        lifecycleScope.launch {
            try {

                val response = RetrofitClient.api.registerUser(request)

                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity, "Регистрация прошла успешно!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errorMsg =
                        response.errorBody()?.string() ?: "Неизвестная ошибка регистрации"
                    Toast.makeText(this@RegisterActivity, "Ошибка: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Ошибка сети: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

    }
}