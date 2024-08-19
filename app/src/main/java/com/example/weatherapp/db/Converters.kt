package com.example.weatherapp

import androidx.room.TypeConverter
import com.example.weatherapp.models.Condition
import com.example.weatherapp.models.Current
import com.example.weatherapp.models.Day
import com.example.weatherapp.models.Forecast
import com.example.weatherapp.models.Forecastday
import com.example.weatherapp.models.Hour
import com.example.weatherapp.models.Location
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromHourList(value: List<Hour>): String {
        val gson = Gson()
        val type = object : TypeToken<List<Hour>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toHourList(value: String): List<Hour> {
        val gson = Gson()
        val type = object : TypeToken<List<Hour>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromDay(day: Day): String {
        return Gson().toJson(day)
    }

    @TypeConverter
    fun toDay(dayString: String): Day {
        return Gson().fromJson(dayString, Day::class.java)
    }

    @TypeConverter
    fun fromForecastdayList(forecastdayList: List<Forecastday>?): String? {
        if (forecastdayList == null) return null
        return Gson().toJson(forecastdayList)
    }

    @TypeConverter
    fun toForecastdayList(data: String?): List<Forecastday>? {
        if (data == null) return null
        val type = object : TypeToken<List<Forecastday>>() {}.type
        return Gson().fromJson(data, type)
    }

    @TypeConverter
    fun fromLocation(location: Location?): String? {
        return location?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun toLocation(locationString: String?): Location? {
        return locationString?.let { Gson().fromJson(it, Location::class.java) }
    }
    @TypeConverter
    fun fromCurrent(current: Current): String {
        return Gson().toJson(current)
    }

    @TypeConverter
    fun toCurrent(data: String): Current {
        return Gson().fromJson(data, Current::class.java)
    }

    @TypeConverter
    fun fromForecast(forecast: Forecast): String {
        return Gson().toJson(forecast)
    }

    @TypeConverter
    fun toForecast(data: String): Forecast {
        val type = object : TypeToken<Forecast>() {}.type
        return Gson().fromJson(data, type)
    }

}
