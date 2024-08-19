package com.example.weatherapp.models

data class Current(
    val condition: Condition,
    val humidity: Int,
    val is_day: Int,
    val temp_c: Double,
    val wind_kph: Double,
)