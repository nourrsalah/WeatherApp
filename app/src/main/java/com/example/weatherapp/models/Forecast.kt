package com.example.weatherapp.models


import java.io.Serializable

data class Forecast(
    var forecastday: List<Forecastday>
) : Serializable
