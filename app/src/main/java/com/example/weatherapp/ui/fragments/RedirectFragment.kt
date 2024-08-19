package com.example.weatherapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.adapters.DaysAdapter
import com.example.weatherapp.adapters.HoursAdapter
import com.example.weatherapp.api.RetrofitInstance
import com.example.weatherapp.models.CityWeather
import com.example.weatherapp.models.Hour
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.ui.WeatherActivity
import com.example.weatherapp.ui.WeatherViewModel
import com.example.weathersapp.R
import com.google.android.gms.location.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RedirectFragment : Fragment() {

    lateinit var viewModel: WeatherViewModel
    private lateinit var hoursAdapter: HoursAdapter
    private lateinit var daysAdapter: DaysAdapter
    private lateinit var locationRequest: LocationRequest
    private lateinit var date: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvDegree: TextView
    private lateinit var tvCity: TextView
    private lateinit var tvWindNumber: TextView
    private lateinit var tvHumidNumber: TextView
    private lateinit var ivWeather: ImageView
    private lateinit var rvHours: RecyclerView
    private lateinit var rvDays: RecyclerView
    private lateinit var paginationProgressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_redirect, container, false)
    }

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvHours = view.findViewById(R.id.rvHours)
        rvDays = view.findViewById(R.id.rvDays)
        viewModel = (activity as WeatherActivity).viewModel
        date = view.findViewById(R.id.tvData)
        tvCity = view.findViewById(R.id.tvCityy)
        tvDegree = view.findViewById(R.id.tvDegree)
        tvWindNumber = view.findViewById(R.id.tvWindNumber)
        tvHumidNumber = view.findViewById(R.id.tvHumidNumber)
        ivWeather = view.findViewById(R.id.ivWeather)
        paginationProgressBar = view.findViewById(R.id.paginationProgressBar)
        tvDate = view.findViewById(R.id.textView2)

        setupRecyclerView()
        setupRecyclerView2()

        // Get data from the Bundle
        arguments?.let { bundle ->
            val cityName = bundle.getString("cityName")
            if (cityName != null) {
                if (hasInternetConnection()) {
                    fetchWeatherData(cityName)
                } else {
                    fetchWeatherDataFromRoom(cityName)
                }
            } else {

            } ?: run {
                Toast.makeText(requireContext(), "No data available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchWeatherData(cityName: String) {
        showProgressBar()
        val api = RetrofitInstance.api

        lifecycleScope.launch {
            try {
                val response = api.searchCities(query = cityName)
                if (response.isSuccessful) {
                    val cities = response.body()
                    if (cities.isNullOrEmpty()) {
                        Toast.makeText(requireContext(), "No cities found", Toast.LENGTH_SHORT).show()
                        hideProgressBar()
                        return@launch
                    }
                    val location = cities[0]
                    fetchForecast(location)
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch weather data", Toast.LENGTH_SHORT).show()
                    hideProgressBar()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                hideProgressBar()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchForecast(location: com.example.weatherapp.models.Location) {
        val api = RetrofitInstance.api

        lifecycleScope.launch {
            try {
                val response = api.getForecast(
                    location = "${location.lat},${location.lon}",
                    days = 5
                )
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    if (weatherResponse != null) {
                        val next24Hours = weatherResponse.forecast.forecastday[0].hour
                        val filteredHours = next24Hours.take(24)
                        displayWeatherData(weatherResponse, filteredHours)
                        // Save fetched data to the local database
                        viewModel.saveArticle(weatherResponse.forecast, weatherResponse.current, weatherResponse.location,weatherResponse.location.name)
                    } else {
                        Toast.makeText(requireContext(), "No data found", Toast.LENGTH_SHORT).show()
                    }
                    hideProgressBar()
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch weather data", Toast.LENGTH_SHORT).show()
                    hideProgressBar()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                hideProgressBar()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchWeatherDataFromRoom(cityName: String) {
        showProgressBar()
        viewModel.getSavedForecast().observe(viewLifecycleOwner) { cityWeather ->
            hideProgressBar()
            if (cityWeather != null) {
                val next24Hours = cityWeather.forecast.forecastday.flatMap { it.hour }.take(24)
                displayWeatherData(cityWeather, next24Hours)
            } else {
                Toast.makeText(requireContext(), "No data found for $cityName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayWeatherData(cityWeather: CityWeather, hourlyData: List<Hour>) {
        val currentWeather = cityWeather.current
        val dailyData = cityWeather.forecast.forecastday
        val location = cityWeather.location

        tvDegree.text = "${currentWeather.temp_c}°C"
        tvWindNumber.text = "${currentWeather.wind_kph} km/h"
        tvHumidNumber.text = "${currentWeather.humidity}%"

        val imageUrl = "https:${currentWeather.condition.icon}"
        Glide.with(this).load(imageUrl).into(ivWeather)

        hoursAdapter.differ.submitList(hourlyData)
        daysAdapter.setData(dailyData)

        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d")
        val formattedDate = currentDate.format(formatter)
        date.text = formattedDate
        tvDate.text = formattedDate
    }

    private fun setupRecyclerView() {
        rvHours.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        hoursAdapter = HoursAdapter { }
        rvHours.adapter = hoursAdapter
    }

    private fun setupRecyclerView2() {
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvDays.layoutManager = layoutManager
        daysAdapter = DaysAdapter { }
        rvDays.adapter = daysAdapter
    }

    private fun showProgressBar() {
        paginationProgressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        paginationProgressBar.visibility = View.INVISIBLE
    }

    @SuppressLint("ServiceCast")
    private fun hasInternetConnection(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting == true
    }
}


