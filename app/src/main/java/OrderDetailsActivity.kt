package com.example.carfixapplication

import android.content.Context
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
import java.util.TimeZone


fun formatApiDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "Не указана"

    val formatsToTry = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm"
    )

    for (formatString in formatsToTry) {
        try {
            val parser = SimpleDateFormat(formatString, Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")

            val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

            val date = parser.parse(isoDate)
            if (date != null) {
                return formatter.format(date)
            }
        } catch (e: Exception) {
            continue
        }
    }

    return isoDate
}
class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val orderId = intent.getIntExtra(EXTRA_ORDER_ID, -1)
        val orderTime = intent.getStringExtra("EXTRA_ORDER_TIME")
        val orderCost = intent.getIntExtra("EXTRA_ORDER_COST", 0)
        val carNumber = intent.getStringExtra("EXTRA_CAR_NUMBER")
        val rawPhone = intent.getStringExtra("EXTRA_PHONE_NUMBER")
        val phoneNumber = if (rawPhone.isNullOrBlank()) "---" else rawPhone

        if (orderId == -1) {
            Toast.makeText(this, "Ошибка: ID заказа не получен", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.orderIdDetails.text = "Заказ №$orderId"
        binding.orderCarNumber.text = "Номер: $carNumber"
        binding.orderPhoneNumber.text = "Тел: $phoneNumber"
        binding.orderTime.text = "Дата: ${formatApiDate(orderTime)}"
        binding.orderCost.text = "Итого к оплате: $orderCost р."
        binding.zapchasti.text = "Загрузка деталей..."

        binding.hiddenDefect.visibility = View.GONE
        binding.hiddenDefectName.visibility = View.GONE
        binding.hiddenDefectCostValue.visibility = View.GONE

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

                    if (!fullOrder?.phone_number.isNullOrBlank()) {
                        val phone = fullOrder?.phone_number ?: ""
                        binding.orderPhoneNumber.text = "Тел: $phone"

                        binding.orderPhoneNumber.setOnClickListener {
                            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("phone", phone)
                            clipboard.setPrimaryClip(clip)

                            Toast.makeText(this@OrderDetailsActivity, "Номер $phone скопирован", Toast.LENGTH_SHORT).show()
                        }
                    }

                    if (!fullOrder?.phone_number.isNullOrBlank()) {
                        binding.orderPhoneNumber.text = "Тел: ${fullOrder?.phone_number}"
                    } else {
                        binding.orderPhoneNumber.text = "Тел: ---"
                    }

                    Log.d("CHECK_DATA", "Пришло с сервера: $fullOrder")
                    Log.d("CHECK_DATA", "Телефон внутри объекта: ${fullOrder?.phone_number}")
                    Log.d("DEBUG_ORDER", "Дефектов пришло: ${fullOrder?.hidden_defects?.size}")

                    if (!fullOrder?.phone_number.isNullOrEmpty()) {
                        binding.orderPhoneNumber.text = "Тел: ${fullOrder?.phone_number}"
                    }

                    val itemsString = fullOrder?.repair_items?.joinToString("\n") {
                        " • ${it.name}: ${it.price} р."
                    } ?: "Детали не указаны"
                    binding.zapchasti.text = itemsString

                    if (fullOrder?.hidden_defects != null && fullOrder.hidden_defects.isNotEmpty()) {
                        val defectsDescription = fullOrder.hidden_defects.joinToString("\n") {
                            " • ${it.description}"
                        }
                        val defectsTotalCost = fullOrder.hidden_defects.sumOf { it.cost }

                        binding.hiddenDefectName.text = defectsDescription
                        binding.hiddenDefectCostValue.text = "Стоимость доп. работ: $defectsTotalCost р."

                        binding.hiddenDefect.visibility = View.VISIBLE
                        binding.hiddenDefectName.visibility = View.VISIBLE
                        binding.hiddenDefectCostValue.visibility = View.VISIBLE
                    }

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
