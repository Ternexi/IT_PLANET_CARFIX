package com.example.carfixapplication.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class RepairItem(
    val name: String,   // Название детали (например, "Бампер")
    val price: Int      // Цена
) : Parcelable          // Обязательная метка для упаковки

@Parcelize
data class HiddenDefect(
    @SerializedName("description") // Имя поля в JSON от сервера
    val description: String,       // Имя поля  в коде

    @SerializedName("cost")
    val cost: Int
) : Parcelable

@Parcelize
data class Order(
    // Сервер присылает "id_order" переименовываем его просто в "id"
    @SerializedName("id_order") val id: Int,

    // Блок для отображения в шаблоне
    @SerializedName("car_number") val car_number: String?, // Номер
    @SerializedName("order_time") val time: String?, // Время
    @SerializedName("order_cost") val cost: Int, // Цена
    @SerializedName("phone_number") val phone_number: String?, //Номер клиента

    // Список скрытых дефектов (может быть null, если их нет)
    @SerializedName("hidden_defects")
    val hidden_defects: List<HiddenDefect>?,

    val description: String? = "Детали в разработке",

    // Список ремонтных работ
    val repair_items: List<RepairItem>?
) : Parcelable


// *** АВТОРИЗАЦИЯ И РЕГИСТРАЦИЯ ***



// То, что отправляем при регистрации
data class RegistrationRequest(
    val fullname: String,
    val email: String,
    val password: String
)

// Ответ сервера после регистрации
data class RegistrationResponse(
    val message: String
)


// *** СОЗДАНИЕ МАШИН И ЗАКАЗОВ ***

// Запрос на создание машины
data class CarRequest(
    val car_number: String,
    @SerializedName("phone_number") val phone_number: String
)

// Ответ с данными машины
data class Car(
    @SerializedName("id_car") val id_car: Int, // id
    @SerializedName("car_number") val car_number: String // Номер машины
)

// Данные для НОВОГО заказа (отправляем на сервер)
data class NewOrderRequest(
    val id_car: Int, // id
    val id_user: Int, // id аккаунта
    val order_cost: Int, // Стоимость ремонта

    @SerializedName("hidden_defects")
    val hidden_defects: List<HiddenDefect>?,

    val repair_items: List<RepairItem>
)

// Ответ сервера после создания заказа
data class NewOrderResponse(
    val status: String,
    val id_order: Int,
)

// Вспомогательный класс для ID созданного заказа (костыли)
data class OrderDetails(
    val id_order: Int
)

data class LoginRequest(
    val email: String,
    val password: String
)

// То, что сервер отвечает при входе
data class LoginResponse(
    val token: String,  // Наш пропуск (JWT)
    val message: String, // Сообщение (например, "Успешно")
    val user: UserData
)

data class UserData(
    @SerializedName("id_user") val id: Int,
    @SerializedName("fullname") val name: String,
    val email: String
)

data class CarResponse(
    val id_car: Int,
    val car_number: String
)

