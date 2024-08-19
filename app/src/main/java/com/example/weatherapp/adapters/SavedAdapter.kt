package com.example.weatherapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.models.CityWeather
import com.example.weatherapp.models.WeatherResponse
import com.example.weathersapp.R

class SavedAdapter(private val onItemClick: (CityWeather) -> Unit) : RecyclerView.Adapter<SavedAdapter.CityViewHolder>() {

    inner class CityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCityName: TextView = itemView.findViewById(R.id.tvCity)
        val tvWindNumber: TextView = itemView.findViewById(R.id.tvWindNumber)
        val tvHumidNumber: TextView = itemView.findViewById(R.id.tvHumidNumber)
        val tvDegree: TextView = itemView.findViewById(R.id.tvDegree)
        val ivWeather: ImageView = itemView.findViewById(R.id.ivWeather)
        val layout: ConstraintLayout = itemView.findViewById(R.id.lay)


    }

    private val differCallback = object : DiffUtil.ItemCallback<CityWeather>() {
        override fun areItemsTheSame(oldItem: CityWeather, newItem: CityWeather): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CityWeather, newItem: CityWeather): Boolean {
            return oldItem == newItem
        }

    }

    private val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return CityViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val weatherResponse = differ.currentList[position]

        holder.tvCityName.text = "${weatherResponse.location.name}, ${weatherResponse.location.region}, ${weatherResponse.location.country}"
        holder.tvDegree.text = "${weatherResponse.current.temp_c}°C"
        holder.tvWindNumber.text = "${weatherResponse.current.wind_kph} km/h"
        holder.tvHumidNumber.text = "${weatherResponse.current.humidity}%"

        holder.layout.setOnClickListener {
            val weatherResponse = differ.currentList[position]
            onItemClick(weatherResponse)
        }
        val iconUrl = "https:${weatherResponse.current.condition.icon}"
        Glide.with(holder.itemView.context).load(iconUrl).into(holder.ivWeather)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<CityWeather>) {
        differ.submitList(list)
    }
}
