package com.example.weatherapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.models.Location
import com.example.weathersapp.R

class SearchAdapter(
    val onItemClicked: (city: Location) -> Unit
) : RecyclerView.Adapter<SearchAdapter.CityViewHolder>() {

    inner class CityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val etCityName: TextView = itemView.findViewById(R.id.tvCity2)
    }

    private val differCallback = object : DiffUtil.ItemCallback<Location>() {
        override fun areItemsTheSame(oldItem: Location, newItem: Location): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Location, newItem: Location): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item2, parent, false)
        return CityViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        Log.i("Search adapter", "getItemCount: ${differ.currentList.size}")
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val city = differ.currentList[position]

        holder.apply {
            etCityName.text = "${city.name}, ${city.region}, ${ city.country}"
            itemView.setOnClickListener {
                onItemClicked(city)
            }
        }
    }
}
