package com.example.weatherapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.adapters.SearchAdapter
import com.example.weatherapp.ui.WeatherActivity
import com.example.weatherapp.ui.WeatherViewModel
import com.example.weatherapp.util.Resource
import com.example.weathersapp.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

class HalfScreenFragment : BottomSheetDialogFragment() {
    lateinit var viewModel: WeatherViewModel
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var rvSearch: RecyclerView
    private lateinit var paginationProgressBar: ProgressBar
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_half_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as WeatherActivity).viewModel
        rvSearch = view.findViewById(R.id.rvSearch)
        paginationProgressBar = view.findViewById(R.id.paginationProgressBar)
        searchView = view.findViewById(R.id.searchView)

        setupRecyclerView()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.searchCities(it)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        viewModel.searchWeather.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { locations ->
                        searchAdapter.differ.submitList(locations)

                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity, "An error occurred: $message", Toast.LENGTH_LONG)
                            .show()
                        Log.e("HalfScreenFragment", "An error occurred: $message")
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
    }

    private fun hideProgressBar() {
        paginationProgressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        paginationProgressBar.visibility = View.VISIBLE
    }

    private fun setupRecyclerView() {
        searchAdapter = SearchAdapter { city ->
            viewModel.saveCity(city)
            Snackbar.make(rvSearch, "City saved successfully", Snackbar.LENGTH_SHORT).show()
            dismiss()
        }
        rvSearch.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }
}


