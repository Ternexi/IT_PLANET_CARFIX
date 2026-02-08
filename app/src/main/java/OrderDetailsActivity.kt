package com.example.carfixapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.carfixapplication.api.Order
import com.example.carfixapplication.api.RetrofitClient
import com.example.carfixapplication.databinding.ActivityOrderDetailsBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

// Вспомогательная функция для форматирования даты
fun formatApiDate(isoDate: String?): String {
    if (isoDate == null) return "Не указана"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val date = parser.parse(isoDate)
        if (date != null) formatter.format(date) else isoDate
    } catch (e: Exception) {
        isoDate
    }
}

class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val orderId = intent.getIntExtra("EXTRA_ORDER_ID", -1)
        val orderTime = intent.getStringExtra("EXTRA_ORDER_TIME")
        val orderCost = intent.getIntExtra("EXTRA_ORDER_COST", 0)
        val carNumber = intent.getStringExtra("EXTRA_CAR_NUMBER") ?: "---"

        if (orderId == -1) {
            Toast.makeText(this, "Ошибка: ID заказа не получен", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Установка базовой информации
        binding.orderIdDetails.text = "Заказ №$orderId"
        binding.orderCarNumber.text = "Номер: $carNumber"
        binding.orderTime.text = "Дата: ${formatApiDate(orderTime)}"
        binding.orderCost.text = "Итого к оплате: $orderCost р."
        binding.zapchasti.text = "Загрузка деталей..."

        // Скрываем все поля для дефектов, пока не получим данные
        binding.hiddenDefect.visibility = View.GONE
        binding.hiddenDefectName.visibility = View.GONE
        binding.hiddenDefectCostValue.visibility = View.GONE

        // Запрос полных деталей
        fetchOrderDetails(orderId, orderCost)
    }

    companion object {
        const val EXTRA_ORDER_ID = "extra_order_id"
    }

    private fun fetchOrderDetails(orderId: Int, initialCost: Int) {
        RetrofitClient.api.getOrderDetail(orderId).enqueue(object : Callback<Order> {
            override fun onResponse(call: Call<Order>, response: Response<Order>) {
                if (response.isSuccessful) {
                    val fullOrder = response.body()

                    // Обновляем список основных работ/запчастей
                    val itemsString = fullOrder?.repair_items?.joinToString("\n") {
                        " • ${it.name}: ${it.price} р."
                    } ?: "Детали не указаны"
                    binding.zapchasti.text = itemsString

                    // *** ГЛАВНОЕ ИСПРАВЛЕНИЕ - работаем с вашими ID из XML ***
                    // Проверяем, есть ли скрытые дефекты
                    if (fullOrder?.hidden_defects != null && fullOrder.hidden_defects.isNotEmpty()) {
                        // Формируем строку из всех дефектов (даже если их несколько)
                        val defectsDescription = fullOrder.hidden_defects.joinToString("\n") {
                            " • ${it.description}"
                        }
                        val defectsTotalCost = fullOrder.hidden_defects.sumOf { it.cost }

                        // Заполняем поля данными
                        binding.hiddenDefectName.text = defectsDescription
                        binding.hiddenDefectCostValue.text = "Стоимость доп. работ: $defectsTotalCost р."

                        // Делаем блок видимым
                        binding.hiddenDefect.visibility = View.VISIBLE
                        binding.hiddenDefectName.visibility = View.VISIBLE
                        binding.hiddenDefectCostValue.visibility = View.VISIBLE
                    }

                    // Обновляем итоговую стоимость
                    val totalCost = fullOrder?.cost ?: initialCost
                    binding.orderCost.text = "Итого к оплате: ${totalCost} р."

                } else {
                    binding.zapchasti.text = "Не удалось загрузить детали"
                    Log.e("API_ERROR", "Сервер ответил ошибкой: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Order>, t: Throwable) {
                Log.e("API_ERROR", t.message ?: "Ошибка сети")
                binding.zapchasti.text = "Ошибка подключения к сети"
            }
        })
    }
}
