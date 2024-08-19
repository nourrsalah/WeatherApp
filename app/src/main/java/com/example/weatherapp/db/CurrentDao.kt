package com.example.weatherapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherapp.models.CityWeather
import com.example.weatherapp.models.Forecast
import com.example.weatherapp.models.WeatherResponse

@Dao
interface CurrentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(cityWeather: CityWeather) : Long

    @Query("SELECT * FROM weather")
    fun getAll(): LiveData<CityWeather>


//    @Delete
//    suspend fun deleteCurrent(forecast: Forecast)


}