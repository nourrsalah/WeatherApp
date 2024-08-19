package com.example.weatherapp.api

import com.example.weatherapp.models.CityWeather
import com.example.weatherapp.models.Location
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.util.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("forecast.json")
    suspend fun getForecast(
        @Query("key") apiKey: String = API_KEY,
        @Query("q") location: String,
        @Query("days") days: Int
    ): Response<CityWeather>

    @GET("search.json")
    suspend fun searchCities(
        @Query("key") apiKey: String = API_KEY,
        @Query("q") query: String
    ): Response<List<Location>>

    @GET("forecast.json")
    suspend fun getLocation(
        @Query("q") query: String,
        @Query("key") apiKey: String = API_KEY,
        @Query("days") days: Int
    ): Response<WeatherResponse>
}
