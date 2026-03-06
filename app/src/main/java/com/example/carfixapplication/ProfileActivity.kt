package com.example.carfixapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvName = findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)


        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val userName = prefs.getString("USER_NAME", "Имя не указано")
        val userEmail = prefs.getString("USER_EMAIL", "Email не указан")

        tvName.text = "Имя: $userName"
        tvEmail.text = "Email: $userEmail"

        btnLogout.setOnClickListener {
            prefs.edit().clear().apply()

            LocalCache.clearNumber(this)

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        }
        val buttonClick = findViewById<ImageButton>(R.id.Back_buttoni)
        buttonClick.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}