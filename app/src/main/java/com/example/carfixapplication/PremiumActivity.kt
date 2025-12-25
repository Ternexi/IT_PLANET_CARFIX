package com.example.carfixapplication

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.text.HtmlCompat
import com.example.carfixapplication.databinding.ActivityPremiumBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class PremiumActivity : AppCompatActivity() {


    private lateinit var binding: ActivityPremiumBinding
    private val TAG = "PremiumActivity" // Тэг для логирования
    private val client = OkHttpClient() // Создаем OkHttpClient один раз

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val htmlString = getString(R.string.premium_benefits_list)

        // 2. Парсим HTML и преобразуем его в форматируемый текст (Spanned)
        val formattedText = HtmlCompat.fromHtml(
            htmlString,
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        // 3. Устанавливаем форматированный текст в TextView
        binding.textView2.text = formattedText

        // ID кнопок в коде теперь совпадают с XML: btnMonth и btnHalfYear
        binding.btnMonth.setOnClickListener {
            // TODO: Замените userId=1 на реальный ID текущего пользователя
            sendPayment(userId = 1, durationDays = 30)
        }

        binding.btnHalfYear.setOnClickListener {
            // TODO: Замените userId=1 на реальный ID текущего пользователя
            sendPayment(userId = 1, durationDays = 183)
        }
            val buttonClick = findViewById<Button>(R.id.back_button_premium)
            buttonClick.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

        }
    }

    private fun sendPayment(userId: Int, durationDays: Int) {

        val url = "http://192.168.31.238:3000/create-payment/$userId" // Эмулятор Android
        Log.d(TAG, "Отправка запроса на $url для подписки на $durationDays дней")

        val json = JSONObject()
        json.put("durationDays", durationDays)

        val body = json.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Ошибка OkHttp: ${e.message}", e) // Логирование ошибки
                runOnUiThread {
                    Toast.makeText(
                        this@PremiumActivity,
                        "Ошибка подключения: ${e.message}",
                        Toast.LENGTH_LONG // Увеличил время показа для критических ошибок
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Всегда используйте .use { ... } для тела ответа, чтобы убедиться, что он закрыт
                response.body.use { body ->
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Сервер вернул ошибку: ${response.code}. Тело: ${body?.string()}")
                        runOnUiThread {
                            Toast.makeText(
                                this@PremiumActivity,
                                "Ошибка сервера: ${response.code}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return
                    }

                    val serverText = body?.string()
                    Log.d(TAG, "Ответ сервера: $serverText")

                    if (serverText.isNullOrEmpty()) {
                        runOnUiThread {
                            Toast.makeText(
                                this@PremiumActivity,
                                "Пустой или некорректный ответ сервера",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return
                    }

                    try {
                        val jsonRes = JSONObject(serverText)
                        val payUrl = jsonRes.getString("payment_url")

                        if (payUrl.isNullOrEmpty()) {
                            Log.e(TAG, "Ответ JSON не содержит payment_url")
                            runOnUiThread {
                                Toast.makeText(this@PremiumActivity, "Ошибка: URL оплаты не найден.", Toast.LENGTH_LONG).show()
                            }
                            return
                        }

                        runOnUiThread {
                            // Открываем браузер с оплатой
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(payUrl))
                            startActivity(intent)
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Ошибка парсинга JSON: ${e.message}", e)
                        runOnUiThread {
                            Toast.makeText(this@PremiumActivity, "Ошибка обработки данных сервера.", Toast.LENGTH_LONG).show()

                        }
                    }
                }

            }
        })
    }
}