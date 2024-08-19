package com.example.weatherapp.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Location(
    val country: String,
    val lat: Double,
    val lon: Double,
    val name: String,
    val region: String,
    val id: Int
)