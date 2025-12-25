package com.example.carfixapplication.api

import android.os.Build
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RepairItem(
    val name: String,
    val price: Int
) : Parcelable

@Parcelize
data class Order(
    @SerializedName("id_order") val id: Int,
    @SerializedName("car_number") val car_number: String?,
    @SerializedName("order_time") val time: String?,
    @SerializedName("order_cost") val cost: Int,
    // ДОБАВЬ ЭТУ СТРОКУ:
    val description: String? = "Детали в разработке",
    val repair_items: List<RepairItem>?
) : Parcelable


// 2. Добавляем недостающие модели для ApiService:

// Для запроса создания/поиска машины
data class CarRequest(
    val car_number: String
)

// Ответ от сервера с данными машины
data class Car(
    @SerializedName("id_car")
    val id_car: Int,
    @SerializedName("car_number")
    val car_number: String
)

// Для создания нового заказа
data class NewOrderRequest(
    val id_car: Int,
    val id_user: Int,
    val order_cost: Int,
    val repair_items: List<RepairItem>
)

// Ответ при создании заказа
data class NewOrderResponse(
    val status: String,
    val order: OrderDetails
)

data class OrderDetails(
    val id_order: Int
)
