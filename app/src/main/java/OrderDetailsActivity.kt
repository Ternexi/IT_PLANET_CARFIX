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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


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

        val orderId = intent.getStringExtra(EXTRA_ORDER_ID)
        val orderTime = intent.getStringExtra("EXTRA_ORDER_TIME")
        val orderCost = intent.getIntExtra("EXTRA_ORDER_COST", 0)
        val carNumber = intent.getStringExtra("EXTRA_CAR_NUMBER")
        val rawPhone = intent.getStringExtra("EXTRA_PHONE_NUMBER")
        val phoneNumber = if (rawPhone.isNullOrBlank()) "---" else rawPhone

        if (orderId.isNullOrEmpty()) {
            Toast.makeText(this, "Ошибка: ID заказа не получен", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


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

    private fun fetchOrderDetails(orderId: String, initialCost: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getOrderDetail(orderId)

                if (response.isSuccessful) {
                    val fullOrder = response.body()

                    val displayId = fullOrder?.orderNumber?.toString() ?: orderId.take(8)
                    binding.orderIdDetails.text = "Заказ №$displayId"

                    val phone = fullOrder?.phone_number
                    if (!phone.isNullOrBlank()) {
                        binding.orderPhoneNumber.text = "Тел: $phone"
                        binding.orderPhoneNumber.setOnClickListener {
                            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("phone", phone)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(this@OrderDetailsActivity, "Номер скопирован", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        binding.orderPhoneNumber.text = "Тел: ---"
                    }

                    val itemsString = fullOrder?.repair_items?.joinToString("\n") {
                        " • ${it.name}: ${it.price} р."
                    } ?: "Детали не указаны"
                    binding.zapchasti.text = itemsString

                    var defectsTotalCost = 0
                    val numberRegex = Regex("\\d+")

                    if (!fullOrder?.hidden_defects.isNullOrEmpty()) {

                        fullOrder?.hidden_defects?.forEach { defectText ->
                            val priceMatch = numberRegex.findAll(defectText).lastOrNull()
                            if (priceMatch != null) {
                                defectsTotalCost += priceMatch.value.toInt()
                            }
                        }

                        val defectsDescription = fullOrder?.hidden_defects?.joinToString("\n") { " • $it" }

                        binding.hiddenDefectName.text = defectsDescription
                        binding.hiddenDefectCostValue.text = "Стоимость доп. работ: $defectsTotalCost р."

                        binding.hiddenDefect.visibility = View.VISIBLE
                        binding.hiddenDefectName.visibility = View.VISIBLE
                        binding.hiddenDefectCostValue.visibility = View.VISIBLE
                    } else {
                        binding.hiddenDefect.visibility = View.GONE
                        binding.hiddenDefectName.visibility = View.GONE
                        binding.hiddenDefectCostValue.visibility = View.GONE
                    }

                    val finalTotalCost = fullOrder?.cost ?: initialCost
                    binding.orderCost.text = "Итого к оплате: $finalTotalCost р."

                } else {
                    // Это else относится к response.isSuccessful
                    binding.zapchasti.text = "Ошибка сервера: ${response.code()}"
                }
            } catch (e: Exception) {
                // Если реально упал интернет или случился crash в коде
                Log.e("API_ERROR", "Crash: ${e.message}")
                binding.zapchasti.text = "Ошибка: ${e.localizedMessage}"
            }
        }
    }
}
