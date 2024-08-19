package com.example.weatherapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "weather"
)
data class WeatherResponse(
    @PrimaryKey(autoGenerate = true)
    val current: Current,
    val forecast: Forecast,
    val location: Location
)