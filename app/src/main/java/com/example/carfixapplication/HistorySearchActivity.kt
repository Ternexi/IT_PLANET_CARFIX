package com.example.carfixapplication
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carfixapplication.api.RetrofitClient
import com.example.carfixapplication.databinding.ActivityHistorySearchBinding
import kotlinx.coroutines.launch

class HistorySearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorySearchBinding
    private lateinit var ordersAdapter: OrdersAdapter // 1. Объявляем адаптер

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView() // 2. Вызываем настройку RecyclerView

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.searchHistory.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    val carNumber = query.trim().uppercase()
                    performSearch(carNumber)
                }
                binding.searchHistory.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    // 3. Метод для первоначальной настройки RecyclerView
    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter(emptyList()) { selectedOrder ->
            val intent = Intent(this, OrderDetailsActivity::class.java)
            intent.putExtra("ORDER_DATA", selectedOrder)
            startActivity(intent)
        }
        binding.main.apply {
            adapter = ordersAdapter
            layoutManager = LinearLayoutManager(this@HistorySearchActivity)
        }
    }



    private fun performSearch(carNumber: String) {
        Log.d("SearchHistory", "Начинаем поиск для номера: $carNumber")

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getOrdersByCarNumber(carNumber)

                if (response.isSuccessful) {
                    val orders = response.body()
                    if (orders != null && orders.isNotEmpty()) {
                        Log.d("SearchHistory", "Успешно! Найдено заказов: ${orders.size}")
                        // 4. ПЕРЕДАЕМ ДАННЫЕ В АДАПТЕР для отображения
                        ordersAdapter.updateOrders(orders)
                    } else {
                        Log.d("SearchHistory", "Заказы для номера $carNumber не найдены.")
                        ordersAdapter.updateOrders(emptyList()) // Очищаем список, если ничего не найдено
                        Toast.makeText(this@HistorySearchActivity, "Ничего не найдено", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("SearchHistory", "Ошибка от сервера: ${response.code()}")
                    ordersAdapter.updateOrders(emptyList()) // Очищаем список при ошибке
                    Toast.makeText(this@HistorySearchActivity, "Ошибка сервера: ${response.code()}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("SearchHistory", "Ошибка сети или выполнения запроса", e)
                ordersAdapter.updateOrders(emptyList()) // Очищаем список при ошибке
                Toast.makeText(this@HistorySearchActivity, "Ошибка сети", Toast.LENGTH_SHORT).show()
            }
        }
    }
}