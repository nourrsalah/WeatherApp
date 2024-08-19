package com.example.weatherapp

import android.app.Application
import android.util.Log
import com.example.weatherapp.db.CurrentDatabase

class WeatherApp : Application(){
    override fun onCreate() {
        super.onCreate()
        val db = CurrentDatabase.invoke(this)
        Log.d("DatabaseTest", "Database initialized: $db")
    }
}