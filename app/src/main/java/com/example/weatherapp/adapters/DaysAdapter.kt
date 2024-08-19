package com.example.weatherapp.adapters

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.models.Day
import com.example.weatherapp.models.Forecastday
import com.example.weathersapp.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class DaysAdapter(
    private val onItemClicked: (Day) -> Unit
) : RecyclerView.Adapter<DaysAdapter.DaysViewHolder>() {

    private var daysList: List<Forecastday> = listOf()

    inner class DaysViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTemp: TextView = itemView.findViewById(R.id.tvTemp)
        val ivDayWeather: ImageView = itemView.findViewById(R.id.ivDayWeather)
        val tvDay: TextView = itemView.findViewById(R.id.tvDay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DaysViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item7, parent, false)
        return DaysViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return daysList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DaysViewHolder, position: Int) {
        val item = daysList[position]
        holder.tvTemp.text = "${item.day.avgtemp_c}°C"

        val date = LocalDate.parse(item.date, DateTimeFormatter.ISO_DATE)
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        holder.tvDay.text = dayOfWeek

        Glide.with(holder.itemView.context)
            .load("https:${item.day.condition.icon}")
            .into(holder.ivDayWeather)
        holder.itemView.setOnClickListener {
            onItemClicked(item.day)
        }
    }

    fun setData(newList: List<Forecastday>) {
        daysList = newList
        Log.d("DaysAdapter", "Number of days: ${daysList.size}")
        notifyDataSetChanged()
    }
}
