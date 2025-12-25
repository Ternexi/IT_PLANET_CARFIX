package com.example.carfixapplication

import android.content.Context

object LocalCache {
    private const val PREF = "local_cache"
    private const val KEY_NUMBER = "car_number"
    private const val KEY_SUM = "order_sum"

    fun saveNumber(context: Context, number: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY_NUMBER, number).apply()
    }

    // ИСПРАВЛЕНО: Возвращает пустую строку ("") вместо null, если номер не найден.
    fun getNumber(context: Context): String {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_NUMBER, "") ?: "" // Возврат "" в случае, если getString вернул null
    }

    fun saveSum(context: Context, sum: Int) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putInt(KEY_SUM, sum).apply()
    }

    fun getSum(context: Context): Int {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_SUM, 0)
    }

    //функция для очистки номера
    fun clearNumber(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().remove(KEY_NUMBER).apply()}
}