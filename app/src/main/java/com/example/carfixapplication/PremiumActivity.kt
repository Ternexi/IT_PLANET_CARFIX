package com.example.carfixapplication

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.carfixapplication.databinding.ActivityPremiumBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class PremiumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPremiumBinding
    // Тэг для удобного поиска логов
    private val TAG = "PremiumActivity"
    // Создаем OkHttpClient один раз, чтобы переиспользовать его для всех запросов
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Инициализируем ViewBinding
        binding = ActivityPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // обработчик клика на нижнюю кнопку "Подключить"
        setupConnectButton()
    }

    private fun setupConnectButton() {
        binding.connectButton.setOnClickListener {
            // TODO: Реализуйте логику выбора между 30 и 183 днями.
            // TODO: Замените userId=1 на реальный ID текущего пользователя.
            sendPaymentRequest(userId = 1, durationDays = 30)
        }
    }

    private fun sendPaymentRequest(userId: Int, durationDays: Int) {
        // IP-адрес для доступа к локальному серверу с эмулятора Android.
        val url = "http://10.0.2.2:3000/create-payment/$userId"
        Log.d(TAG, "Отправка запроса на $url для подписки на $durationDays дней")

        // Тело запроса в формате JSON
        val json = JSONObject()
        json.put("durationDays", durationDays)
        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        // Сам запрос
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // Выполняем запрос асинхронно, чтобы не блокировать основной поток приложения
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Сетевая ошибка OkHttp: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@PremiumActivity, "Ошибка сети. Проверьте подключение.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.use { responseBody ->
                    val serverText = responseBody.string()

                    // Если ответ от сервера неуспешный (код не 2xx)
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Ошибка сервера: ${response.code}, Тело: $serverText")
                        runOnUiThread {
                            Toast.makeText(this@PremiumActivity, "Ошибка сервера: ${response.code}", Toast.LENGTH_LONG).show()
                        }
                        return
                    }

                    Log.d(TAG, "Успешный ответ от сервера: $serverText")

                    try {
                        val jsonResponse = JSONObject(serverText)
                        val paymentUrl = jsonResponse.getString("payment_url")

                        // Открываем браузер со ссылкой на оплату в основном потоке
                        runOnUiThread {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl))
                            startActivity(browserIntent)
                        }

                    } catch (e: JSONException) {
                        Log.e(TAG, "Ошибка парсинга JSON: ${e.message}", e)
                        runOnUiThread {
                            Toast.makeText(this@PremiumActivity, "Ошибка обработки ответа от сервера.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        })
    }
}
