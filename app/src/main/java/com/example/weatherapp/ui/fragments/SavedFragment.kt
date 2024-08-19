package com.example.weatherapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.adapters.SavedAdapter
import com.example.weatherapp.models.Current
import com.example.weatherapp.models.Location
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.ui.WeatherActivity
import com.example.weatherapp.ui.WeatherViewModel
import com.example.weathersapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SavedFragment : Fragment() {

    lateinit var viewModel: WeatherViewModel
    private lateinit var savedAdapter: SavedAdapter
    private lateinit var rvSaved: RecyclerView
    private lateinit var tvDate: TextView
    private lateinit var paginationProgressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_saved, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as WeatherActivity).viewModel
        rvSaved = view.findViewById(R.id.rvSaved)
        tvDate = view.findViewById(R.id.textView6)
        paginationProgressBar = view.findViewById(R.id.paginationProgressBar)

        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d")
        val formattedDate = currentDate.format(formatter)
        tvDate.text = formattedDate

        val floatingActionButton = view.findViewById<FloatingActionButton>(R.id.floatingActionButton)
        floatingActionButton.setOnClickListener {
            val bottomSheetFragment = HalfScreenFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        setupRecyclerView()

        if (hasInternetConnection()) {
            viewModel.savedCities.observe(viewLifecycleOwner) { savedCities ->
                savedAdapter.submitList(savedCities)
            }
        } else {
            fetchWeatherDataFromRoom()
        }
    }
private lateinit var current1: Current
private lateinit var location1: Location
    private fun setupRecyclerView() {
        savedAdapter = SavedAdapter { weatherResponse ->
            val forecast = weatherResponse.forecast
            current1 = weatherResponse.current
            //Log.d("SAVED","before $current1, $location1" )
            location1 = weatherResponse.location
            Log.d("SAVED","before $current1, $location1" )
            if (forecast != null) {
                viewModel.saveArticle(forecast, weatherResponse.current,weatherResponse.location,weatherResponse.location.name)
                current1 = weatherResponse.current
                location1 = weatherResponse.location
                Log.d("SAVED","after $current1, $location1" )
                val bundle = Bundle().apply {
                    putString("cityName", weatherResponse.location.name)
                }
                findNavController().navigate(R.id.redirectFragment, bundle)
            }


        }
        rvSaved.apply {
            adapter = savedAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun showProgressBar() {
        paginationProgressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        paginationProgressBar.visibility = View.GONE
    }

    @SuppressLint("ServiceCast")
    private fun hasInternetConnection(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting == true
    }

    private fun fetchWeatherDataFromRoom() {
        showProgressBar()
        viewModel.getSavedForecast().observe(viewLifecycleOwner) { savedForecasts ->
            hideProgressBar()
            if (savedForecasts.location.name != null) {
                val weatherResponse = savedForecasts
                savedAdapter.submitList(listOf(weatherResponse))
            } else {
                Snackbar.make(requireView(), "No saved data available", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    }

