package com.example.carfixapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.carfixapplication.api.*
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Context
class MainActivity : AppCompatActivity() {

    private lateinit var searchHistoryButton: Button
    private lateinit var searchNumberButton: Button
    private lateinit var settingsButton: ImageButton
    private lateinit var carNumberDisplay: TextView
    private lateinit var inputFields: List<Pair<TextInputEditText, String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(!isUserLoggedIn()) {

            goToLogin()
            return
        }


        setContentView(R.layout.activity_main)


        initializeViews()
        setupListeners()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    private fun isUserLoggedIn(): Boolean {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        // Ищем токен. Если он не пустой, значит пользователь вошел.
        return prefs.getString("USER_TOKEN", null) != null
    }

    /**
     * Запускает экран входа и закрывает все предыдущие экраны.
     */
    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        // Эти флаги не дадут пользователю кнопкой "Назад" вернуться на главный экран, если он не залогинен
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }



    override fun onStart() {
        super.onStart()
        // Эта проверка нужна, чтобы код не упал, если мы вышли из onCreate раньше времени
        if (isUserLoggedIn()) {
            displayCurrentCarNumber()
        }

        displayCurrentCarNumber()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        displayCurrentCarNumber()
    }

    // 1. ПРОВЕРКА НОМЕРА (Оставлена одна копия)
    fun isValidRussianNumber(number: String): Boolean {
        val regex = "^[АВЕКМНОРСТУХ]\\d{3}[АВЕКМНОРСТУХ]{2}\\d{2,3}$".toRegex(RegexOption.IGNORE_CASE)
        return regex.matches(number)
    }

    // 2. СОЗДАНИЕ МАШИНЫ (Оставлена одна копия с проверкой внутри)
    private fun createCarAndGetId(carNumber: String, onResult: (Int?) -> Unit) {
        if (!isValidRussianNumber(carNumber)) {
            Toast.makeText(this, "Номер введен некорректно! Используйте только русские буквы.", Toast.LENGTH_SHORT).show()
            onResult(null)
            return
        }

        val carRequestBody = CarRequest(car_number = carNumber)

        RetrofitClient.api.createCar(carRequestBody).enqueue(object : Callback<Car> {
            override fun onResponse(call: Call<Car>, response: Response<Car>) {
                if (response.isSuccessful && response.body() != null) {
                    onResult(response.body()!!.id_car)
                } else {
                    Toast.makeText(this@MainActivity, "Не удалось создать/найти машину. Код: ${response.code()}", Toast.LENGTH_SHORT).show()
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<Car>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                onResult(null)
            }
        })
    }

    private fun initializeViews() {
        val premiumButton: Button = findViewById(R.id.premiumButton)
        premiumButton.setOnClickListener {
            val intent = Intent(this, PremiumActivity::class.java)
            startActivity(intent)
        }

        searchNumberButton = findViewById(R.id.Search_number)
        settingsButton = findViewById(R.id.settings_buton)
        searchHistoryButton = findViewById(R.id.Search_history)
        carNumberDisplay = findViewById(R.id.current_car_number)

        inputFields = listOf(
            findViewById<TextInputEditText>(R.id.bamper1_input) to "Передний бампер",
            findViewById<TextInputEditText>(R.id.capot_input) to "Капот",
            findViewById<TextInputEditText>(R.id.lobovoe_input) to "Лобовое стекло",
            findViewById<TextInputEditText>(R.id.crisha_input) to "Крыша",
            findViewById<TextInputEditText>(R.id.zadnee_steclo_input) to "Заднее стекло",
            findViewById<TextInputEditText>(R.id.bamper2_input) to "Задний бампер",


            findViewById<TextInputEditText>(R.id.podcrilokLeft_Input) to "Левое переднее крыло",
            findViewById<TextInputEditText>(R.id.door_center_left_input1) to "Левая передняя дверь",
            findViewById<TextInputEditText>(R.id.top_left_window_input1) to "Левое переднее стекло",
            findViewById<TextInputEditText>(R.id.front_porog_left1_input) to "Левый передний порог",
            findViewById<TextInputEditText>(R.id.back_porog_left1_input) to "Левый задний порог",
            findViewById<TextInputEditText>(R.id.back_crilo_left_input) to "Левое заднее крыло",
            findViewById<TextInputEditText>(R.id.door_center_left_input2) to "Левая задняя дверь",
            findViewById<TextInputEditText>(R.id.top_left_window_input2) to "Левое заднее стекло",
            findViewById<TextInputEditText>(R.id.top_left_window_input3) to "Левая задняя форточка",


            findViewById<TextInputEditText>(R.id.podcrilokRight_Input) to "Правое переднее крыло",
            findViewById<TextInputEditText>(R.id.door_center_right_input1) to "Правая передняя дверь",
            findViewById<TextInputEditText>(R.id.top_right_window_input1) to "Правое переднее стекло",
            findViewById<TextInputEditText>(R.id.front_porog_right1_input) to "Правый передний порог",
            findViewById<TextInputEditText>(R.id.back_porog_right1_input) to "Правый задний порог",
            findViewById<TextInputEditText>(R.id.back_crilo_right_input) to "Правое заднее крыло",
            findViewById<TextInputEditText>(R.id.door_center_right_input2) to "Правая задняя дверь",
            findViewById<TextInputEditText>(R.id.top_right_window_input3) to "Правая задняя форточка"

        ).filter { it.first != null }.map { it.first!! to it.second }
    }

    private fun displayCurrentCarNumber() {
        val carNumber = LocalCache.getNumber(this)?.trim() ?: ""
        carNumberDisplay.text = if (carNumber.isEmpty()) "Номер не выбран" else "Выбранный номер: $carNumber"
    }

    private fun setupListeners() {
        searchNumberButton.setOnClickListener {
            startActivity(Intent(this, SearchCarActivity::class.java))
        }
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        searchHistoryButton.setOnClickListener {
            startOrderCreationProcess()
        }
    }

    private fun startOrderCreationProcess() {
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

        if (!hasInput) {
            Toast.makeText(this, "Заполните хотя бы одно поле!", Toast.LENGTH_SHORT).show()
            return
        }

        createCarAndGetId(carNumber) { carId ->
            if (carId != null) {
                val requestBody = NewOrderRequest(
                    id_car = carId,
                    id_user = 1,
                    order_cost = totalSum,
                    repair_items = repairItemsList
                )
                sendOrderToApi(requestBody)
            } else {
                Toast.makeText(this, "Ошибка идентификации автомобиля.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendOrderToApi(request: NewOrderRequest) {
        RetrofitClient.api.createOrder(request).enqueue(object : Callback<NewOrderResponse> {
            override fun onResponse(call: Call<NewOrderResponse>, response: Response<NewOrderResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val orderId = response.body()!!.order.id_order
                    Toast.makeText(this@MainActivity, "Заказ #$orderId создан! Сумма: ${request.order_cost} ₽", Toast.LENGTH_LONG).show()
                    LocalCache.clearNumber(this@MainActivity)
                    displayCurrentCarNumber()
                    clearAllInputFields()
                } else if (response.code() == 403) {
                    // НОВАЯ ЛОГИКА ДЛЯ ЛИМИТА
                    Toast.makeText(this@MainActivity, "Лимит бесплатных расчетов исчерпан! Купите Premium.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@MainActivity, PremiumActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@MainActivity, "Ошибка сервера: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<NewOrderResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun clearAllInputFields() {
        inputFields.forEach { (field, _) -> field.setText("") }
    }
}
