package com.example.carfixapplication.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Path

interface ApiService {

    /**
     * Поиск заказов по номеру машины.
     */
    @GET("orders/search")
    suspend fun getOrdersByCarNumber(
        @Query("carNumber") carNumber: String
    ): Response<List<Order>>

    /**
     * Создание или поиск машины.
     */
    @POST("cars")
    fun createCar(@Body request: CarRequest): Call<Car>

    /**
     * Создание нового заказа.
     */
    @POST("orders")
    fun createOrder(@Body request: NewOrderRequest): Call<NewOrderResponse>

    @GET("orders")
    fun getOrders(): Call<List<Order>>


    @POST("register")
    fun registerUser(@Body registrationData: RegistrationRequest): Call<RegistrationResponse>

    data class RegistrationRequest(
        val fullname: String,
        val email: String,
        val password: String,
    )

    data class RegistrationResponse(
        val message: String,
    )

    @POST("login")
    fun loginUser(@Body loginData: LoginRequest): Call<LoginResponse>
    data class LoginRequest(
        val email: String,
        val password: String,
    )


    data class LoginResponse(
        val token: String,
        val message: String,
    )




    @GET("orders/{id}") // Предполагаемый путь к API. Замените на ваш, если он другой.
    fun getOrderDetail(@Path("id") orderId: Int): Call<Order>
}

