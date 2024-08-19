package com.example.weatherapp.ui.fragments

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_ETHERNET
import android.net.ConnectivityManager.TYPE_MOBILE
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.weatherapp.WeatherApp
import com.example.weatherapp.adapters.DaysAdapter
import com.example.weatherapp.adapters.HoursAdapter
import com.example.weatherapp.models.CityWeather
import com.example.weatherapp.models.Forecast
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.ui.WeatherActivity
import com.example.weatherapp.ui.WeatherViewModel
import com.example.weatherapp.util.Constants.Companion.API_KEY
import com.example.weatherapp.util.Resource
import com.example.weathersapp.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.launch
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    lateinit var viewModel: WeatherViewModel
    private lateinit var hoursAdapter: HoursAdapter
    private lateinit var daysAdapter: DaysAdapter
    private lateinit var locationRequest: LocationRequest
    private lateinit var date: TextView
    private lateinit var saved: ImageView
    private lateinit var tvDegree: TextView
    private lateinit var tvCity: TextView
    private lateinit var tvWindNumber: TextView
    private lateinit var tvHumidNumber: TextView
    private lateinit var tvDate: TextView
    private lateinit var ivWeather: ImageView
    private lateinit var rvHours: RecyclerView
    private lateinit var rvDays: RecyclerView
    private lateinit var paginationProgressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvHours = view.findViewById(R.id.rvHours)
        rvDays = view.findViewById(R.id.rvDays)
        viewModel = (activity as WeatherActivity).viewModel
        date = view.findViewById(R.id.tvData)
        saved = view.findViewById(R.id.imageView)
        tvCity = view.findViewById(R.id.tvCityName)
        tvDegree = view.findViewById(R.id.tvDegree)
        tvWindNumber = view.findViewById(R.id.tvWindNumber)
        tvHumidNumber = view.findViewById(R.id.tvHumidNumber)
        ivWeather = view.findViewById(R.id.ivWeather)
        paginationProgressBar = view.findViewById(R.id.paginationProgressBar)
        tvDate = view.findViewById(R.id.textView2)

        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.refresh)

        swipeRefreshLayout.setOnRefreshListener {
            handleAppLaunch()
            swipeRefreshLayout.isRefreshing = false
        }
        setupRecyclerView()
        setupRecyclerView2()

        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
            fastestInterval = 2000
        }
        saved.setOnClickListener {
            findNavController().navigate(R.id.savedFragment)
        }
        handleAppLaunch()
        //setupObservers()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleAppLaunch() {
        val sharedPreferences = requireContext().getSharedPreferences("LASTSHARE", Context.MODE_PRIVATE)
        val lastCity = sharedPreferences.getString("city_name_0", null)

        Log.d("HomeFragment", "Last city retrieved from SharedPreferences: $lastCity")

        if (isNetworkAvailable(requireContext())) {
            if (isGPSEnabled()) {
                getCurrentLocation()
            } else {
                showLocationDisabledBanner()
            }
        } else {
            if (lastCity != null) {
                fetchWeatherDataFromRoom(lastCity)
            } else {
                Toast.makeText(requireContext(), "No network connection and no city saved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchWeatherDataFromRoom(cityName: String) {
        viewModel.getSavedForecast().observe(viewLifecycleOwner) { forecasts ->
            if (forecasts.location.name != null) {
                val weatherResponse = forecasts
                updateUI2(weatherResponse)
                hideProgressBar()
            } else {
                Toast.makeText(requireContext(), "No data found for $cityName", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }

        return networkCapabilities?.run {
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } ?: false
    }
    private fun hasInternetConnection() : Boolean{
        val connectivityManager = context?.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        //connectivityManager.activeNetwork.isConnected  deprecated
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when{
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
        else{
            connectivityManager.activeNetworkInfo?.run {
                return when(type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (isGPSEnabled()) {
                requestLocationUpdates()
            } else {
                turnOnGPS()
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    private fun isGPSEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        LocationServices.getFusedLocationProviderClient(requireContext())
            .requestLocationUpdates(locationRequest, object : LocationCallback() {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    if (isAdded && view != null) {
                        LocationServices.getFusedLocationProviderClient(requireContext())
                            .removeLocationUpdates(this)
                        if (locationResult.locations.isNotEmpty()) {
                            val lastLocation = locationResult.locations.last()
                            val latitude = lastLocation.latitude
                            val longitude = lastLocation.longitude
                            getCityName(latitude, longitude)
                        }
                    }
                }
            }, Looper.getMainLooper())
    }

    lateinit var cityName : String
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCityName(lat: Double, lon: Double) {
        if (hasInternetConnection()) {
            viewModel.getLocation(lat, lon, API_KEY).observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Resource.Success -> {
                        hideProgressBar()
                        val weather = result.data
                        cityName = weather?.location?.name ?: "unknown city 1"
                        saveCityInPreferences(cityName, 0)
                        Log.d("HomeFragment", "Success: City Name: $cityName")
                        Log.d("HomeFragment", "Weather Response: $weather")
                        tvCity.text = cityName
                        if (weather != null) {
                            viewModel.saveArticle(weather.forecast,weather.current,weather.location,cityName)
                            updateUI(weather)
                        }
                    }
                    is Resource.Error -> {
                        Log.e("HomeFragment", "Error: ${result.message}")
                    }
                    is Resource.Loading -> {
                        showProgressBar()
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
            handleFailure()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleFailure() {
        //saveCityInPreferences("test",0)
        //saveCityInPreferences(cityName,0)
        val sharedPreferences = requireContext().getSharedPreferences("LASTSHARE", Context.MODE_PRIVATE)
        val savedCity = sharedPreferences.getString("city_name_0", null)

        if (savedCity != null) {
            Log.d("HomeFragment", "Using saved city name from SharedPreferences: $savedCity")
            fetchWeatherDataFromRoom(savedCity)
        } else {
            Log.d("HomeFragment", "No city saved in SharedPreferences")
            Toast.makeText(requireContext(), "No internet connection and no city saved", Toast.LENGTH_SHORT).show()
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchWeatherData(cityName: String) {
        viewModel.getWeatherForecast(cityName, 5)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupObservers() {
        viewModel.weatherForecast.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showProgressBar() }
                is Resource.Success -> {
                    hideProgressBar()
                    val weatherResponse = resource.data
                    if (weatherResponse != null) {
                        updateUI2(weatherResponse)
                        val forecast = weatherResponse.forecast
                        if (forecast != null) {
                            //viewModel.saveArticle(forecast,)
                        }
                    } else {
                        Toast.makeText(requireContext(), "No weather data found", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                    hideProgressBar()
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUI(weatherResponse: WeatherResponse) {
        val next24Hours = weatherResponse.forecast.forecastday.flatMap { it.hour }
        hoursAdapter.submitList(next24Hours)
        daysAdapter.setData(weatherResponse.forecast.forecastday)

        tvCity.text = weatherResponse.location.name
        tvDegree.text = "${weatherResponse.current.temp_c}°C"
        tvWindNumber.text = "${weatherResponse.current.wind_kph} km/h"
        tvHumidNumber.text = "${weatherResponse.current.humidity}%"
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d")
        val formattedDate = currentDate.format(formatter)
        tvDate.text = formattedDate
        date.text = formattedDate
        Glide.with(requireContext()).load("https:${weatherResponse.current.condition.icon}").into(ivWeather)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUI2(cityWeather: CityWeather) {
        val next24Hours = cityWeather.forecast.forecastday.flatMap { it.hour }
        hoursAdapter.submitList(next24Hours)
        val dailyData = cityWeather.forecast.forecastday
        daysAdapter.setData(dailyData)

        val cityName = cityWeather.cityNameTEXT
        val currentTemp = cityWeather.forecast.forecastday.firstOrNull()?.day?.avgtemp_c ?: "N/A"
        val windSpeed = cityWeather.forecast.forecastday.firstOrNull()?.hour?.get(0)?.wind_kph ?: "N/A"
        val humidity = cityWeather.forecast.forecastday.firstOrNull()?.hour?.get(0)?.humidity ?: "N/A"

        tvCity.text = cityName
        tvDegree.text = "${currentTemp}°C"
        tvWindNumber.text = "${windSpeed} km/h"
        tvHumidNumber.text = "${humidity}%"

        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d")
        val formattedDate = currentDate.format(formatter)
        tvDate.text = formattedDate
        date.text = formattedDate

        // Assuming you have an icon URL stored in Forecast or Day, update this accordingly
        val iconUrl = cityWeather.forecast.forecastday.firstOrNull()?.day?.condition?.icon ?: ""
        Glide.with(requireContext()).load("https:$iconUrl").into(ivWeather)
    }


    private fun saveCityInPreferences(cityName: String, index: Int) {
        val sharedPreferences = requireContext().getSharedPreferences("LASTSHARE", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("city_name_$index", cityName)
            apply()
        }
        Log.d("HomeFragment", "City name $index saved to SharedPreferences: $cityName")
    }




    private fun showLocationDisabledBanner() {
        Toast.makeText(requireContext(), "Location services are disabled. Please enable location services to get updated weather information.", Toast.LENGTH_LONG).show()
    }

    private fun turnOnGPS() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(requireContext())
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            requestLocationUpdates()
        }

        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(requireActivity(), 0x1)
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }


    private fun setupRecyclerView() {
        hoursAdapter = HoursAdapter {}
        rvHours.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvHours.adapter = hoursAdapter
    }

    private fun setupRecyclerView2() {
        daysAdapter = DaysAdapter {}
        rvDays.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvDays.adapter = daysAdapter
    }

    private fun showProgressBar() {
        paginationProgressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        paginationProgressBar.visibility = View.GONE
    }
}
