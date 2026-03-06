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
import retrofit2.Response

class HistorySearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorySearchBinding
    private lateinit var ordersAdapter: OrdersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

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

    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter(emptyList()) { selectedOrder ->

            val intent = Intent(this, OrderDetailsActivity::class.java).apply {


                putExtra(OrderDetailsActivity.EXTRA_ORDER_ID, selectedOrder.id)

                putExtra("EXTRA_ORDER_TIME", selectedOrder.time)
                putExtra("EXTRA_ORDER_COST", selectedOrder.cost)
                putExtra("EXTRA_CAR_NUMBER", selectedOrder.car_number)
                putExtra("EXTRA_PHONE_NUMBER", selectedOrder.phone_number)

            }

            startActivity(intent)
        }

        binding.recyclerViewHistory.apply {
            adapter = ordersAdapter
            layoutManager = LinearLayoutManager(this@HistorySearchActivity)
        }
    }



    private fun performSearch(carNumber: String) {
        Log.d("SearchHistory", "Начинаем поиск для номера: $carNumber")

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getOrdersByCarNumber(carNumber)
                Log.d("HISTORY_DEBUG", "Ответ от сервера: ${response.code()}")

                if (response.isSuccessful) {
                    val orders = response.body()
                    Log.d("HISTORY_DEBUG", "Список заказов: $orders")

                    if (orders != null && orders.isNotEmpty()) {
                        Log.d("HISTORY_DEBUG", "ID первого заказа: ${orders[0].id}")
                        ordersAdapter.updateOrders(orders)
                    } else {
                        Log.d("HISTORY_DEBUG", "Сервер вернул пустой список")

                        ordersAdapter.updateOrders(emptyList())
                        Toast.makeText(this@HistorySearchActivity, "Ничего не найдено", Toast.LENGTH_SHORT).show()
                    }
                } else {

                    val errorMsg = response.errorBody()?.string()
                    Log.e("HISTORY_DEBUG", "Ошибка сервера: $errorMsg")
                    ordersAdapter.updateOrders(emptyList())
                    Toast.makeText(this@HistorySearchActivity, "Ошибка сервера: ${response.code()}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("HISTORY_DEBUG", "Сбой при поиске: ${e.message}", e)
                ordersAdapter.updateOrders(emptyList())
                Toast.makeText(this@HistorySearchActivity, "Ошибка сети", Toast.LENGTH_SHORT).show()
            }
        }
    }
}