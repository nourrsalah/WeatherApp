package com.example.weatherapp.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.weatherapp.Converters
import com.example.weatherapp.models.CityWeather
import com.example.weatherapp.models.Forecast
import com.example.weatherapp.models.Forecastday


@Database(
    entities = [CityWeather::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CurrentDatabase : RoomDatabase() {
    abstract fun getCurrentDao(): CurrentDao

    companion object {
        @Volatile
        private var instance: CurrentDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also {
                instance = it
            }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                CurrentDatabase::class.java,
                "weather_db.db"
            ).fallbackToDestructiveMigration() // This line clears the database if a migration is missing
                .build().apply {
                    Log.d("Room", "DB CREATED $this")
                }
    }
}
