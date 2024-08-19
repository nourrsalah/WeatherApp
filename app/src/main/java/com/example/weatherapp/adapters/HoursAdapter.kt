package com.example.weatherapp.adapters

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.models.Hour
import com.example.weathersapp.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HoursAdapter(
    val onItemClicked: (hour: Hour) -> Unit
) : RecyclerView.Adapter<HoursAdapter.HoursViewHolder>() {

    inner class HoursViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTemp: TextView = itemView.findViewById(R.id.tvTemp)
        val ivHourWeather: ImageView = itemView.findViewById(R.id.ivHourWeather)
        val tvHour: TextView = itemView.findViewById(R.id.tvHour)
    }

    private val differCallback = object : DiffUtil.ItemCallback<Hour>() {
        override fun areItemsTheSame(oldItem: Hour, newItem: Hour): Boolean {
            return oldItem.time == newItem.time
        }

        override fun areContentsTheSame(oldItem: Hour, newItem: Hour): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoursViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item5, parent, false)
        return HoursViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        Log.i("HoursAdapter", "getItemCount: ${differ.currentList.size}")
        return differ.currentList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: HoursViewHolder, position: Int) {
        val hour = differ.currentList[position]

        holder.apply {
            tvTemp.text = "${hour.temp_c}°C"

            tvHour.text = hour.time.split(" ")[1]
            Glide.with(itemView.context)
                .load(getWeatherIconUrl(hour.condition.icon))
                .into(ivHourWeather)

            itemView.setOnClickListener {
                onItemClicked(hour)
            }
        }
    }
    private fun getWeatherIconUrl(iconPath: String): String {
        return "https:$iconPath"
    }
    fun submitList(hours: List<Hour>) {
        differ.submitList(hours)
    }
}
