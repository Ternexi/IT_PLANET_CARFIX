package com.example.carfixapplication.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // Поиск по истории
    @GET("orders/search")
    suspend fun getOrdersByCarNumber(
        @Query("carNumber") carNumber: String
    ): Response<List<Order>>

    // Работа с машинами и заказами
    @POST("cars")
    suspend fun createCar(
        @Body request: CarRequest
    ): Response<Car>

    @POST("orders")
    suspend fun createOrder(
        @Body request: NewOrderRequest
    ): Response<NewOrderResponse>


    // Все заказы для конкретного пользователя
    @GET("orders")
    suspend fun getOrders(
    ): Response<List<Order>>


    // Получить детали конкретного заказа
    @GET("orders/{id}")
    suspend fun getOrderDetail(
        @Path("id") orderId: String
    ): Response<Order>

    @GET("orders/recent")
    suspend fun getRecentOrders(
    ): Response<List<Order>>


    // Авторизация
    @POST("register")
    suspend fun registerUser(
        @Body data: RegistrationRequest
    ): Response<RegistrationResponse>

    @POST("login")
    suspend fun loginUser(
        @Body data: LoginRequest
    ): Response<LoginResponse>
}