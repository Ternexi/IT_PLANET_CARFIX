package com.example.carfixapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carfixapplication.api.RetrofitClient
import kotlinx.coroutines.launch

class SearchCarActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var ordersAdapter: OrdersAdapter
    private lateinit var userDataInput: EditText

    private lateinit var phoneDataInput: EditText

    private lateinit var saveButton: Button
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_car)

        recyclerView = findViewById(R.id.searchResultsRecyclerView)
        userDataInput = findViewById(R.id.user_data)
        phoneDataInput = findViewById(R.id.phone_data)
        saveButton = findViewById(R.id.button)
        backButton = findViewById(R.id.Back_buttoni)

        setupRecyclerView()

        loadRecentOrders()

        backButton.setOnClickListener { finish() }

        saveButton.setOnClickListener {
            val carNumber = userDataInput.text.toString().trim().uppercase()
            val phoneNumber = phoneDataInput.text.toString().trim().uppercase()


            if (carNumber.isNotEmpty() || phoneNumber.isNotEmpty()) {
                selectCarAndExit(carNumber, phoneNumber)
            } else {
                Toast.makeText(this, "Введите номер", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter(emptyList()) { selectedOrder ->
            val intent = Intent(this, OrderDetailsActivity::class.java).apply {
                putExtra(OrderDetailsActivity.EXTRA_ORDER_ID, selectedOrder.id)
                putExtra("EXTRA_CAR_NUMBER", selectedOrder.car_number)
                putExtra("EXTRA_ORDER_COST", selectedOrder.cost)
                putExtra("EXTRA_ORDER_TIME", selectedOrder.time)
                putExtra("EXTRA_PHONE_NUMBER", selectedOrder.phone_number)
            }
            startActivity(intent)
        }
        recyclerView.apply {
            adapter = ordersAdapter
            layoutManager = LinearLayoutManager(this@SearchCarActivity)
        }
    }

    private fun loadRecentOrders() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getRecentOrders()
                if (response.isSuccessful) {
                    val recentOrders = response.body() ?: emptyList()
                    ordersAdapter.updateOrders(recentOrders)
                }else{
                    Log.e("SearchCar", "Сервер ответил ошибкой: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SearchCar", "Ошибка загрузки недавних: ${e.message}")
            }
        }
    }

    private fun selectCarAndExit(carNumber: String, phoneNumber: String) {
        LocalCache.saveNumber(this, carNumber)
        LocalCache.savePhone(this, phoneNumber)


        Toast.makeText(this, "Номер $carNumber выбран", Toast.LENGTH_SHORT).show()

        finish()
    }
}