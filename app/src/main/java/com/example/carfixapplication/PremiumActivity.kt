package com.example.carfixapplication

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.carfixapplication.databinding.ActivityPremiumBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class PremiumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPremiumBinding
    private val TAG = "PremiumActivity"
    private val client = OkHttpClient()

    // ВАЖНО: Тумблер для модерации.
    // true = заглушка для RuStore. false = боевой режим для реальной оплаты.
    private val isBetaMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupConnectButton()
        setupConnectButtonMonht()
    }

    private fun setupConnectButtonMonht() {
        binding.connectButtonMonth.setOnClickListener {
            if (isBetaMode) {
                showBetaDialog()
            } else {
                // Боевой режим (здесь потом будет твой реальный ID пользователя)
                sendPaymentRequest(userId = 1, durationDays = 184)
            }
        }
    }

    private fun setupConnectButton() {
        binding.connectButton.setOnClickListener {
            if (isBetaMode) {
                // Заглушка для модераторов RuStore и ЮKassa
                showBetaDialog()
            } else {
                // Боевой режим (здесь потом будет твой реальный ID пользователя)
                sendPaymentRequest(userId = 1, durationDays = 30)
            }
        }
    }

    private fun showBetaDialog() {
        AlertDialog.Builder(this).setTitle("Оплата в разработке").setMessage("Приложение находится в стадии открытого бета-тестирования. Подключение тарифов Premium временно недоступно. Ожидайте обновлений!").setPositiveButton("Понятно", null).show()
    }

    private fun sendPaymentRequest(userId: Int, durationDays: Int) {
        // Когда переключишь isBetaMode в false, не забудь поменять IP на свой Selectel!
        val url = "http://178.72.170.137:8000/payments/create-subscription"
        Log.d(TAG, "Отправка запроса на $url")

        val json = JSONObject().apply {
            put("durationDays", durationDays)
            put("userId", userId)
            // Добавляем deep link для возврата в приложение после оплаты
            put("returnUrl", "carfix://payment_success")
        }

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder().url(url).post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@PremiumActivity, "Ошибка сети", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.use { responseBody ->
                    val serverText = responseBody.string()
                    if (response.isSuccessful) {
                        try {
                            val jsonResponse = JSONObject(serverText)
                            val paymentUrl = jsonResponse.getString("payment_url")
                            // Открываем браузер для оплаты
                            runOnUiThread {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl))
                                startActivity(browserIntent)
                            }
                        } catch (e: JSONException) {
                            Log.e(TAG, "Ошибка парсинга", e)
                        }
                    }
                }
            }
        })
    }
}