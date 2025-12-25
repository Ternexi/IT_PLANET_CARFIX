package com.example.carfixapplication

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.carfixapplication.api.Order
import com.example.carfixapplication.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

// Ваша функция formatApiDate остается без изменений
fun formatApiDate(isoDate: String?): String {
    if (isoDate == null) {
        return "Не указана"
    }
    try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val date = parser.parse(isoDate)
        return if (date != null) formatter.format(date) else isoDate
    } catch (e: Exception) {
        return isoDate
    }
}

class OrderDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)

        // ======================= НАЧАЛО ИСПРАВЛЕНИЙ =======================

        // 1. ПОЛУЧАЕМ ДАННЫЕ ПО-НОВОМУ, по ключам, которые передает SearchCarActivity
        val orderId = intent.getIntExtra("EXTRA_ORDER_ID", -1)
        val orderTime = intent.getStringExtra("EXTRA_ORDER_TIME")
        val orderCost = intent.getIntExtra("EXTRA_ORDER_COST", 0)
        val carNumber = intent.getStringExtra("EXTRA_CAR_NUMBER") ?: "---"

        // Проверка, что хотя бы ID пришел корректно
        if (orderId == -1) {
            Log.e("DEBUG_ORDER", "Ключевые данные (ID заказа) не пришли!")
            Toast.makeText(this, "Ошибка: ID заказа не получен", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. Находим все TextView на экране
        val tvOrderId = findViewById<TextView>(R.id.order_id_details)
        val tvCarNumber = findViewById<TextView>(R.id.orderCarNumber)
        val tvOrderDate = findViewById<TextView>(R.id.orderTime)
        val tvOrderDetails = findViewById<TextView>(R.id.zapchasti)
        val tvOrderCost = findViewById<TextView>(R.id.orderCost)

        // 3. Устанавливаем базовую информацию, которую уже получили
        tvOrderId.text = "Заказ №$orderId"
        tvCarNumber.text = "Номер: $carNumber"
        tvOrderDate.text = "Дата: ${formatApiDate(orderTime)}"
        tvOrderCost.text = "Итого к оплате: $orderCost р." // Отображаем начальную стоимость
        tvOrderDetails.text = "Загрузка деталей..."

        // ======================== КОНЕЦ ИСПРАВЛЕНИЙ ========================

        // 4. Запрос подробностей (запчастей) через API по ID
        // Этот код уже правильный, так как использует orderId
        RetrofitClient.api.getOrderDetail(orderId).enqueue(object : Callback<Order> {
            override fun onResponse(call: Call<Order>, response: Response<Order>) {
                if (response.isSuccessful) {
                    val fullOrder = response.body()

                    // Формируем строку для запчастей
                    val itemsString = fullOrder?.repair_items?.joinToString("\n") {
                        " • ${it.name}: ${it.price} р."
                    } ?: "Нет деталей"

                    // Определяем итоговую стоимость из полного ответа сервера
                    val totalCost = fullOrder?.cost ?: orderCost // Если в ответе нет, берем старую

                    // Устанавливаем обновленный текст
                    tvOrderDetails.text = itemsString
                    tvOrderCost.text = "Итого к оплате: ${totalCost} р."

                } else {
                    tvOrderDetails.text = "Не удалось загрузить детали"
                }
            }

            override fun onFailure(call: Call<Order>, t: Throwable) {
                Log.e("API_ERROR", t.message ?: "Ошибка сети")
                tvOrderDetails.text = "Ошибка подключения к сети"
            }
        })
    }
}
