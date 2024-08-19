package com.example.weatherapp.models

data class Forecastday(
    var id : Int? = null,
    val date: String,
    var day : Day,
    val hour: List<Hour>
)