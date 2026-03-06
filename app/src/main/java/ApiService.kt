package com.example.carfixapplication.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ПОИСК ПО ИСТОРИИ
    @GET("orders/search")
    suspend fun getOrdersByCarNumber(
        @Query("carNumber") carNumber: String
    ): retrofit2.Response<List<Order>> // Возвращает список заказов


    // РАБОТА С МАШИНАМИ И ЗАКАЗАМИ
    @POST("cars")
    suspend fun createCar(
        @Body request: CarRequest
    ): retrofit2.Response<Car> // Возвращает созданную машину

    @POST("orders")
    fun createOrder(
        @Body request: NewOrderRequest
    ): Call<NewOrderResponse> // Возвращает статус и ID нового заказа


    // Просто получить вообще все заказы для конкретного пользователя
    @GET("orders")
    suspend fun getOrders(
        @Query("user_id") userId: Int
    ): retrofit2.Response<List<Order>>


    // Получить детали конкретного заказа по его ID
    @GET("orders/{id}")
    fun getOrderDetail(
        // @Path -> Заменяет {id} в ссылке на реальное число (например: orders/55)
        @Path("id") orderId: Int
    ): Call<Order>

    @GET("orders/recent")
    suspend fun getRecentOrders(): Response<List<Order>>




    // БЛОК АВТОРИЗАЦИИ

    @POST("register")
    fun registerUser(
        @Body registrationData: RegistrationRequest
    ): Call<RegistrationResponse>

    @POST("login")
    fun loginUser(
        @Body loginData: LoginRequest
    ): Call<LoginResponse>
}