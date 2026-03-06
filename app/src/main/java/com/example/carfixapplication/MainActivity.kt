package com.example.carfixapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.carfixapplication.api.*
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var searchHistoryButton: LinearLayout
    private lateinit var searchNumberButton: LinearLayout

    private lateinit var search_history_real: LinearLayout
    private lateinit var settingsButton: ImageButton
    private lateinit var carNumberDisplay: TextView
    private lateinit var inputFields: List<Pair<TextInputEditText, String>>

    private lateinit var hiddenDefectNameInput: EditText
    private lateinit var hiddenDefectCostInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeViews()
        setupListeners()

        if (!isUserLoggedIn()) {
            goToLogin()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStart() {
        super.onStart()
        displayCurrentCarNumber()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (isUserLoggedIn()) {
            displayCurrentCarNumber()
        }
    }

    private fun initializeViews() {
        findViewById<Button>(R.id.premiumButton).setOnClickListener {
            startActivity(Intent(this, PremiumActivity::class.java))
        }

        searchNumberButton = findViewById(R.id.Search_number)
        search_history_real = findViewById(R.id.Search_history_real)
        settingsButton = findViewById(R.id.settings_buton)
        searchHistoryButton = findViewById(R.id.Search_history)
        carNumberDisplay = findViewById(R.id.current_car_number)
        hiddenDefectNameInput = findViewById(R.id.hidden_defects_input_left)
        hiddenDefectCostInput = findViewById(R.id.hidden_defects_input_right)



        val fieldsToMap = listOf(
            R.id.bamper1_input to "Передний бампер",
            R.id.capot_input to "Капот",
            R.id.lobovoe_input to "Лобовое стекло",
            R.id.crisha_input to "Крыша",
            R.id.zadnee_steclo_input to "Заднее стекло",
            R.id.bamper2_input to "Задний бампер",
            R.id.podcrilokLeft_Input to "Левое переднее крыло",
            R.id.door_center_left_input1 to "Левая передняя дверь",
            R.id.top_left_window_input1 to "Левое переднее стекло",
            R.id.front_porog_left1_input to "Левый порог",
            R.id.front_porog_right1_input to "Правый порог",
            R.id.back_crilo_left_input to "Левое заднее крыло",
            R.id.door_center_left_input2 to "Левая задняя дверь",
            R.id.top_left_window_input2 to "Левое заднее стекло",
            R.id.top_left_window_input3 to "Левая задняя форточка",
            R.id.podcrilokRight_Input to "Правое переднее крыло",
            R.id.door_center_right_input1 to "Правая передняя дверь",
            R.id.top_right_window_input1 to "Правое переднее стекло",
            R.id.back_crilo_right_input to "Правое заднее крыло",
            R.id.door_center_right_input2 to "Правая задняя дверь",
            R.id.top_right_window_input3 to "Правая задняя форточка"
        )


        // не понял
        inputFields = fieldsToMap.mapNotNull { (id, name) ->
            val field = findViewById<TextInputEditText>(id)
            if (field == null) {
                Log.e("MainActivity", "View with ID '$id' ($name) not found!")
                null
            } else {
                field to name
            }
        }
    }

    private fun setupListeners() {
        searchNumberButton.setOnClickListener {
            startActivity(Intent(this, SearchCarActivity::class.java))
        }
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        search_history_real.setOnClickListener {
            startActivity(Intent(this, HistorySearchActivity::class.java))
        }
        searchHistoryButton.setOnClickListener {
            startOrderCreationProcess()
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return prefs.getString("USER_TOKEN", null) != null
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun isValidRussianNumber(number: String): Boolean {
        val regex = "^[АВЕКМНОРСТУХ]\\d{3}[АВЕКМНОРСТУХ]{2}\\d{2,3}$".toRegex(RegexOption.IGNORE_CASE)
        return regex.matches(number)
    }

    private suspend fun createCarAndGetId(carNumber: String): Int? {
        if (!isValidRussianNumber(carNumber)) {
            Log.e("CAR_DEBUG", "Номер $carNumber не прошел проверку регулярным выражением!")

            Toast.makeText(this, "Неверный формат номера", Toast.LENGTH_SHORT).show()
            return null
        }

        val realPhone = LocalCache.getPhone(this)

        return try {
            Log.d("CAR_DEBUG", "Отправляем запрос на сервер для номера: $carNumber, ${realPhone}e")

            val response = RetrofitClient.api.createCar(CarRequest(carNumber, realPhone))

            // 3. Анализируем ответ
            if (response.isSuccessful) {
                val carResponse = response.body()
                Log.d("CAR_DEBUG", "Сервер ответил успешно: $carResponse")

                if (carResponse != null && carResponse.id_car != 0) {
                    carResponse.id_car
                } else {
                    Log.e("CAR_DEBUG", "Успех, но id_car пустой или равен 0! Проверь модель Car.")
                    null
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CAR_DEBUG", "Ошибка сервера: код ${response.code()}, текст: $errorBody")
                null
            }
        } catch (e: Exception) {
            Log.e("CAR_DEBUG", "Сбой сети или парсинга: ${e.message}", e)
            null
        }
    }

    private fun displayCurrentCarNumber() {
        val carNumber = LocalCache.getNumber(this)?.trim() ?: ""
        carNumberDisplay.text = if (carNumber.isEmpty()) "Номер не выбран" else "Выбранный номер: $carNumber"
    }



    // НЕ понятно скореее всего не работает
    private fun getUserIdFromToken(): Int? {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("USER_TOKEN", null) ?: return null
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            val payloadBytes = android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE)
            val payloadJson = String(payloadBytes, Charsets.UTF_8)
            val jsonObject = org.json.JSONObject(payloadJson)
            jsonObject.getJSONObject("user").getInt("id")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun startOrderCreationProcess() {
        val userId = getUserIdFromToken()
        if (userId == null) {
            Toast.makeText(this, "Ошибка авторизации. Пожалуйста, войдите заново.", Toast.LENGTH_LONG).show()
            goToLogin()
            return
        }

        val carNumber = LocalCache.getNumber(this)?.trim() ?: ""
        if (carNumber.isEmpty()) {
            Toast.makeText(this, "Сначала выберите номер автомобиля!", Toast.LENGTH_LONG).show()
            return
        }

        var totalSum = 0

        val repairItemsList = mutableListOf<RepairItem>()
        var hasInput = false

        for ((field, name) in inputFields) {
            val value = field.text?.toString()?.trim()
            if (!value.isNullOrEmpty()) {
                hasInput = true
                val price = value.toIntOrNull()
                if (price == null || price < 0) {
                    Toast.makeText(this, "Ошибка в поле '$name'. Вводите только числа.", Toast.LENGTH_LONG).show()
                    return
                }
                totalSum += price
                repairItemsList.add(RepairItem(name = name, price = price))
            }
        }

        val defectsList = mutableListOf<HiddenDefect>()

        val hiddenDefectName = hiddenDefectNameInput.text.toString().trim()
        val hiddenDefectCostText = hiddenDefectCostInput.text.toString().trim()

        var hiddenDefectCost = 0

        if (hiddenDefectName.isNotEmpty() || hiddenDefectCostText.isNotEmpty()) {

            val cost = hiddenDefectCostText.toIntOrNull()
            if (hiddenDefectName.isEmpty() || cost == null) {
                Toast.makeText(this, "Для скрытого дефекта должны быть заполнены и название, и стоимость.", Toast.LENGTH_LONG).show()
                return
            }
            hiddenDefectCost = cost
            defectsList.add(HiddenDefect(description = hiddenDefectName, cost = cost))
            hasInput = true
        }

        if (!hasInput) {
            Toast.makeText(this, "Заполните хотя бы одно поле!", Toast.LENGTH_SHORT).show()
            return
        }

        val finalTotalSum = totalSum + hiddenDefectCost

        // не понятно коруины
        lifecycleScope.launch{
         val carId = createCarAndGetId(carNumber)
            if (carId != null) {

                val requestBody = NewOrderRequest(
                    id_car = carId,
                    id_user = userId,
                    order_cost = finalTotalSum,
                    repair_items = repairItemsList,
                    hidden_defects = if (defectsList.isNotEmpty()) defectsList else null
                )

                sendOrderToApi(requestBody)
            } else {
                Toast.makeText(this@MainActivity, "Ошибка идентификации автомобиля.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun sendOrderToApi(request: NewOrderRequest) {
        RetrofitClient.api.createOrder(request).enqueue(object : Callback<NewOrderResponse> {

            override fun onResponse(call: Call<NewOrderResponse>, response: Response<NewOrderResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val orderId = response.body()?.id_order
                    Toast.makeText(this@MainActivity, "Заказ #$orderId создан! Сумма: ${request.order_cost} ₽", Toast.LENGTH_LONG).show()
                    LocalCache.clearNumber(this@MainActivity)
                    displayCurrentCarNumber()
                    clearAllInputFields()
                } else if (response.code() == 403) {
                    Toast.makeText(this@MainActivity, "Лимит бесплатных расчетов исчерпан! Купите Premium.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@MainActivity, PremiumActivity::class.java))
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("MainActivity", "Server error: ${response.code()} - $errorBody")
                    Toast.makeText(this@MainActivity, "Ошибка сервера: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<NewOrderResponse>, t: Throwable) {
                Log.e("MainActivity", "Network failure", t)
                Toast.makeText(this@MainActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun clearAllInputFields() {
        inputFields.forEach { (field, _) -> field.setText("") }
    }
}
