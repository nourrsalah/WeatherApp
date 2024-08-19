package com.example.weatherapp.models

data class Hour(
    val condition: Condition,
    val humidity: Int,
    val is_day: Int,
    val temp_c: Double,
    val time: String,
    val wind_kph: Double,
)