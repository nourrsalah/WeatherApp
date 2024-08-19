package com.example.weatherapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.models.CityWeather
import com.example.weatherapp.models.Current
import com.example.weatherapp.models.Forecast
import com.example.weatherapp.util.Resource
import com.example.weatherapp.models.Location
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.repository.WeatherRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeoutException

class WeatherViewModel(
    val app: Application,
    private val weatherRepository: WeatherRepository
) : AndroidViewModel(app) {

    val searchWeather: MutableLiveData<Resource<List<Location>>> = MutableLiveData()

    private val _weatherForecast = MutableLiveData<Resource<CityWeather>>()
    val weatherForecast: LiveData<Resource<CityWeather>> get() = _weatherForecast

    private val _savedCities = MutableLiveData<List<CityWeather>>()
    val savedCities: LiveData<List<CityWeather>> get() = _savedCities

    private val savedCitiesList = mutableListOf<CityWeather>()


    fun saveArticle(forecast: Forecast, current: Current, location: Location, cityName: String) = viewModelScope.launch {
        weatherRepository.upsert(CityWeather(forecast = forecast, current =  current, location = location, cityNameTEXT = cityName))
    }

//    fun deleteArticle(forecast: Forecast) = viewModelScope.launch {
//        weatherRepository.deleteArticle(forecast)
//    }

    fun getSavedForecast() = weatherRepository.getSavedForecasts()


    fun searchCities(query: String) {
        viewModelScope.launch { safeSearchCitiesCall(query)
        }
    }

    private var selectedCityPosition: Int? = null

    fun saveCity(location: Location) {
        viewModelScope.launch {
            if (hasInternetConnection(app.applicationContext)) {
                val response = weatherRepository.getForecast(location.name, 1) // Assuming 1 day for now
                val weatherResponse = handleWeatherForecastResponse(response)
                if (weatherResponse is Resource.Success) {
                    savedCitiesList.add(weatherResponse.data!!)
                    _savedCities.postValue(savedCitiesList)
                }
            } else {
                _savedCities.postValue(savedCitiesList)
            }
        }
    }

    fun getCityPosition(cityName: String): Int? {
        return savedCitiesList.indexOfFirst {
            it.location.name.equals(cityName, ignoreCase = true)
        }.takeIf { it >= 0 }
    }

    fun setSelectedCityPosition(position: Int) {
        selectedCityPosition = position
    }

    fun getSelectedCityPosition(): Int? {
        return selectedCityPosition
    }

    private val _locationResult = MutableLiveData<Resource<Location>>()
    val locationResult: LiveData<Resource<Location>> get() = _locationResult

    fun getLocation(lat: Double, lon: Double, apiKey: String): LiveData<Resource<WeatherResponse>> {
        val locationResult = MutableLiveData<Resource<WeatherResponse>>()
        Log.d("NOUR","test 1")
        viewModelScope.launch {
            locationResult.postValue(Resource.Loading())
            try {
                Log.d("NOUR", "test before response")
                val q = "lat=$lat&lon=$lon"
                    val response = weatherRepository.getLocation(q, apiKey,5)
                    Log.d("NOUR", "test $response")
                    locationResult.postValue(handleLocationResponse(response))

            } catch (e: Exception) {
                Log.e("LocationViewModel", "Unknown error: ${e.message}")
                locationResult.postValue(Resource.Error("Unknown error: ${e.message}"))
            }
        }

        return locationResult
    }

    private fun handleLocationResponse(response: Response<WeatherResponse>): Resource<WeatherResponse> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Log.d("LocationViewModel", "API Response: $response")
                Log.d("LocationViewModel", "API Response: $body")
                Resource.Success(body)
            } else {
                Log.d("LocationViewModel", "API Response is null")
                Resource.Error("Error1: ${response.message()}")
            }
        } else {
            Log.e("LocationViewModel", "API Error: ${response.errorBody()?.string()}")
            Resource.Error("Error2: ${response.message()}")
        }
    }

//    private fun handleLocationResponse(response: Response<Location>): Resource<Location> {
//        return if (response.isSuccessful) {
//            val body = response.body()
//            if (body != null) {
//                Log.d("LocationViewModel", "API Response: $body")
//                Resource.Success(body)
//            } else {
//                Log.d("LocationViewModel", "API Response is null")
//                Resource.Success(Location("United States of America", 35.13, -117.99, "California City", "California", 22))
//            }
//        } else {
//            Log.e("LocationViewModel", "API Error: ${response.errorBody()?.string()}")
//            Resource.Error("Error: ${response.message()}")
//        }
//    }


    fun getWeatherForecast(location: String, days: Int) {
        _weatherForecast.postValue(Resource.Loading())

        viewModelScope.launch {
            try {
                if (hasInternetConnection(app.applicationContext)) {
                    val response = weatherRepository.getForecast(location, days)
                    _weatherForecast.postValue(handleWeatherForecastResponse(response))
                } else {
                    _weatherForecast.postValue(Resource.Error("No internet connection"))
                }
            } catch (e: Exception) {
                _weatherForecast.postValue(Resource.Error(e.message ?: "Unknown error"))
            }
        }
    }

    fun handleWeatherForecastResponse(response: Response<CityWeather>): Resource<CityWeather> {
        return if (response.isSuccessful) {
            val cityWeather = response.body()
            if (cityWeather != null) {
                Resource.Success(cityWeather)
            } else {
                Resource.Error("No data found")
            }
        } else {
            Resource.Error("Error: ${response.message()}")
        }
    }


    private fun handleSearchCitiesResponse(response: Response<List<Location>>): Resource<List<Location>> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private suspend fun safeSearchCitiesCall(query: String) {
        searchWeather.postValue(Resource.Loading())
        try {
            if (hasInternetConnection(app.applicationContext)) {
                val response = weatherRepository.searchCities(query)
                searchWeather.postValue(handleSearchCitiesResponse(response))
            } else {
                searchWeather.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchWeather.postValue(Resource.Error(t.message ?: ""))
                else -> searchWeather.postValue(Resource.Error(t.message ?: ""))
            }
        }
    }

    private fun hasInternetConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }
}
