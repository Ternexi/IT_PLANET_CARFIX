package com.example.carfixapplication.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// Детали
@Parcelize
data class RepairItem(
    val name: String,
    val price: Int
) : Parcelable

@Parcelize
data class HiddenDefect(
    @SerializedName("description") val description: String,
    @SerializedName("cost") val cost: Int
) : Parcelable

@Parcelize
data class Order(
    @SerializedName("id") val id: String,

    @SerializedName("order_number") val orderNumber: Int?,
    @SerializedName("car_number") val car_number: String?,
    @SerializedName("order_time") val time: String?,
    @SerializedName("order_cost") val cost: Int?,
    @SerializedName("phone_number") val phone_number: String?,
    @SerializedName("hidden_defects")
    val hidden_defects: List<String>?,

    val description: String? = "Детали в разработке",

    val repair_items: List<RepairItem>?
) : Parcelable


// Регистрации
data class RegistrationRequest(
    val fullname: String,
    val email: String,
    val password: String
)

// Ответ сервера после регистрации
data class RegistrationResponse(
    val message: String
)



// Запрос на создание машины
data class CarRequest(
    val car_number: String,
    @SerializedName("phone_number") val phone_number: String
)

// Ответ с данными машины
data class Car(
    @SerializedName("id_car") val id_car: String,
    @SerializedName("car_number") val car_number: String
)

// Данные для новго заказа
data class NewOrderRequest(
    val id_car: String,
    val order_cost: Int,
    @SerializedName("hidden_defects") val hidden_defects: List<String>,
    val repair_items: List<RepairItem>
)

// Ответ сервера после создания заказа
data class NewOrderResponse(
    val status: String,
    val id_order: String,
)

// Вспомогательный класс для ID созданного заказа
data class OrderDetails(
    val id_order: String,
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val message: String,
    val user: UserData
)

data class UserData(
    @SerializedName("id_user") val id: String,
    val name: String,
    val email: String,
    val subscription_end: String?,
    val isPremium: Boolean

)

data class CarResponse(
    val id_car: String,
    val car_number: String
)

