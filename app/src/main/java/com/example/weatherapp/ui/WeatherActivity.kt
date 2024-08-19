package com.example.weatherapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.db.CurrentDatabase
import com.example.weatherapp.repository.WeatherRepository
import com.example.weatherapp.ui.fragments.LaunchFragment
import com.example.weatherapp.ui.WeatherViewModel
import com.example.weatherapp.ui.WeatherViewModelProviderFactory
import com.example.weatherapp.api.RetrofitInstance
import com.example.weathersapp.R

class WeatherActivity : AppCompatActivity() {

    val viewModel: WeatherViewModel by lazy {
        val weatherRepository = WeatherRepository(CurrentDatabase(this))
        val viewModelProviderFactory = WeatherViewModelProviderFactory(application, weatherRepository)
        ViewModelProvider(this, viewModelProviderFactory)[WeatherViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.weatherNavFragment, LaunchFragment())
            }
        }
    }
}
