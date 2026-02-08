package com.example.carfixapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter // <-- Возвращаем ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView // <-- Возвращаем ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.carfixapplication.api.Order
import com.example.carfixapplication.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchCarActivity : AppCompatActivity() {

    // 1. Возвращаем старые типы переменных
    private lateinit var listView: ListView
    private lateinit var userData: EditText
    private lateinit var saveButton: Button
    private lateinit var backButton: Button
    private lateinit var adapter: ArrayAdapter<Order> // Используем простой ArrayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_car)

        // 2. Находим View-элементы, теперь ищем ListView
        listView = findViewById(R.id.searchResultsRecyclerView)
        userData = findViewById(R.id.user_data)
        saveButton = findViewById(R.id.button)
        backButton = findViewById(R.id.Back_button)

        // 3. Настраиваем отступы
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 4. Возвращаем старую настройку адаптера
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<Order>())
        listView.adapter = adapter

        // 5. Загружаем данные из сети
        loadHistory()

        // 6. Настраиваем слушателей для кнопок
        saveButton.setOnClickListener {
            val text = userData.text.toString().trim().uppercase()
            if (text.isNotEmpty()) {
                saveAndReturn(text)
            } else {
                Toast.makeText(this, "Введите номер автомобиля", Toast.LENGTH_SHORT).show()
            }
        }

        backButton.setOnClickListener {
            finish()
        }

        // 7. Исправленный обработчик клика для ListView
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedOrder = adapter.getItem(position)
            if (selectedOrder != null) {

                // Создаем "посылку" и кладем в нее данные по частям,
                // используя правильные ключи, которые ожидает OrderDetailsActivity
                val intent = Intent(this, OrderDetailsActivity::class.java).apply {
                    putExtra("EXTRA_ORDER_ID", selectedOrder.id)
                    putExtra("EXTRA_ORDER_TIME", selectedOrder.time)
                    putExtra("EXTRA_ORDER_COST", selectedOrder.cost)
                    putExtra("EXTRA_CAR_NUMBER", selectedOrder.car_number)
                }
                startActivity(intent)
            }
        }

    }

    private fun saveAndReturn(carNumber: String) {
        LocalCache.saveNumber(this, carNumber)
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    private fun loadHistory() {
        RetrofitClient.api.getOrders().enqueue(object : Callback<List<Order>> {
            override fun onResponse(call: Call<List<Order>>, response: Response<List<Order>>) {
                if (isFinishing || isDestroyed) return
                if (response.isSuccessful) {
                    val ordersList = response.body() ?: emptyList()
                    // Возвращаем старый способ обновления данных
                    adapter.clear()
                    adapter.addAll(ordersList)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Toast.makeText(this@SearchCarActivity, "Ошибка сервера: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<Order>>, t: Throwable) {
                if (isFinishing || isDestroyed) return
                Toast.makeText(this@SearchCarActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}