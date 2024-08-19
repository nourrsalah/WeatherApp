package com.example.weatherapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "weather")

data class CityWeather (
    @PrimaryKey( autoGenerate = true )
    var id: Int = 0,
    var forecast: Forecast,
    var current: Current,
    var location: Location,
    var cityNameTEXT : String
)