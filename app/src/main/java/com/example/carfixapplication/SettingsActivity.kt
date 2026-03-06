package com.example.carfixapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var l: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tutorials = arrayOf(
            "Профиль",
            "Пользовательское соглашение",
            "О приложении",
            "CarFix Premium",
            "Поиск по истории"
        )

        l = findViewById(R.id.polzovatel_information)

        val arr = ArrayAdapter(this, android.R.layout.simple_list_item_1, tutorials)
        l.adapter = arr

        l.setOnItemClickListener { _, _, position, _ ->
            when (position) {

                0 -> { val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent) }

                1 -> {  }

                2 -> { }

                3 -> {
                    val intent = Intent(this, PremiumActivity::class.java)
                    startActivity(intent)
                }

                4 -> {
                    val intent = Intent(this, HistorySearchActivity::class.java)
                    startActivity(intent)
                }
            }
        }


        val buttonClick = findViewById<ImageButton>(R.id.back_button2)
        buttonClick.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
