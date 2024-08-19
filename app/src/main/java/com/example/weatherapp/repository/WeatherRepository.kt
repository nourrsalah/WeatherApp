package com.example.weatherapp.repository

import androidx.lifecycle.LiveData
import com.example.weatherapp.api.RetrofitInstance
import com.example.weatherapp.api.RetrofitInstance.Companion.api
import com.example.weatherapp.db.CurrentDatabase
import com.example.weatherapp.models.CityWeather
import com.example.weatherapp.models.Forecast
import com.example.weatherapp.models.Forecastday
import com.example.weatherapp.models.Location
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.util.Constants.Companion.API_KEY
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Query

class WeatherRepository(
    private val db: CurrentDatabase,
) {

    suspend fun searchCities(location: String) =
        RetrofitInstance.api.searchCities(API_KEY, location)


    suspend fun getForecast(cityName: String, days: Int): Response<CityWeather> {
        return api.getForecast(location = cityName, days = days)
    }



    suspend fun upsert(cityWeather: CityWeather) = db.getCurrentDao().upsert(cityWeather)


    fun getSavedForecasts() : LiveData<CityWeather>  {
        return db.getCurrentDao().getAll()
    }

    //suspend fun deleteArticle(forecast: Forecast) = db.getCurrentDao().deleteCurrent(forecast)


    suspend fun getLocation(q:String, apiKey: String, days: Int): Response<WeatherResponse> {
        return api.getLocation(q, apiKey, 5)
    }



}
